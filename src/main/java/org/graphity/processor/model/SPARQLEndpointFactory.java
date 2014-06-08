/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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
import org.graphity.server.model.SPARQLEndpoint;

/**
 * A factory class for creating SPARQL endpoints.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLEndpointFactory extends org.graphity.server.model.SPARQLEndpointFactory
{
    /**
     * Creates new SPARQL endpoint from application configuration, request data, and sitemap ontology.
     * 
     * @param uriInfo URI information of the current request
     * @param sitemap ontology of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @param request current request
     * @param servletContext webapp context
     * @param application webapp instance
     * @return new endpoint
     */
    public static SPARQLEndpoint createEndpoint(OntModel sitemap, DataManager dataManager,
            UriInfo uriInfo, Request request, ServletContext servletContext, javax.ws.rs.core.Application application)
    {
	return new SPARQLEndpointBase(sitemap, dataManager, uriInfo, request, servletContext, application);
    }

    /**
     * Creates new SPARQL endpoint from explicit URI resource, application configuration, and request data.
     * 
     * @param endpoint endpoint resource
     * @param dataManager RDF data manager for this endpoint
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param servletContext webapp context
     * @param application webapp
     * @return new endpoint
     */
    public static SPARQLEndpoint createEndpoint(Resource endpoint, DataManager dataManager,
            UriInfo uriInfo, Request request, ServletContext servletContext, javax.ws.rs.core.Application application)
    {
	return new SPARQLEndpointBase(endpoint, dataManager, uriInfo, request, servletContext, application);
    }

}
