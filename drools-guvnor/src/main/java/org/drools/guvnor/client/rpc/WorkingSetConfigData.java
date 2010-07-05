package org.drools.guvnor.client.rpc;

import java.io.Serializable;
import java.util.List;

import org.drools.factconstraints.client.ConstraintConfiguration;
import org.drools.factconstraints.client.customform.CustomFormConfiguration;
import org.drools.ide.common.client.modeldriven.brl.PortableObject;

/**
 * 
 * @author bauna
 */
public class WorkingSetConfigData implements PortableObject, Serializable {
	private static final long serialVersionUID = 510L;

	public String name;
	public String description;
	public List<ConstraintConfiguration> constraints;
	public List<CustomFormConfiguration> customForms;
	
	public String[] validFacts;
	public WorkingSetConfigData[] workingSets;	
}
