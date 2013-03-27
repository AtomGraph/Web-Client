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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.server.model.LinkedDataResource;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class GlobalResourceBase extends LocalResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GlobalResourceBase.class);

    private final MediaType mediaType;
    private final String topicUri;

    public GlobalResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("uri") String topicUri,
	    @QueryParam("accept") MediaType mediaType)
    {
	super(uriInfo, request, httpHeaders, resourceConfig, resourceContext,
		limit, offset, orderBy, desc);
	
	this.mediaType = mediaType;
	this.topicUri = topicUri;	
    }

    public String getTopicUri()
    {
	return topicUri;
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
    public Response getResponse()
    {
	if (getTopicUri() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getTopicUri());

	    Model model = DataManager.get().loadModel(getTopicUri());
	    
	    // use original Cache-Control? 
	    LinkedDataResource topic = new LinkedDataResourceBase(model.createResource(getTopicUri()),
		getCacheControl());
	    
	    addProperty(FOAF.primaryTopic, topic); // does this have any effect?

	    return getResponseBuilder(model, XHTML_VARIANTS).build();
	}	

	return super.getResponse();
    }

}
