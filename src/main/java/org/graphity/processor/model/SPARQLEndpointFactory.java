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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceContext;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.util.DataManager;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.model.SPARQLEndpointProxy;

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
     * @param request current request
     * @param servletContext webapp context
     * @param dataset dataset of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @return new endpoint
     */
    public static SPARQLEndpoint createEndpoint(UriInfo uriInfo, Request request, ServletContext servletContext,
            Dataset dataset, DataManager dataManager)
    {
	return new SPARQLEndpointBase(uriInfo, request, servletContext, dataset, dataManager);
    }

    /**
     * Creates new SPARQL endpoint from explicit URI resource, application configuration, and request data.
     * 
     * @param endpoint endpoint resource
     * @param request current request
     * @param servletContext webapp context
     * @param dataset dataset of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @return new endpoint
     */
    public static SPARQLEndpoint createEndpoint(Resource endpoint, Request request, ServletContext servletContext,
            Dataset dataset, DataManager dataManager)
    {
	return new SPARQLEndpointBase(endpoint, request, servletContext, dataset, dataManager);
    }

    public static SPARQLEndpointProxy createEndpointProxy(UriInfo uriInfo, Request request, ServletContext servletContext,
            DataManager dataManager, SPARQLEndpoint metaEndpoint, javax.ws.rs.core.Application application)
    {
	return new SPARQLEndpointProxyBase(uriInfo, request, servletContext, dataManager, metaEndpoint, application);
    }

    public static SPARQLEndpointProxy createEndpointProxy(Resource endpoint, Request request, ServletContext servletContext,
            DataManager dataManager, SPARQLEndpoint metaEndpoint, UriInfo uriInfo, javax.ws.rs.core.Application application)
    {
	return new SPARQLEndpointProxyBase(endpoint, request, servletContext, dataManager, metaEndpoint, uriInfo, application);
    }

}
