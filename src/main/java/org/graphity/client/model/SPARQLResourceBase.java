/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLResourceBase extends LocalResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLResourceBase.class);

    private final Query userQuery;

    public SPARQLResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("query") Query userQuery)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		getOntology(uriInfo, resourceConfig).createOntResource(uriInfo.getAbsolutePath().toString()),
		new SPARQLEndpointBase(ResourceFactory.createResource(uriInfo.getAbsolutePath().toString()),
		    uriInfo, request, resourceConfig),
		(resourceConfig.getProperty(PROPERTY_CACHE_CONTROL) == null) ? null : CacheControl.valueOf(resourceConfig.getProperty(PROPERTY_CACHE_CONTROL).toString()),
		limit, offset, orderBy, desc,
		userQuery);	
    }

    protected SPARQLResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpointBase endpoint,
	    CacheControl cacheControl, Long limit, Long offset, String orderBy, Boolean desc,
	    Query userQuery)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint, cacheControl,
		limit, offset, orderBy, desc);
	
	this.userQuery = userQuery;
	if (log.isDebugEnabled()) log.debug("Constructing SPARQLEndpointBase");
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
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is RDF, returning SPARQL Response", mediaType);
	    return getEndpoint().query(getUserQuery());
	}
	else
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is not RDF, returning default Response", mediaType);

	return super.getResponse(); // if HTML is requested, return DESCRIBE ?this results instead of user query
    }

    public Query getUserQuery()
    {
	return userQuery;
    }

}
