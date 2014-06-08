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
import javax.servlet.ServletContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.util.DataManager;
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
     * @param sitemap ontology of this webapp
     * @param uriInfo URI information of the current request
     * @param dataManager RDF data manager for this graph store
     * @param request current request
     * @param servletContext webapp context
     * @param application webapp instance
     * @return graph store instance
     */
    public static GraphStore createGraphStore(OntModel sitemap, DataManager dataManager,
            UriInfo uriInfo, Request request, ServletContext servletContext, javax.ws.rs.core.Application application)
    {
	return new GraphStoreBase(sitemap, dataManager, uriInfo, request, servletContext, application);
    }

    /**
     * Creates new Graph Store from explicit URI resource, application configuration, and request data.
     * 
     * @param graphStore graph store resource
     * @param dataManager RDF data manager for this graph store
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param servletContext webapp context
     * @param application webapp instance
     * @return graph store instance
     */
    public static GraphStore createGraphStore(Resource graphStore, DataManager dataManager,
            UriInfo uriInfo, Request request, ServletContext servletContext, javax.ws.rs.core.Application application)
    {
	return new GraphStoreBase(graphStore, dataManager, uriInfo, request, servletContext, application);
    }

}
