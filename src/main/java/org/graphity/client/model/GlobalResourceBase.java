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
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.sun.jersey.api.core.ResourceConfig;
import java.net.URI;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.processor.model.SPARQLEndpointBase;
import org.graphity.server.model.LinkedDataResource;
import org.graphity.server.util.DataManager;
import org.graphity.server.vocabulary.GS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("{path: .*}")
public class GlobalResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GlobalResourceBase.class);

    private final MediaType mediaType;
    private final URI topicURI;

    public GlobalResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("uri") URI topicURI,
	    @QueryParam("accept") MediaType mediaType)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		getOntology(uriInfo, resourceConfig),
		getEndpoint(uriInfo, request, resourceConfig),
		(resourceConfig.getProperty(GS.cacheControl.getURI()) == null) ?
		    null :
		    CacheControl.valueOf(resourceConfig.getProperty(GS.cacheControl.getURI()).toString()),
		limit, offset, orderBy, desc,
		XHTML_VARIANTS,
		topicURI, mediaType);	
    }

    protected GlobalResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntModel ontModel, SPARQLEndpointBase endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, List<Variant> variants,
	    URI topicURI, MediaType mediaType)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		ontModel.createOntResource(uriInfo.getRequestUri().toString()), endpoint, cacheControl,
		limit, offset, orderBy, desc, variants,
		topicURI, mediaType);
    }

    protected GlobalResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpointBase endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, List<Variant> variants,
	    URI topicURI, MediaType mediaType)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint, cacheControl, limit, offset, orderBy, desc, variants);
	
	this.mediaType = mediaType;
	this.topicURI = topicURI;
    }

    public URI getTopicURI()
    {
	return topicURI;
    }

    @Override
    public List<Variant> getVariants()
    {
	if (getMediaType() != null)
	    return Variant.VariantListBuilder.newInstance().
		    mediaTypes(getMediaType()).
		    add().build();

	return super.getVariants();
    }

    public MediaType getMediaType()
    {
	return mediaType;
    }

    @Override
    public Response get()
    {
	if (getTopicURI() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getTopicURI());

	    Model model = DataManager.get().loadModel(getTopicURI().toString());
	    
	    // use original Cache-Control? 
	    LinkedDataResource topic = new LinkedDataResourceBase(model.createResource(getTopicURI().toString()),
		getCacheControl());
	    
	    addProperty(FOAF.primaryTopic, topic); // does this have any effect?

	    return getResponseBuilder(model, getVariants()).build();
	}	

	return super.get();
    }

}
