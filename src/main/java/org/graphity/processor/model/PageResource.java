/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.processor.model;

import com.hp.hpl.jena.rdf.model.Resource;
import org.graphity.server.model.LinkedDataResource;

/**
 * Interface of page resources. A page is a unit pagination, and only applies to container resources.
 * SPARQL-based solution modifiers are used to implement pagination.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 * @see <a href="http://www.w3.org/TR/2012/WD-ldp-20121025/#ldpc-paging">Linked Data Platform 1.0: Paging</a>
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public interface PageResource extends LinkedDataResource
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
     * {@link getOrderBy()}. Otherwise, no order modifier is set (which equals to <code>ASC</code>).
     * 
     * @return true if the order is descending, false otherwise
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    Boolean getDesc();

    /**
     * Get the RDF resource of the previous page. Can be used for HATEOS link relations.
     * 
     * @return previous page resource
     */
    Resource getPrevious();

    /**
     * Get the RDF resource of the next page. Can be used for HATEOS link relations.
     * 
     * @return next page resource
     */
    Resource getNext();

}