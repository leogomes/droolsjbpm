/**
 * Copyright 2011 JBoss Inc
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

package org.drools.lang.api;

import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.EvalDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.ForallDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;

/**
 *  A descriptor builder for Conditional Elements
 *  
 *  rule.lhs()
 *    .and()
 *      .pattern("Foo").id("$foo").constraint("bar==baz").constraint("x>y").end()
 *      .not().pattern("Bar").constraint("a+b==c").end()
 *    .end()
 *  .rhs()
 *  .end()
 *     
 */
public interface CEDescrBuilder<P extends DescrBuilder<?>, T extends BaseDescr>
    extends
    DescrBuilder<T> {

    public CEDescrBuilder<CEDescrBuilder<P, T>, AndDescr> and();
    public CEDescrBuilder<CEDescrBuilder<P, T>, OrDescr> or();
    public CEDescrBuilder<CEDescrBuilder<P, T>, NotDescr> not();
    public CEDescrBuilder<CEDescrBuilder<P, T>, ExistsDescr> exists();
    public CEDescrBuilder<CEDescrBuilder<P, T>, ForallDescr> forall();
    public CEDescrBuilder<CEDescrBuilder<P, T>, EvalDescr> eval();
    
    public PatternDescrBuilder<CEDescrBuilder<P, T>> pattern( String type );
    
    public P end();
}
