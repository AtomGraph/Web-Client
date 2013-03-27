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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.graphity.client.model.ResourceBase;
import org.graphity.client.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL endpoint resource, implementing ?query= access method
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends org.graphity.server.model.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final UriInfo uriInfo;

    public SPARQLEndpointBase(@Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig)
    {
	this(ResourceFactory.createResource(uriInfo.getAbsolutePath().toString()),
		uriInfo, request, resourceConfig);
    }

    protected SPARQLEndpointBase(Resource endpoint, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
	super(endpoint, request, resourceConfig);

	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	this.uriInfo = uriInfo;
	
	if (log.isDebugEnabled()) log.debug("Adding service Context for SPARQL endpoint with URI: {}", endpoint.getURI());
	DataManager.get().addServiceContext(endpoint.getURI());
    }

    /*
    @Override
    public Response getResponse()
    {
	MediaType mediaType = getHttpHeaders().getAcceptableMediaTypes().get(0);

	// don't create query resource if HTML is requested
	if (getUserQuery() != null && (mediaType.isCompatible(org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE) ||
	    mediaType.isCompatible(org.graphity.server.MediaType.TEXT_TURTLE_TYPE) ||
	    mediaType.isCompatible(org.graphity.server.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE) ||
	    mediaType.isCompatible(org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE)))
	{
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is RDF, returning SPARQL Response", mediaType);
	    return query(getUserQuery());
	}
	else
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is not RDF, returning default Response", mediaType);

	return super.getResponse(); // if HTML is requested, return DESCRIBE ?this results instead of user query
    }
    */
    
    @Override
    public Response query(Query query)
    {
	if (getEndpoint().equals(this))
	{
	    //if (log.isDebugEnabled()) log.debug("Generating Response from local Model with Query: {}", getUserQuery());
	    return getResponseBuilder(query, ResourceBase.getOntology(getUriInfo(), getResourceConfig())).build();
	}
	else
	{
	    //if (log.isDebugEnabled()) log.debug("Generating Response from SPARQL endpoint: {} with Query: {}", getEndpoint(), query);
	    return super.query(query);
	}
    }

    public ResponseBuilder getResponseBuilder(Query queryParam, Model model)
    {
	if (queryParam == null) throw new WebApplicationException(Response.Status.BAD_REQUEST);

	if (queryParam.isSelectType())
	{
	    if (log.isDebugEnabled()) log.debug("SPARQL endpoint executing SELECT query: {}", queryParam);
	    if (getResourceConfig().getProperty(org.graphity.server.model.SPARQLEndpointBase.PROPERTY_QUERY_RESULT_LIMIT) != null)
		queryParam.setLimit(Long.parseLong(getResourceConfig().
			getProperty(org.graphity.server.model.SPARQLEndpointBase.PROPERTY_QUERY_RESULT_LIMIT).toString()));

	    return getResponseBuilder(loadResultSetRewindable(model, queryParam));
	}

	if (queryParam.isConstructType() || queryParam.isDescribeType())
	{
	    if (log.isDebugEnabled()) log.debug("SPARQL endpoint executing CONSTRUCT/DESCRIBE query: {}", queryParam);
	    return getResponseBuilder(loadModel(model, queryParam));
	}

	if (log.isWarnEnabled()) log.warn("SPARQL endpoint received unknown type of query: {}", queryParam);
	throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    public ResultSetRewindable loadResultSetRewindable(Model model, Query query)
    {
	if (log.isDebugEnabled()) log.debug("Loading ResultSet from Model: {} using Query: {}", model, query);
	return DataManager.get().loadResultSet(model, query); // .getResultSetRewindable()
    }

    public Model loadModel(Model model, Query query)
    {
	if (log.isDebugEnabled()) log.debug("Loading Model from Model: {} using Query: {}", model, query);
	return DataManager.get().loadModel(model, query);
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

}