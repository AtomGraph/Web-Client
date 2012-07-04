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

package org.graphity.model.impl;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.MediaType;
import org.graphity.model.LinkedDataResource;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class LinkedDataResourceImpl implements LinkedDataResource
{
    private static final Logger log = LoggerFactory.getLogger(LinkedDataResourceImpl.class);

    @Context private UriInfo uriInfo = null;
    private @QueryParam("uri") String uri = null;
    private @QueryParam("service-uri") String serviceUri = null;
    private @QueryParam("accept") String accept = null;

    @GET
    @Produces(MediaType.APPLICATION_XHTML_XML + "; charset=UTF-8")
    public Response getResponse()
    {
	if (accept != null)
	{
	    if (accept.equals(MediaType.APPLICATION_RDF_XML))
		return Response.ok(getModel(), MediaType.APPLICATION_RDF_XML_TYPE).
		    build();
	    if (accept.equals(MediaType.TEXT_TURTLE))
		return Response.ok(getModel(), MediaType.TEXT_TURTLE_TYPE).
		    build();
	}
	    
	return Response.ok(this).
	    build();
    }

    @GET
    @Produces({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
    @Override
    public Model getModel()
    {
	Model model = null;

	try
	{
	    if (getEndpointURI() != null) // in case we have an endpoint, first try loading using SPARQL
	    {
		log.debug("Querying remote service: {} with Query: {}", getEndpointURI(), getQuery());
		model = DataManager.get().loadModel(getEndpointURI(), getQuery());
	    }
	    if (model == null || model.isEmpty()) // otherwise (no endpoint or no result) load directly from URI (Linked Data)
	    {
		log.debug("Loading Model from URI: {}", getURI());
		model = DataManager.get().loadModel(getURI());
	    }

	    log.debug("Number of Model stmts read: {}", model.size());
	}
	catch (Exception ex)
	{
	    log.trace("Could not load Model from URI: {}", getURI(), ex);
	    throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
	}
	
	return model;
    }

    @Override
    public Query getQuery()
    {
	log.debug("Default query {} for URI {}", "DESCRIBE <" + getURI() + ">", getURI());
	return QueryFactory.create("DESCRIBE <" + getURI() + ">");
    }

    @Override
    public String getURI()
    {
	if (uri != null && !uri.isEmpty()) return uri;

	return uriInfo.getAbsolutePath().toString();
    }
    
    @Override
    public String getEndpointURI()
    {
	if (serviceUri == null || serviceUri.isEmpty()) return null;
	
	return serviceUri;
    }

}
