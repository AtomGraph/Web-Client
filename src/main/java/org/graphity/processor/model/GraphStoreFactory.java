/*
 * Copyright (C) 2013 Martynas
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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.graphity.server.model.GraphStore;

/**
 *
 * @author Martynas
 */
public class GraphStoreFactory extends org.graphity.server.model.GraphStoreFactory
{
    /**
     * Creates new Graph Store from application configuration, request data, and sitemap ontology.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     * @param sitemap ontology of this webapp
     * @return graph store instance
     */
    public static GraphStore createGraphStore(UriInfo uriInfo, Request request, ResourceConfig resourceConfig,
	    OntModel sitemap)
    {
	return new GraphStoreBase(uriInfo, request, resourceConfig, sitemap);
    }

    /**
     * Creates new Graph Store from explicit URI resource, application configuration, and request data.
     * 
     * @param graphStore graph store resource
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     * @return graph store instance
     */
    public static GraphStore createGraphStore(Resource graphStore, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
	return new GraphStoreBase(graphStore, uriInfo, request, resourceConfig);
    }

}
