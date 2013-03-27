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
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
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

    public Resource getOntModelEndpoint()
    {
	return ResourceFactory.createResource(getUriInfo().
		getBaseUriBuilder().
		path(SPARQLEndpointBase.class).
		build().toString());
    }
    
    @Override
    public Model loadModel(Resource endpoint, Query query)
    {
	if (endpoint.equals(getOntModelEndpoint()))
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from OntModel using Query: {}", query);
	    OntModel ontModel = ResourceBase.getOntology(getUriInfo(), getResourceConfig());
	    return DataManager.get().loadModel(ontModel, query);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from SPARQL endpoint: {} using Query: {}", endpoint, query);
	    return org.graphity.server.util.DataManager.get().loadModel(endpoint.getURI(), query);
	}
    }

    @Override
    public ResultSetRewindable loadResultSetRewindable(Resource endpoint, Query query)
    {
	if (endpoint.equals(this))
	{
	    OntModel ontModel = ResourceBase.getOntology(getUriInfo(), getResourceConfig());
	    return DataManager.get().loadResultSet(ontModel, query);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Loading ResultSet from SPARQL endpoint: {} using Query: {}", endpoint.getURI(), query);
	    return DataManager.get().loadResultSet(endpoint.getURI(), query);
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