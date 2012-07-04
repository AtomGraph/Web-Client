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

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
    
    private ResultSet resultSet = null;
    private Response response = null;
    
    public SPARQLResource(@Context UriInfo uriInfo,
	@QueryParam("uri") String uri,
	@QueryParam("service-uri") String serviceUri,
	@QueryParam("accept") String accept,
	@QueryParam("limit") @DefaultValue("10") long limit,
	@QueryParam("offset") @DefaultValue("0") long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") @DefaultValue("true") boolean desc,
	@QueryParam("query") String queryString)
    {
	super(uriInfo, uri, serviceUri, accept, limit, offset, orderBy, desc);

	if (queryString != null)
	{
	    if (queryString.isEmpty()) throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    setQuery(QueryFactory.create(queryString));
	    if (getQuery().isUnknownType()) throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    log.debug("Submitted SPARQL query: {}", getQuery());
	    
	    getQuery().setLimit(MAX_LIMIT);

	    if (getQuery().isConstructType() || getQuery().isDescribeType())
	    {
		if (getEndpointURI() != null)
		    setModel(DataManager.get().loadModel(getEndpointURI(), getQuery()));
		else
		    setModel(DataManager.get().loadModel(getOntModel(), getQuery()));
		
		response = Response.ok(getModel()).build();
	    }
	    if (getQuery().isSelectType() || getQuery().isAskType())
	    {
		if (getEndpointURI() != null)
		    resultSet = DataManager.get().loadResultSet(getEndpointURI(), getQuery());
		else
		    resultSet = DataManager.get().loadResultSet(getOntModel(), getQuery());
		
		response = Response.ok(resultSet).build();
	    }
	}
    }

    @Override
    public Response getResponse()
    {
	return response;
    }

    @GET
    @Produces({org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML + "; charset=UTF-8", org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON + "; charset=UTF-8"})
    public ResultSet getResultSet()
    {
	return resultSet;
    }
    
}