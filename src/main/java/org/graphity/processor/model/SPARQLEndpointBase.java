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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.util.DataManager;
import org.graphity.server.vocabulary.GS;
import org.graphity.server.vocabulary.VoID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of SPARQL endpoints proxies.
 * Given a remote endpoint, it functions as a proxy and forwards SPARQL HTTP protocol requests to a remote SPARQL endpoint.
 * Otherwise, the endpoint serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol for RDF</a>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends org.graphity.server.model.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final UriInfo uriInfo;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * Uses <code>void:sparqlEndpoint</code> parameter value from web.xml as endpoint URI, if present.
     * Otherwise, uses <code>@Path</code> annotation value for this class (usually <code>/sparql</code> to
     * build local endpoint URI.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     * @param sitemap ontology of this webapp
     */
    public SPARQLEndpointBase(@Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig,
	    @Context OntModel sitemap)
    {
	this(resourceConfig.getProperty(VoID.sparqlEndpoint.getURI()) == null ?
	    sitemap.createResource(uriInfo.getBaseUriBuilder().
		path(SPARQLEndpointBase.class).
		build().toString()) :
		ResourceFactory.createResource(resourceConfig.getProperty(VoID.sparqlEndpoint.getURI()).toString()),
		uriInfo, request, resourceConfig);
    }

    /**
     * Protected constructor with explicit endpoint resource.
     * Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param endpoint RDF resource of this endpoint (must be URI resource, not a blank node)
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     */
    protected SPARQLEndpointBase(Resource endpoint, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
	super(endpoint, request, resourceConfig);

	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	this.uriInfo = uriInfo;
	
	if (endpoint.equals(getOntModelEndpoint(uriInfo)) && !DataManager.get().hasServiceContext(endpoint))
	{
	    if (log.isDebugEnabled()) log.debug("Adding service Context for local SPARQL endpoint with URI: {}", endpoint.getURI());
	    DataManager.get().addServiceContext(endpoint);
	}
    }

    /**
     * Builds URI resource of the local SPARQL endpoint (which is serving the sitemap ontology).
     * 
     * @return endpoint resource
     */
    public Resource getOntModelEndpoint()
    {
	return getOntModelEndpoint(getUriInfo());
    }

    /**
     * Builds local SPARQL endpoint resource from URI information of a request.
     * 
     * @param uriInfo URI information of the current request
     * @return resource
     */
    public final Resource getOntModelEndpoint(UriInfo uriInfo)
    {
	return ResourceFactory.createResource(uriInfo.
		getBaseUriBuilder().
		path(getClass()).
		build().toString());
    }
    
    /**
     * Loads RDF model by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param endpoint endpoint resource
     * @param query query object
     * @return loaded model
     */
    @Override
    public Model loadModel(Resource endpoint, Query query)
    {
	if (endpoint == null) throw new IllegalArgumentException("Endpoint cannot be null");
	if (!endpoint.isURIResource()) throw new IllegalArgumentException("Endpoint must be URI Resource (not a blank node)");

	if (endpoint.getURI().equals(getOntModelEndpoint().getURI()))
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from Model using Query: {}", query);
	    return DataManager.get().loadModel(getModel(), query);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from SPARQL endpoint: {} using Query: {}", endpoint, query);
	    return DataManager.get().loadModel(endpoint.getURI(), query);
	}
    }

    /**
     * Loads RDF model by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param endpoint endpoint resource
     * @param query query object
     * @return loaded model
     */
    @Override
    public ResultSetRewindable loadResultSetRewindable(Resource endpoint, Query query)
    {
	if (endpoint == null) throw new IllegalArgumentException("Endpoint cannot be null");
	if (!endpoint.isURIResource()) throw new IllegalArgumentException("Endpoint must be URI Resource (not a blank node)");

	if (endpoint.getURI().equals(getOntModelEndpoint().getURI()))
	{
	    if (log.isDebugEnabled()) log.debug("Loading ResultSet from Model using Query: {}", query);
	    return DataManager.get().loadResultSet(getModel(), query);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Loading ResultSet from SPARQL endpoint: {} using Query: {}", endpoint.getURI(), query);
	    return DataManager.get().loadResultSet(endpoint.getURI(), query);
	}
    }

    /**
     * Creates a response builder from SPARQL query and RDF model.
     * Contains the main SPARQL endpoint JAX-RS implementation logic.
     * Uses <code>gs:resultLimit</code> parameter value from web.xml as <code>LIMIT</code> value on <code>SELECT</code> queries, if present.
     * 
     * @param query query object
     * @param model RDF model
     * @return response builder
     */
    public ResponseBuilder getResponseBuilder(Query query, Model model)
    {
	if (query == null) throw new WebApplicationException(Response.Status.BAD_REQUEST);

	if (query.isSelectType())
	{
	    if (log.isDebugEnabled()) log.debug("SPARQL endpoint executing SELECT query: {}", query);
	    if (getResourceConfig().getProperty(GS.resultLimit.getURI()) != null)
		query.setLimit(Long.parseLong(getResourceConfig().
			getProperty(GS.resultLimit.getURI()).toString()));

	    if (log.isDebugEnabled()) log.debug("Loading ResultSet from Model: {} using Query: {}", model, query);
	    return getResponseBuilder(DataManager.get().loadResultSet(model, query));
	}

	if (query.isConstructType() || query.isDescribeType())
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from Model: {} using Query: {}", model, query);
	    return getResponseBuilder(DataManager.get().loadModel(model, query));
	}

	if (log.isWarnEnabled()) log.warn("SPARQL endpoint received unknown type of query: {}", query);
	throw new WebApplicationException(Response.Status.BAD_REQUEST);
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