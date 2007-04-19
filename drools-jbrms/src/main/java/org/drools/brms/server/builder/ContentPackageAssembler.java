package org.drools.brms.server.builder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarInputStream;

import org.drools.brms.client.common.AssetFormats;
import org.drools.brms.server.contenthandler.ContentHandler;
import org.drools.brms.server.contenthandler.IRuleAsset;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.PackageDescr;
import org.drools.repository.AssetItem;
import org.drools.repository.AssetItemIterator;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepositoryException;
import org.drools.repository.VersionableItem;
import org.drools.rule.Package;

/**
 * This assembles packages in the BRMS into binary package objects, and deals with errors etc.
 * Each content type is responsible for contributing to the package. 
 * 
 * @author Michael Neale
 */
public class ContentPackageAssembler {

    private PackageItem pkg;
    
    /**
     * We accumulate errors here. If they come from the builder,
     * then we reset the builders errors so as to not double report.
     * It also means we can track errors to the exact asset that caused it.
     */
    private List<ContentAssemblyError> errors = new ArrayList<ContentAssemblyError>();

    BRMSPackageBuilder builder;
    
    public ContentPackageAssembler(PackageItem assetPackage) {
        this.pkg = assetPackage;
        
        if (preparePackage()) {
            buildPackage();
        }
        
    }
    
    /**
     * This will build the package. 
     */
    private void buildPackage() {
        Iterator it = pkg.getAssets();
        while (it.hasNext()) {
            AssetItem asset = (AssetItem) it.next();
            ContentHandler h = ContentHandler.getHandler( asset.getFormat() );
            if (h instanceof IRuleAsset) {
                try {
                    ((IRuleAsset) h).compile( builder, asset, new ErrorLogger() );
                    if (builder.hasErrors()) {
                        this.recordBuilderErrors( asset );
                        //clear the errors, so we don't double report.
                        builder.clearErrors();
                    }
                } catch ( DroolsParserException e ) {
                    throw new RulesRepositoryException(e);
                } catch ( IOException e ) {
                    throw new RulesRepositoryException(e);
                }
            }
        }
    }

    /**
     * This prepares the package builder, loads the jars/classpath.
     * @return true if everything is good to go, false if its all gone horribly wrong, 
     * and we can't even get the package header up.
     */
    private boolean preparePackage() {
        
        //firstly we loadup the classpath
        List<JarInputStream> jars = BRMSPackageBuilder.getJars( pkg );
        builder = BRMSPackageBuilder.getInstance( jars );
        builder.addPackage( new PackageDescr(pkg.getName()) );
        
        //now we deal with the header (imports, templates, globals).
        addDrl(pkg.getHeader());
        if (builder.hasErrors()) {
            recordBuilderErrors(pkg);
            //if we have any failures, lets drop out now, no point in going
            //any further
            return false;
        }

        //now we load up the DSL files
        builder.setDSLFiles( BRMSPackageBuilder.getDSLMappingFiles( pkg, new BRMSPackageBuilder.DSLErrorEvent() {
            public void recordError(AssetItem asset, String message) {
                errors.add( new ContentAssemblyError(asset, message) );
            }
        }));
        
        //finally, any functions we will load at this point.
        AssetItemIterator it = this.pkg.listAssetsByFormat( new String[] {AssetFormats.FUNCTION} );
        while(it.hasNext()) {
            AssetItem func = (AssetItem) it.next();
            addDrl( func.getContent() );
            if (builder.hasErrors()) {
                recordBuilderErrors(func);
                builder.clearErrors();
            }
        }
        
        return errors.size() == 0;
    }

    /**
     * This will return true if there is an error in the package configuration or functions.
     * @return
     */
    public boolean isPackageConfigurationInError() {
        if (this.errors.size() > 0) {
            return this.errors.get( 0 ).itemInError instanceof PackageItem;
        } else {
            return false;
        }
    }
    
    private void addDrl(String drl) {
        try {
            builder.addPackageFromDrl( new StringReader(drl) );
        } catch ( DroolsParserException e ) {
            throw new RulesRepositoryException("Unexpected error when parsing package.", e);
        } catch ( IOException e ) {
            throw new RulesRepositoryException("IO Exception occurred when parsing package.", e);
        }
    }



    /**
     * This will accumulate the errors.
     */
    private void recordBuilderErrors(VersionableItem asset) {
        DroolsError[] errs = builder.getErrors();
        for ( int i = 0; i < errs.length; i++ ) {
            this.errors.add( new ContentAssemblyError(asset, errs[i].getMessage()) );
        }
        
    }

    /**
     * I've got a package people !
     */
    public Package getBinaryPackage() {
        if (this.hasErrors()) {
            throw new IllegalStateException("There is no package available, as there were errors.");
        }
        return builder.getPackage();
    }

    
    public boolean hasErrors() {
        return errors.size() > 0;
    }
    
    public List<ContentAssemblyError> getErrors() {
        return this.errors;
    }
    
    
    /**
     * This is passed in to the compilers so extra errors can be added.
     * 
     * @author Michael Neale
     */
    public class ErrorLogger {
        public void logError(ContentAssemblyError err) {
            errors.add(err);
        }
    }
    
}
