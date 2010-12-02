package org.drools.process.instance;

import org.drools.common.AbstractWorkingMemory;
import org.drools.runtime.process.InternalProcessRuntime;
import org.drools.runtime.process.ProcessRuntimeFactoryService;

public class ProcessRuntimeFactoryServiceImpl implements ProcessRuntimeFactoryService {

	public InternalProcessRuntime newProcessRuntime(AbstractWorkingMemory workingMemory) {
		return new ProcessRuntimeImpl(workingMemory);
	}
	
}
