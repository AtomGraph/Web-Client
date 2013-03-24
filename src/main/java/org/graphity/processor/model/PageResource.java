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

import org.graphity.server.model.LinkedDataResource;

/**
 * Interface of page resources (with LIMIT, OFFSET, ORDER BY, DESC parameters)
 * 
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#solutionModifiers">SPARQL Solution Sequences and Modifiers</a>
 * @see <a href="http://www.w3.org/TR/2012/WD-ldp-20121025/#ldpc-paging">Linked Data Platform 1.0: Paging</a>
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public interface PageResource extends LinkedDataResource
{
    Long getLimit();
    
    Long getOffset();
    
    String getOrderBy();
    
    Boolean getDesc();

    /**
     * Get the resource of the previous page
     * 
     * @return Jena resource with previous page URI
     */
    com.hp.hpl.jena.rdf.model.Resource getPrevious();

    /**
     * Get the resource of the next page
     * 
     * @return Jena resource with next page URI
     */
    com.hp.hpl.jena.rdf.model.Resource getNext();

}