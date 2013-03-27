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

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLEndpointBase extends ResourceBase
{

    public SPARQLEndpointBase(UriInfo uriInfo, Request request, ResourceConfig resourceConfig, ResourceContext resourceContext, Long limit, Long offset, String orderBy, Boolean desc, String topicUri, MediaType mediaType)
    {
	super(uriInfo, request, resourceConfig, resourceContext, limit, offset, orderBy, desc, topicUri, mediaType);
    }

    
}
