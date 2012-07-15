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
package org.graphity.browser.resource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.browser.Resource;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLResource extends Resource
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLResource.class);
    private static final long MAX_LIMIT = 100;
    
    private Query query = null;
    private Model resultModel = null;
    private ResultSet resultSet = null;
    private String endpointUri = null;

    public SPARQLResource(@Context UriInfo uriInfo,
	@QueryParam("endpoint-uri") String endpointUri,
	@QueryParam("accept") MediaType acceptType,
	@QueryParam("limit") @DefaultValue("10") long limit,
	@QueryParam("offset") @DefaultValue("0") long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") @DefaultValue("true") boolean desc,
	@QueryParam("query") Query query)
    {
	super(uriInfo, null, null, acceptType, limit, offset, orderBy, desc);
	log.debug("Accept type: {}", acceptType);
	this.query = query;
	if (query != null)
	{
	    this.query.setLimit(MAX_LIMIT);
	    log.debug("Submitted SPARQL query: {}", query);
	}
	if (endpointUri != null && !endpointUri.isEmpty()) this.endpointUri = endpointUri;
    }

    @GET
    @Produces({org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML + "; charset=UTF-8", org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON + "; charset=UTF-8"})
    public ResultSet getResultSet()
    {
	if (resultSet == null && query != null && (query.isSelectType() || query.isAskType()))
	{
	    if (endpointUri != null)
		resultSet = DataManager.get().loadResultSet(endpointUri, query);
	    else
		resultSet = DataManager.get().loadResultSet(getOntModel(), query);
	}
	
	return resultSet;
    }

    //@GET
    //@Produces({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
    @Override
    public Model getResponseModel()
    {
	if (resultModel == null && query != null && (query.isConstructType() || query.isDescribeType()))
	{
	    if (endpointUri != null)
		resultModel = DataManager.get().loadModel(endpointUri, query);
	    else
		resultModel = DataManager.get().loadModel(getOntModel(), query);
	}
	    
	return resultModel;
    }
    
    @Override
    public Response getResponse()
    {
	if (getAcceptType() != null && getResultSet() != null)
	{
	    log.debug("Accept param: {}, writing SPARQL results (XML or JSON)", getAcceptType());

	    if (getAcceptType().equals(org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE))
		return Response.ok(getResultSet(), org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
	    if (getAcceptType().equals(org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON_TYPE))
		return Response.ok(getResultSet(), org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON_TYPE).build();
	}
	if (getAcceptType() != null && getResponseModel() != null)
	{
	    log.debug("Accept param: {}, writing RDF/XML or Turtle", getAcceptType());

	    if (getAcceptType().equals(org.graphity.MediaType.APPLICATION_RDF_XML_TYPE))
		return Response.ok(getResponseModel(), org.graphity.MediaType.APPLICATION_RDF_XML_TYPE).build();
	    if (getAcceptType().equals(org.graphity.MediaType.TEXT_TURTLE_TYPE))
		return Response.ok(getResponseModel(), org.graphity.MediaType.TEXT_TURTLE_TYPE).build();
	}

	return super.getResponse();
    }

}