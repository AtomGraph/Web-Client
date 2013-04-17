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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends org.graphity.processor.model.SPARQLEndpointBase
{

    public SPARQLEndpointBase(@Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig,
	    @Context OntModel sitemap)
    {
	super(uriInfo, request, resourceConfig, sitemap);
    }

    public SPARQLEndpointBase(Resource endpoint, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
	super(endpoint, uriInfo, request, resourceConfig);
    }
    
}
