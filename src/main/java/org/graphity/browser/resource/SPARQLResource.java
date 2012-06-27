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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
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
    
    private @QueryParam("query") String queryString = null;
    private @QueryParam("accept") String accept = null;
    private @QueryParam("mode") String mode = null;
    
    @Override
    public Response getResponse()
    {
	if (mode != null && mode.equals("EmbedMode"))
	{
	    log.debug("?mode parameter: {}", mode);
	    
	    if (queryString == null || queryString.isEmpty())
		throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    
	    Query query = QueryFactory.create(queryString);
	    query.setLimit(MAX_LIMIT);

	    if (getEndpointURI() != null)
	    {
		// ModelEmbedWriter
		if (query.isDescribeType() || query.isConstructType())
		    return Response.ok(DataManager.get().loadModel(getEndpointURI(), query),
			    MediaType.TEXT_HTML).build();
		// ResultSetEmbedWriter
		if (query.isSelectType())
		    return Response.ok(DataManager.get().loadResultSet(getEndpointURI(), query), MediaType.TEXT_HTML).
			build();
	    }
	    else
	    {
		// ModelEmbedWriter
		if (query.isDescribeType() || query.isConstructType())
		    return Response.ok(DataManager.get().loadModel(getOntModel(), query),
			    MediaType.TEXT_HTML).build();
		// ResultSetEmbedWriter
		if (query.isSelectType())
		    return Response.ok(DataManager.get().loadResultSet(getOntModel(), query), MediaType.TEXT_HTML).
			build();		
	    }
	}
		
	return super.getResponse();
    }

    // queries are handled via EmbedMode from the client side
    /*
    @Override
    public Query getQuery()
    {
	if (queryString != null && !queryString.isEmpty())
	{
	    log.debug("?query parameter: {}", queryString);
	    Query query = QueryFactory.create(queryString);
	    if (query.isConstructType() || query.isDescribeType())
		return query;
	}
	
	return super.getQuery();
    }
    */

    @Override
    public String getEndpointURI()
    {
	/*
	if (queryString != null && !queryString.isEmpty())
	{
	    log.debug("?query parameter: {}", queryString);
	    Query query = QueryFactory.create(queryString);
	    if (query.isConstructType() || query.isDescribeType())
		return null;
	}
	*/
	if (mode == null || !mode.equals("EmbedMode"))
	    return null;

	return super.getEndpointURI();
    }

}
