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
import javax.naming.ConfigurationException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of Graph Store proxies.
 * Given a remote store, it functions as a proxy and forwards SPARQL Graph Store protocol requests to a remote graph store.
 * Otherwise, the store serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a>
 */
@Path("/service") // not standard
public class GraphStoreBase extends org.graphity.server.model.GraphStoreBase

{    private static final Logger log = LoggerFactory.getLogger(GraphStoreBase.class);

    private final UriInfo uriInfo;
    
    public GraphStoreBase(@Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig,
            @Context OntModel sitemap)
    {
        this(sitemap.createResource(uriInfo.getBaseUriBuilder().
                path(GraphStoreBase.class).
                build().
                toString()),
            uriInfo, request, resourceConfig);
    }

    public GraphStoreBase(Resource graphStore, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
        super(graphStore, request, resourceConfig);
        
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	this.uriInfo = uriInfo;
    }

    /**
     * Returns configured SPARQL service resource.
     * Uses <code>gp:service</code> parameter value from sitemap resource with application base URI.
     * 
     * @return service resource
     */
    public Resource getService()
    {
        return getModel().createResource(getUriInfo().getBaseUri().toString()).
                getPropertyResourceValue(GP.service);
    }

     /**
     * Returns configured Graph Store resource.
     * 
     * @return graph store resource
     */
    @Override
    public Resource getRemoteStore()
    {
        try
        {
            if (getService() == null) throw new ConfigurationException("SPARQL service not configured (gp:service not set in sitemap ontology)");
            Resource graphStore = getGraphStore(getService());
            if (graphStore == null) throw new ConfigurationException("Configured SPARQL service (gp:service in sitemap ontology) does not have a Graph Store (gp:graphStore)");
            
            // configure Context
            
            return graphStore;
        }
        catch (ConfigurationException ex)
        {
            if (log.isErrorEnabled()) log.warn("Graph Store configuration error", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);            
        }
    }
       
    /**
     * Returns Graph Store resource for the supplied SPARQL service.
     * Uses <code>gp:graphStore</code> parameter value from current SPARQL service resource.
     * 
     * @param service SPARQL service resource
     * @return service resource
     */
    public Resource getGraphStore(Resource service)
    {
        if (service == null) throw new IllegalArgumentException("SPARQL service resource cannot be null");
        return service.getPropertyResourceValue(GP.graphStore);
    }

    /**
     * Returns URI information of the current request.
     * 
     * @return URI information
     */
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }
    
}