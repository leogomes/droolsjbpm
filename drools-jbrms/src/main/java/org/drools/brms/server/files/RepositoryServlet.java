package org.drools.brms.server.files;

import javax.servlet.http.HttpServlet;

import org.drools.brms.server.util.FileManagerUtils;
import org.drools.brms.server.util.TestEnvironmentSessionHelper;
import org.drools.repository.RulesRepository;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;

/**
 * This is a base servlet that all repo servlets inherit behaviour from. 
 * 
 * @author Michael Neale
 */
public class RepositoryServlet extends HttpServlet {

    private static final long serialVersionUID = 3909768997932550498L;
    protected final FileManagerUtils uploadHelper = new FileManagerUtils();    
    
    protected RulesRepository getRepository() {
        if ( Contexts.isApplicationContextActive() ) {
            return (RulesRepository) Component.getInstance( "repository" );
        } else {
            //MN: NOTE THIS IS MY HACKERY TO GET IT WORKING IN GWT HOSTED MODE.
            //THIS IS ALL THAT IS NEEDED.
            System.out.println( "WARNING: RUNNING IN NON SEAM MODE SINGLE USER MODE - ONLY FOR TESTING AND DEBUGGING !!!!!" );

            try {
                return new RulesRepository( TestEnvironmentSessionHelper.getSession( false ) );
            } catch ( Exception e ) {
                throw new IllegalStateException( "Unable to launch debug mode..." );
            }
        }
    }    
    
}
