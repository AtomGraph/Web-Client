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
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPARQL endpoint resource, implementing ?query= access method
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends org.graphity.platform.model.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    public SPARQLEndpointBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	@Context ResourceConfig config,
	@QueryParam("accept") javax.ws.rs.core.MediaType mediaType,
	@QueryParam("limit") @DefaultValue("20") Long limit,
	@QueryParam("offset") @DefaultValue("0") Long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") Boolean desc,
	//@QueryParam("query") Query query,
	@QueryParam("uri") String uri)
    {
	super(uriInfo, request, httpHeaders, config,
		limit, offset, orderBy, desc,
		null);	
    }

}