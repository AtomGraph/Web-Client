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
package org.graphity.client.model;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.client.util.DataManager;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL endpoint resource, implementing ?query= access method
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final Query userQuery;

    public SPARQLEndpointBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	@Context ResourceConfig config,
	@QueryParam("limit") @DefaultValue("20") Long limit,
	@QueryParam("offset") @DefaultValue("0") Long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") Boolean desc,
	@QueryParam("accept") javax.ws.rs.core.MediaType mediaType,
	@QueryParam("query") Query userQuery)
    {
	super(uriInfo, request, httpHeaders, config,
		limit, offset, orderBy, desc,
		null, mediaType);	

	this.userQuery = userQuery;

	if (log.isDebugEnabled()) log.debug("Adding service Context for SPARQL endpoint with URI: {}", uriInfo.getAbsolutePath().toString());
	DataManager.get().addServiceContext(uriInfo.getAbsolutePath().toString());
    }

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
	    SPARQLEndpoint sparql = new org.graphity.server.model.SPARQLEndpointBase(getUriInfo(), getRequest(), getHttpHeaders(), getResourceConfig(), getQuery());
	    return sparql.query(getUserQuery());
	}

	return super.getResponse(); // if HTML is requested, return DESCRIBE ?this results instead of user query
    }

    @Override
    public Model describe()
    {
	return loadModel(getOntModel(), getQuery(getUriInfo().getAbsolutePath().toString()));
    }
    
    public Query getUserQuery()
    {
	return userQuery;
    }

}