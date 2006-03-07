package org.drools.reteoo;

/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.drools.FactException;
import org.drools.spi.FieldConstraint;
import org.drools.spi.PropagationContext;

/**
 * <code>AlphaNodes</code> are nodes in the <code>Rete</code> network used
 * to apply <code>FieldConstraint<.code>s on asserted fact 
 * objects where the <code>FieldConstraint</code>s have no dependencies on any other of the facts in the current <code>Rule</code>.
 * 
 *  @see FieldConstraint
 * 
 * @author <a href="mailto:mark.proctor@jboss.com">Mark Proctor</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 *
 */
class AlphaNode extends ObjectSource
    implements
    ObjectSink,
    NodeMemory {

    /** The <code>FieldConstraint</code> */
    private final FieldConstraint constraint;

    /** The <code>ObjectSource</code> */
    private final ObjectSource    objectSource;

    /**
     * Construct an <code>AlphaNode</code> with a unique id using the provided
     * <code>FieldConstraint</code>. <code>NodeMemory</code> is optional in
     * <code>AlphaNode</code>s and is only of benefit when adding additional
     * <code>Rule</code>s at runtime.
     * 
     * @param id
     * @param constraint
     * @param hasMemory
     * @param objectSource
     */
    AlphaNode(int id,
              FieldConstraint constraint,
              ObjectSource objectSource) {
        super( id );
        this.constraint = constraint;
        this.objectSource = objectSource;
        setHasMemory( true );
    }

    /**
     * Retruns the <code>FieldConstraint</code>
     * 
     * @return <code>FieldConstraint</code>
     */
    public FieldConstraint getConstraint() {
        return this.constraint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.drools.reteoo.BaseNode#attach()
     */
    public void attach() {
        this.objectSource.addObjectSink( this );
    }

    public void assertObject(FactHandleImpl handle,
                             PropagationContext context,
                             WorkingMemoryImpl workingMemory) throws FactException {
        Set memory = (Set) workingMemory.getNodeMemory( this );
        if ( this.constraint.isAllowed( handle,
                                        null,
                                        workingMemory ) ) {
            memory.add( handle );
            propagateAssertObject( handle,
                                   context,
                                   workingMemory );
        }
    }

    public void retractObject(FactHandleImpl handle,
                              PropagationContext context,
                              WorkingMemoryImpl workingMemory) {
        Set memory = (Set) workingMemory.getNodeMemory( this );
        if ( memory.remove( handle ) ) {
            propagateRetractObject( handle,
                                    context,
                                    workingMemory );
        }
    }

    public void modifyObject(FactHandleImpl handle,
                             PropagationContext context,
                             WorkingMemoryImpl workingMemory) {
        Set memory = (Set) workingMemory.getNodeMemory( this );

        if ( this.constraint.isAllowed( handle,
                                        null,
                                        workingMemory ) ) {
            if ( memory.add( handle ) ) {
                propagateAssertObject( handle,
                                       context,
                                       workingMemory );
            } else {                
                // handle already existed so propagate as modify
                propagateModifyObject( handle,
                                       context,
                                       workingMemory );                             
            }
        } else {
            if ( memory.remove( handle ) ) {
                propagateRetractObject( handle,
                                        context,
                                        workingMemory );
            }
        }        
    }

    public void updateNewNode(WorkingMemoryImpl workingMemory,
                              PropagationContext context) {
        this.attachingNewNode = true;

        if ( hasMemory() ) {
            Set memory = (Set) workingMemory.getNodeMemory( this );

            for ( Iterator it = memory.iterator(); it.hasNext(); ) {
                FactHandleImpl handle = (FactHandleImpl) it.next();
                Object object = workingMemory.getObject( handle );
                propagateAssertObject( handle,
                                       context,
                                       workingMemory );
            }
        } else {
            // We need to detach and re-attach to make sure the node is at the
            // top
            // for the propagation
            this.objectSource.removeObjectSink( this );
            this.objectSource.addObjectSink( this );
            this.objectSource.updateNewNode( workingMemory,
                                             context );
        }

        this.attachingNewNode = false;
    }

    /**
     * Creates a HashSet for the AlphaNode's memory.
     */
    public Object createMemory() {
        return new HashSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || getClass() != object.getClass() ) {
            return false;
        }

        AlphaNode other = (AlphaNode) object;

        return this.objectSource.equals( other.objectSource ) && this.constraint.equals( other.constraint );
    }
    
    public int hashCode() {
        return this.objectSource.hashCode() * 17 +
               ((this.constraint != null) ? this.constraint.hashCode() : 0);
    }

    public void remove() {
        // TODO Auto-generated method stub

    }
}
