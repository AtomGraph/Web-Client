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
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
    private Model model = null;
    private ResultSet resultSet = null;
    private Response response = null;
    
    public SPARQLResource(@QueryParam("query") String queryString)
    {
	super();

	if (queryString != null)
	{
	    if (queryString.isEmpty()) throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    query = QueryFactory.create(queryString);
	    if (query.isUnknownType()) throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    log.debug("Submitted SPARQL query: {}", query);
	    
	    query.setLimit(MAX_LIMIT);

	    if (query.isConstructType() || query.isDescribeType())
	    {
		if (getEndpointURI() != null)
		    model = DataManager.get().loadModel(getEndpointURI(), query);
		else
		    model = DataManager.get().loadModel(getOntModel(), query);
		
		response = Response.ok(model).build();
	    }
	    if (query.isSelectType() || query.isAskType())
	    {
		if (getEndpointURI() != null)
		    resultSet = DataManager.get().loadResultSet(getEndpointURI(), query);
		else
		    resultSet = DataManager.get().loadResultSet(getOntModel(), query);
		
		response = Response.ok(resultSet).build();
	    }
	    
	    if (resultSet == response) response = super.getResponse();
	}
    }

    @Override
    public Query getQuery()
    {
	return query;
    }

    @Override
    public Response getResponse()
    {
	return response;
    }
    
    @Override
    public Model getModel()
    {
	return model;
    }

    @GET
    @Produces({org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML + "; charset=UTF-8", org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON + "; charset=UTF-8"})
    public ResultSet getResultSet()
    {
	return resultSet;
    }
    
}