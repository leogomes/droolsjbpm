package org.drools.ide.editors.completion; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.ColumnDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.semantics.java.ClassTypeResolver;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.FileEditorInput;

/**
 * For handling within rules, including DSLs.
 * At present this provides a fixed list, plus what is available
 * in the DSL configuration. 
 * 
 * TODO: This can be enhanced to look back for declarations, and introspect to get
 * field names. (More can be done as well, this would just be the first step).
 * 
 * This also handles queries, as they are just a type of rule essentially.
 * 
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {
	
    private static final Pattern query = Pattern.compile(".*\\Wquery\\W.*", Pattern.DOTALL);
    private static final Image droolsIcon = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
    private static final Image dslIcon = DroolsPluginImages.getImage( DroolsPluginImages.DSL_EXPRESSION );
    private static final Image classIcon = DroolsPluginImages.getImage(DroolsPluginImages.CLASS);
    private static final Pattern START_OF_CONSEQUENCE = Pattern.compile(".*then\\s*", Pattern.DOTALL);
    
    public RuleCompletionProcessor(DRLRuleEditor editor) {
    	super(editor);
    }
    
	protected List getCompletionProposals(ITextViewer viewer, final int documentOffset) {
        try {
	        final List list = new ArrayList();
	        DSLAdapter adapter = getDSLAdapter(viewer);
	        
	        IDocument doc = viewer.getDocument();
	        String backText = readBackwards(documentOffset, doc);            
	
	        final String prefix = stripWhiteSpace(backText);
            
	        if (backText.length() < 5) {
	        	return list;
	        }
            
        	if (consequence(backText)) {
	        	List dslConsequences = adapter.listConsequenceItems();
                addDSLProposals(list, prefix, dslConsequences);
	            if (!adapter.hasConsequences()) {
	            	// only add functions and keywords if at the beginning of a new statement
	            	String backTextWithoutPrefix = backText.substring(0, backText.length() - prefix.length());
	            	if (START_OF_CONSEQUENCE.matcher(backTextWithoutPrefix).matches()
	            			|| START_OF_NEW_JAVA_STATEMENT.matcher(backTextWithoutPrefix).matches()) {
            			addRHSCompletionProposals(list, prefix);                    
            			addRHSFunctionCompletionProposals(viewer, list, prefix);
	            	}
        			addRHSJavaCompletionProposals(list, backText, prefix);
	            }
	        } else if (condition(backText) || query(backText)) {
	        	List dslConditions = adapter.listConditionItems();
	        	addDSLProposals(list, prefix, dslConditions);
	            addLHSCompletionProposals(viewer, list, adapter, prefix, backText);
	        } else {             
	            //we are in rule header
	            addRuleHeaderItems(list, prefix);
	        }
            
            filterProposalsOnPrefix(prefix, list);
	        return list;           
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
    }

	private void addLHSCompletionProposals(ITextViewer viewer, final List list, DSLAdapter adapter, final String prefix, String backText) throws CoreException, DroolsParserException {
		Iterator iterator;
		if (!adapter.hasConditions()) {
			// determine location in condition
			LocationDeterminator.Location location = LocationDeterminator.getLocationInCondition(backText);
			
			switch (location.getType()) {
				case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION: 
					// if we are at the beginning of a new condition
					// add drools keywords
				    list.add( new RuleCompletionProposal(prefix.length(), "and", "and ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "or", "or ", droolsIcon));
				    RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "eval", "eval(  )", 6 );
					prop.setImage(droolsIcon);
				    list.add(prop);
					prop = new RuleCompletionProposal(prefix.length(), "then", "then" + System.getProperty("line.separator") + "\t");
					prop.setImage(droolsIcon);
					list.add(prop);
				    // we do not break but also add all elements that are needed for and/or
				case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR:
				    list.add( new RuleCompletionProposal(prefix.length(), "not", "not ", droolsIcon));
				    // we do not break but also add all elements that are needed for not
				case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT:
					list.add( new RuleCompletionProposal(prefix.length(), "exists", "exists ", droolsIcon));
				    // we do not break but also add all elements that are needed for exists
				case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS:
				    // and add imported classes
				    List imports = getDRLEditor().getImports();
				    iterator = imports.iterator();
				    while (iterator.hasNext()) {
				        String name = (String) iterator.next();
				        int index = name.lastIndexOf(".");
				        if (index != -1) {
				        	String className = name.substring(index + 1);
				        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), className, className + "(  )", className.length() + 2);
				        	p.setPriority(-1);
				        	p.setImage(classIcon);
				        	list.add(p);
				        }
				    }
				    List classesInPackage = getDRLEditor().getClassesInPackage();
				    iterator = classesInPackage.iterator();
				    while (iterator.hasNext()) {
				        String name = (String) iterator.next();
				        int index = name.lastIndexOf(".");
				        if (index != -1) {
				        	String className = name.substring(index + 1);
				        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), className, className + "(  )", className.length() + 2);
				        	p.setPriority(-1);
				        	p.setImage(classIcon);
				        	list.add(p);
				        }
				    }
					break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_START :
					String className = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
					if (className != null) {
						ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor().getImports(), ProjectClassLoader.getProjectClassLoader(getEditor()));
						try {
							Class clazz = resolver.resolveType(className);
							if (clazz != null) {
								Iterator iterator2 = new ClassFieldInspector(clazz).getFieldNames().keySet().iterator();
								while (iterator2.hasNext()) {
							        String name = (String) iterator2.next();
						        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), name, name + " ");
						        	p.setImage(methodIcon);
						        	list.add(p);
							    }
							}
						} catch (IOException exc) {
							// Do nothing
						} catch (ClassNotFoundException exc) {
							// Do nothing
						}
					}
					break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR :
					className = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
					String property = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME);
					String type = getPropertyClass(className, property);
					
				    list.add( new RuleCompletionProposal(prefix.length(), "==", "== ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "!=", "!= ", droolsIcon));
					list.add( new RuleCompletionProposal(prefix.length(), ":", ": ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "->", "-> (  )", 5, droolsIcon));
				    
				    if (isComparable(type)) {
						list.add( new RuleCompletionProposal(prefix.length(), "<", "< ", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), "<=", "<= ", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), ">", "> ", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), ">=", ">= ", droolsIcon));
				    }
				    if (type.equals("java.lang.String")) {
				    	list.add( new RuleCompletionProposal(prefix.length(), "matches", "matches \"\"", 9, droolsIcon));
				    }
				    if (isSubtypeOf(type, "java.util.Collection")) {
					    list.add( new RuleCompletionProposal(prefix.length(), "contains", "contains ", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), "excludes", "excludes ", droolsIcon));
				    }
				    break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT :
					// determine type
					className = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
					property = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME);
					String operator = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR);
					type = getPropertyClass(className, property);
					
					if ("contains".equals(operator) || "excludes".equals(operator)) {
						type = "java.lang.Object";
					}
					
					boolean isObject = false;
					if ("java.lang.Object".equals(type)) {
						isObject = true;
					}

				    list.add( new RuleCompletionProposal(prefix.length(), "null", "null", droolsIcon));
					if ("boolean".equals(type)) {
					    list.add( new RuleCompletionProposal(prefix.length(), "true", "true", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), "false", "false", droolsIcon));
					}
					if (isObject || "java.lang.String".equals(type)) {
						list.add( new RuleCompletionProposal(prefix.length(), "\"\"", "\"\"", 1, droolsIcon));
					}
					if (isObject || "java.util.Date".equals(type)) {
						list.add( new RuleCompletionProposal(prefix.length(), "\"dd-mmm-yyyy\"", "\"dd-mmm-yyyy\"", 1, droolsIcon));
					}
				    list.add( new RuleCompletionProposal(prefix.length(), "()", "(  )", 2, droolsIcon));
			    	DrlParser parser = new DrlParser();
			    	try {
			    		PackageDescr descr = parser.parse(backText);
			    		List rules = descr.getRules();
			    		if (rules != null && rules.size() == 1) {
			    			Map result = new HashMap();
			    			getRuleParameters(result, ((RuleDescr) rules.get(0)).getLhs().getDescrs());
			    			Iterator iterator2 = result.entrySet().iterator();
			    			while (iterator2.hasNext()) {
			    				Map.Entry entry = (Map.Entry) iterator2.next();
			    				String paramName = (String) entry.getKey();
			    				String paramType = (String) entry.getValue();
			    				if (isSubtypeOf(paramType, type)) {
			    					RuleCompletionProposal proposal = new RuleCompletionProposal(prefix.length(), paramName);
				    				proposal.setPriority(-1);
				    				proposal.setImage(methodIcon);
									list.add(proposal);
			    				}
			    			}
			    		}
			    	} catch (DroolsParserException exc) {
			    		// do nothing
			    	}
				    break;
				case LocationDeterminator.LOCATION_INSIDE_EVAL :
					String content = (String) location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT);
	    			Map params = new HashMap();
					try {
				    	parser = new DrlParser();
			    		PackageDescr descr = parser.parse(backText);
			    		List rules = descr.getRules();
			    		if (rules != null && rules.size() == 1) {
			    			getRuleParameters(params, ((RuleDescr) rules.get(0)).getLhs().getDescrs());
			    			// rule params are already added by JavaCompletionProposals
			    			// 
			    			// Iterator iterator2 = params.keySet().iterator();
			    			// while (iterator2.hasNext()) {
			    			// 	String name = (String) iterator2.next();
			    			// 	RuleCompletionProposal proposal = new RuleCompletionProposal(prefix.length(), name);
			    			// 	proposal.setPriority(-1);
			    			// 	proposal.setImage(methodIcon);
							// 	list.add(proposal);
			    			// }
			    		}
			    	} catch (DroolsParserException exc) {
			    		// do nothing
			    	}
			    	list.addAll(getJavaCompletionProposals(content, prefix, params));
			    	break;
			}
		}
	}
	
	private String getPropertyClass(String className, String propertyName) {
		if (className != null) {
			ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor().getImports(), ProjectClassLoader.getProjectClassLoader(getEditor()));
			try {
				Class clazz = resolver.resolveType(className);
				if (clazz != null) {
					Class clazzz = (Class) new ClassFieldInspector(clazz).getFieldTypes().get(propertyName);
					if (clazzz != null) {
						return clazzz.getName();
					}
				}
			} catch (IOException exc) {
				// Do nothing
			} catch (ClassNotFoundException exc) {
				// Do nothing
			}
		}
		return null;
	}
	
	private boolean isComparable(String type) {
		if (type == null) {
			return false;
		}
		if (isPrimitiveNumericType(type)) {
			return true;
		}
		if (isSubtypeOf(type, "java.lang.Comparable")) {
			return true;
		}
		return false;
	}
	
	private boolean isPrimitiveType(String type) {
		return isPrimitiveNumericType(type)
			|| type.equals("boolean");
	}
	
	private boolean isPrimitiveNumericType(String type) {
		return type.equals("byte") || type.equals("short")
				|| type.equals("int") || type.equals("long")
				|| type.equals("float") || type.equals("double")
				|| type.equals("char");
	}
	
	/**
	 * Returns true if the first class is the same or a subtype of the second class.
	 * @param class1
	 * @param class2
	 * @return
	 */
	private boolean isSubtypeOf(String class1, String class2) {
		if (class1 == null || class2 == null) {
			return false;
		}
		class1 = convertToNonPrimitiveClass(class1);
		class2 = convertToNonPrimitiveClass(class2);
		// TODO add code to take primitive types into account
		ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor().getImports(), ProjectClassLoader.getProjectClassLoader(getEditor()));
		try {
			Class clazz1 = resolver.resolveType(class1);
			Class clazz2 = resolver.resolveType(class2);
			if (clazz1 == null || clazz2 == null) {
				return false;
			}
			return clazz2.isAssignableFrom(clazz1);
		} catch (ClassNotFoundException exc) {
			return false;
		}
	}
	
	private String convertToNonPrimitiveClass(String clazz) {
		if (!isPrimitiveType(clazz)) {
			return clazz;
		}
		if ("byte".equals(clazz)) {
			return "java.lang.Byte";
		} else if ("short".equals(clazz)) {
			return "java.lang.Short";
		} else if ("int".equals(clazz)) {
			return "java.lang.Integer";
		} else if ("long".equals(clazz)) {
			return "java.lang.Long";
		} else if ("float".equals(clazz)) {
			return "java.lang.Float";
		} else if ("double".equals(clazz)) {
			return "java.lang.Double";
		} else if ("char".equals(clazz)) {
			return "java.lang.Char";
		} else if ("boolean".equals(clazz)) {
			return "java.lang.Boolean";
		}
		// should never occur
		return null;
	}
	
	private boolean consequence(String backText) {
		return isKeywordOnLine(backText, "then");
	}

	private boolean condition(String backText) {
		return isKeywordOnLine(backText, "when");
	}

	boolean query(String backText) {
		return query.matcher(backText).matches();
	}
	
	/**
	 * Check to see if the keyword appears on a line by itself.
	 */
	private boolean isKeywordOnLine(String chunk, String keyword) {
		StringTokenizer st = new StringTokenizer(chunk, "\n\t");
    	while(st.hasMoreTokens()) {
    		if (st.nextToken().trim().equals(keyword)) {
    			return true;
    		}    		
    	}
    	return false;
	}

    private void addRHSFunctionCompletionProposals(ITextViewer viewer,
                                                   final List list,
                                                   final String prefix) throws CoreException,
                                                                       DroolsParserException {
        Iterator iterator;
        RuleCompletionProposal prop;
        List functions = getDRLEditor().getFunctions();
        iterator = functions.iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next() + "()";
        	prop = new RuleCompletionProposal(prefix.length(), name, name + ";", name.length() - 1);
        	prop.setPriority(-1);
        	prop.setImage(methodIcon);
        	list.add(prop);
        }
    }

    private void addRHSCompletionProposals(final List list,
                                           final String prefix) {
        RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "modify", "modify();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "retract", "retract();", 8);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assert", "assert();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assertLogical", "assertLogical();", 14);
        prop.setImage(droolsIcon);
        list.add(prop);
    }
    
    private void addRHSJavaCompletionProposals(List list, String backText, String prefix) {
    	int thenPosition = backText.lastIndexOf("then");
    	String conditions = backText.substring(0, thenPosition);
		Map params = new HashMap();
    	DrlParser parser = new DrlParser();
    	try {
    		PackageDescr descr = parser.parse(conditions);
    		List rules = descr.getRules();
    		if (rules != null && rules.size() == 1) {
    			getRuleParameters(params, ((RuleDescr) rules.get(0)).getLhs().getDescrs());
    			// rule params are already added by JavaCompletionProposals
    			// 
    			// Iterator iterator = params.keySet().iterator();
    			// while (iterator.hasNext()) {
    			// 	String name = (String) iterator.next();
    			// 	RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), name, name + ".");
				// 	prop.setPriority(-1);
				// 	prop.setImage(methodIcon);
				// 	list.add(prop);
    			// }
    		}
    	} catch (DroolsParserException exc) {
    		// do nothing
    	}
    	String consequence = backText.substring(thenPosition + 4);
    	list.addAll(getJavaCompletionProposals(consequence, prefix, params));
    }
    
    private void getRuleParameters(Map result, List descrs) {
    	if (descrs == null) {
    		return;
    	}
    	Iterator iterator = descrs.iterator();
    	while (iterator.hasNext()) {
    		PatternDescr descr = (PatternDescr) iterator.next();
    		if (descr instanceof ColumnDescr) {
				String name = ((ColumnDescr) descr).getIdentifier();
				if (name != null) {
					result.put(name, ((ColumnDescr) descr).getObjectType());
				}
				getRuleSubParameters(result, ((ColumnDescr) descr).getDescrs(), ((ColumnDescr) descr).getObjectType());
			} else if (descr instanceof AndDescr) {
				getRuleParameters(result, ((AndDescr) descr).getDescrs());
			} else if (descr instanceof OrDescr) {
				getRuleParameters(result, ((OrDescr) descr).getDescrs());
			} else if (descr instanceof ExistsDescr) {
				getRuleParameters(result, ((ExistsDescr) descr).getDescrs());
			} else if (descr instanceof NotDescr) {
				getRuleParameters(result, ((NotDescr) descr).getDescrs());
			}
		}
    }
    
    private void getRuleSubParameters(Map result, List descrs, String clazz) {
    	if (descrs == null) {
    		return;
    	}
    	Iterator iterator = descrs.iterator();
    	while (iterator.hasNext()) {
    		PatternDescr descr = (PatternDescr) iterator.next();
    		if (descr instanceof FieldBindingDescr) {
				FieldBindingDescr fieldDescr = (FieldBindingDescr) descr;
				String name = fieldDescr.getIdentifier();
				String field = fieldDescr.getFieldName();
				String type = getPropertyClass(clazz, field);
				if (name != null) {
					result.put(name, type);
				}
			}
    	}
    }

    private void addRuleHeaderItems(final List list,
                                    final String prefix) {
        list.add(new RuleCompletionProposal(prefix.length(), "salience", "salience ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "no-loop", "no-loop ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "agenda-group", "agenda-group ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "duration", "duration ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "auto-focus", "auto-focus ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "when", "when" + System.getProperty("line.separator") + "\t ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "activation-group", "activation-group ", droolsIcon));        
    }

    private void addDSLProposals(final List list,
                                 final String prefix,
                                 List dslItems) {
        Iterator iterator = dslItems.iterator();
        while (iterator.hasNext()) {
        	String consequence = (String) iterator.next();
            RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), consequence);
            p.setImage( dslIcon );
            list.add(p);
        }
    }

    /** 
     * Lazily get the adapter for DSLs, and cache it with the editor for future reference.
     * If it is unable to load a DSL, it will try again next time.
     * But once it has found and loaded one, it will keep it until the editor is closed.
     * 
     * This delegates to DSLAdapter to poke around the project to try and load the DSL.
     */
    private DSLAdapter getDSLAdapter(ITextViewer viewer) {
    	// TODO: cache DSL adapter in plugin, and reset when dsl file saved
    	// retrieve dsl name always (might have changed) and try retrieving
    	// cached dsl from plugin first
//    	return new DSLAdapter(viewer.getDocument().get(), ((FileEditorInput) getEditor().getEditorInput()).getFile());
        DSLAdapter adapter = getDRLEditor().getDSLAdapter();
        if (adapter == null) {
            String content = viewer.getDocument().get();
            adapter = new DSLAdapter(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
            if (adapter.isValid()) {
            	getDRLEditor().setDSLAdapter(adapter);
            }
        }
        return adapter;
    }

}
