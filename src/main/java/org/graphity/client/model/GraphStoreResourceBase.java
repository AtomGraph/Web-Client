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
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.server.model.GraphStore;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.vocabulary.GS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/graphs")
public class GraphStoreResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GraphStoreResourceBase.class);

    @Context GraphStore store;

    public GraphStoreResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @Context OntModel sitemap,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		sitemap.createOntResource(uriInfo.getAbsolutePath().toString()),
		SPARQLEndpointFactory.createEndpoint(sitemap.createResource(uriInfo.getAbsolutePath().toString()),
		    uriInfo, request, resourceConfig),
		resourceConfig.getProperty(GS.cacheControl.getURI()) == null ?
		    null :
		    CacheControl.valueOf(resourceConfig.getProperty(GS.cacheControl.getURI()).toString()),
		limit, offset, orderBy, desc, graphURI, mode,
		XHTML_VARIANTS);
    }

    protected GraphStoreResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI, URI mode,
	    List<Variant> variants)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint, cacheControl,
		limit, offset, orderBy, desc, graphURI, mode,
		variants);
    }
    
    @Override
    public Response post(Model model)
    {
	return super.post(model);
    }
    
    @Override
    public Response put(Model model)
    {
	return super.put(model);
    }
    
}