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

    private String uri, endpointUri = null;
    private Query query = null;
    private Model model = null;
    
    public LinkedDataResourceImpl(String uri, String endpointUri)
    {
	this.uri = uri;
	if (endpointUri != null && endpointUri.isEmpty()) setEndpointURI(endpointUri);
	log.debug("URI: {} Endpoint URI: {}", getURI(), getEndpointURI());
	
	if (getURI() != null)
	{
	    setQuery(QueryFactory.create("DESCRIBE <" + getURI() + ">"));
	    log.debug("Query {} for URI {}", getQuery(), getURI());
	}
    }

    @Override
    public Model getModel()
    {
	if (model == null)
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

	return model;
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
