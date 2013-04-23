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

import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Client subclass of Server read-only Linked Data resource.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LinkedDataResourceBase extends org.graphity.server.model.LinkedDataResourceBase
{

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * The URI of the resource being created is the absolute path of the current request URI.
     * 
     * @param uriInfo URI information of this request
     * @param resourceConfig webapp configuration
     */
    public LinkedDataResourceBase(@Context UriInfo uriInfo, @Context ResourceConfig resourceConfig)
    {
	super(uriInfo, resourceConfig);
    }

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param resource This resource as RDF resource (must be URI resource, not a blank node)
     * @param cacheControl cache control config
     */
    protected LinkedDataResourceBase(Resource resource, CacheControl cacheControl)
    {
	super(resource, cacheControl);
    }

}
