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
import org.graphity.model.SPARQLResource;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLResourceImpl implements SPARQLResource
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLResourceImpl.class);

    private String endpointUri = null;
    private Query query = null;
    private Model model = null;
    
    public SPARQLResourceImpl(String endpointUri, Query query)
    {
	setEndpointURI(endpointUri);
	setQuery(query);
	log.debug("Endpoint URI: {} Query: {}", getEndpointURI(), getQuery());
    }

    public SPARQLResourceImpl(String endpointUri, String uri)
    {
	this(endpointUri, QueryFactory.create("DESCRIBE <" + uri + ">"));
    }

    @Override
    public Model getModel()
    {
	if (model == null)
	{
	    log.debug("Querying remote service: {} with Query: {}", getEndpointURI(), getQuery());
	    model = DataManager.get().loadModel(getEndpointURI(), getQuery());

	    log.debug("Number of Model stmts read: {}", model.size());
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
	if (query == null || !(query.isConstructType() || query.isDescribeType())) throw new IllegalArgumentException("Query must be not null and CONSTRUCT or DESCRIBE");
	
	this.query = query;
    }
    
    @Override
    public final String getEndpointURI()
    {
	return endpointUri;
    }

    protected final void setEndpointURI(String endpointUri)
    {
	if (endpointUri == null) throw new IllegalArgumentException("Endpoint URI must be not null");

	this.endpointUri = endpointUri;
    }

}
