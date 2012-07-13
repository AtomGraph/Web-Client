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
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.core.HttpRequestContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.MediaType;
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
    
    //private @Context HttpRequestContext request = null;
    private Query query = null;
    private Model resultModel = null;
    private ResultSet resultSet = null;
    private String accept, endpointUri = null;

    public SPARQLResource(@Context UriInfo uriInfo,
	@QueryParam("endpoint-uri") String endpointUri,
	//@QueryParam("accept") String accept,
	@QueryParam("limit") @DefaultValue("10") long limit,
	@QueryParam("offset") @DefaultValue("0") long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") @DefaultValue("true") boolean desc,
	@QueryParam("query") Query query
	    )
    {
	super(uriInfo, null, null, limit, offset, orderBy, desc);
	//this.accept = accept;
	this.query = query;
	this.query.setLimit(MAX_LIMIT);
	this.endpointUri = endpointUri;
	
	/*
	if (queryString != null)
	{
	    if (queryString == null || queryString.isEmpty())
		throw new WebApplicationException(Response.Status.BAD_REQUEST);

	    query = QueryFactory.create(queryString);
	    if (query.isUnknownType()) throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    log.debug("Submitted SPARQL query: {}", query);
	    
	    query.setLimit(MAX_LIMIT);
	}
	*/ 
    }

    @GET
    @Produces({org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML + "; charset=UTF-8", org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON + "; charset=UTF-8"})
    public ResultSet getResultSet()
    {
	log.debug("Submitted SPARQL query: {}", query);
	if (query != null && (query.isSelectType() || query.isAskType()))
	{
	    if (endpointUri != null)
		return DataManager.get().loadResultSet(endpointUri, query);
	    else
		return DataManager.get().loadResultSet(getOntModel(), query);
	}

	return resultSet;
    }

    //@GET
    //@Produces({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
    //public Model getResultModel(@QueryParam("query") String queryString)
    @Override
    public Model getXXX()
    {
	if (query != null && (query.isConstructType() || query.isDescribeType()))
	{
	    if (endpointUri != null)
		//setModel(DataManager.get().loadModel(getEndpointURI(), getQuery()));
		resultModel = DataManager.get().loadModel(endpointUri, query);
	    else
		//setModel(DataManager.get().loadModel(getOntModel(), getQuery()));
		resultModel = DataManager.get().loadModel(getOntModel(), query);
	}
	    
	return resultModel;
    }

    /*
    @Override
    public Model getModel()
    {
	//if (getResultModel() != null) return getResultModel();
	
	return super.getModel();
    }
    */
    
    @Override
    //public Response getResponse(@QueryParam("accept") String accept)
    public Response getResponse()
    {
	//setModel(super.getModel());

	/*
	if (accept != null && getResultSet() != null)
	{
	    log.debug("Accept param: {}, writing SPARQL XML results (JSON or Turtle)", accept);

	    if (accept.equals(MediaType.APPLICATION_SPARQL_RESULTS_XML))
		return Response.ok(getResultSet(), MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
	    if (accept.equals(MediaType.APPLICATION_SPARQL_RESULTS_JSON))
		return Response.ok(getResultSet(), MediaType.APPLICATION_SPARQL_RESULTS_JSON_TYPE).build();
	}
	*/
	
	//return super.getResponse(accept);
	return super.getResponse();
    }

}