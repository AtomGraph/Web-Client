/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.processor.model;

import org.graphity.server.model.LinkedDataResource;

/**
 * Interface of container resources. Pages (pagination units) only applies to container resources.
 * SPARQL-based solution modifiers are used to implement pagination.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 * @see <a href="http://www.w3.org/TR/2012/WD-ldp-20121025/#ldpc-paging">Linked Data Platform 1.0: Paging</a>
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public interface ContainerResource extends LinkedDataResource
{
    /**
     * Returns the value of <code>LIMIT</code> query modifier.
     * It indicates the number of resources per page.
     * 
     * @return resources per page
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modResultLimit">15.5 LIMIT</a>
     */
    Long getLimit();
    
    /**
     * Returns the value of <code>OFFSET</code> query modifier.
     * It indicates the number of resources skipped before the current page.
     * 
     * @return number of resources skipped
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOffset">15.4 OFFSET</a>
     */
    Long getOffset();
    
    /**
     * Returns the name of the variable used by the <code>ORDER BY</code> ordering comparator.
     * It indicates the ordering of resources in a container and on a page.
     * 
     * <em>While SPARQL allows a sequence of multiple variables used as comparators, the processor currently
     * supports only one value.</em>
     * 
     * @return variable name or null, if not specified
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    String getOrderBy();
    
    /**
     * Indicates whether the direction of the ordering is <code>DESC</code>.
     * If this method returns true, <code>DESC</code> order modifier is set on the variable indicated by
     * {@link #getOrderBy()}. Otherwise, no order modifier is set (which equals to <code>ASC</code>).
     * 
     * @return true if the order is descending, false otherwise
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    Boolean getDesc();

}