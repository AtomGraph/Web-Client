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
import javax.ws.rs.core.Response;
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

    private String uri, endpointUri, accept = null;
    private Query query = null;
    private Model model = null;
    
    public LinkedDataResourceImpl(
	@QueryParam("uri") String uri,
	@QueryParam("endpoint-uri") String endpointUri,
	@QueryParam("accept") String accept)
    {
	setURI(uri);
	setEndpointURI(endpointUri);
	log.debug("URI: {} Endpoint URI: {}", getURI(), getEndpointURI());
		
	this.accept = accept;
	
	if (getURI() != null)
	{
	    setQuery(QueryFactory.create("DESCRIBE <" + getURI() + ">"));
	    log.debug("Query {} for URI {}", getQuery(), getURI());
	}
    }

    @GET
    @Produces(MediaType.APPLICATION_XHTML_XML + "; charset=UTF-8")
    public Response getResponse()
    {
	if (accept != null)
	{
	    if (accept.equals(MediaType.APPLICATION_RDF_XML))
		return Response.ok(model, MediaType.APPLICATION_RDF_XML_TYPE).build();
	    if (accept.equals(MediaType.TEXT_TURTLE))
		return Response.ok(model, MediaType.TEXT_TURTLE_TYPE).build();
	}

	return Response.ok(this).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
    @Override
    public Model getModel()
    {
	if (getURI() == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	
	if (model == null)
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

		log.debug("Number of Model stmts read: {}", getModel().size());
	    }
	    catch (Exception ex)
	    {
		log.trace("Could not load Model from URI: {}", getURI(), ex);
		throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
	    }

	return model;
    }

    protected final void setModel(Model model)
    {
	this.model = model;
    }

    @Override
    public final Query getQuery()
    {
	return query;
    }

    protected final void setQuery(Query query)
    {
	this.query = query;
    }

    @Override
    public final String getURI()
    {
	return uri;
    }

    protected final void setURI(String uri)
    {
	this.uri = uri;
    }
    
    @Override
    public final String getEndpointURI()
    {
	return endpointUri;
    }

    protected final void setEndpointURI(String endpointUri)
    {
	this.endpointUri = endpointUri;
    }

}
