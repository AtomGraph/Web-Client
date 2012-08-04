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
package org.graphity.ldp.model.impl;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import org.graphity.model.ResourceFactory;
import org.graphity.model.SPARQLResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class SPARQLResourceBase extends ResourceBase implements SPARQLResource
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLResourceBase.class);

    private String endpointUri = null;
    private Query query = null;
    private Model model = null;

    public SPARQLResourceBase(String endpointUri, Query query)
    {
	this.endpointUri = endpointUri;
	this.query = query;
	log.debug("Endpoint URI: {} Query: {}", endpointUri, query);
    }

    public SPARQLResourceBase(String endpointUri, String uri)
    {
	this(endpointUri, QueryFactory.create("DESCRIBE <" + uri + ">"));
    }

    @Override
    public Model getModel()
    {
	if (model == null) model = ResourceFactory.createSPARQLResource(endpointUri, query).getModel();

	return model;
    }

    @Override
    public String getEndpointURI()
    {
	return endpointUri;
    }

    @Override
    public Query getQuery()
    {
	return query;
    }
    
}
