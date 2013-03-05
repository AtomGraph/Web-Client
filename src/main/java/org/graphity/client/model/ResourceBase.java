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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.platform.model.LinkedDataResource;
import org.graphity.platform.model.LinkedDataResourceBase;
import org.graphity.platform.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of generic read-write Graphity Client resources
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("{path: .*}")
public class ResourceBase extends org.graphity.platform.model.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    public static List<Variant> XHTML_VARIANTS = Variant.VariantListBuilder.newInstance().
		mediaTypes(MediaType.APPLICATION_XHTML_XML_TYPE,
			org.graphity.platform.MediaType.APPLICATION_RDF_XML_TYPE,
			org.graphity.platform.MediaType.TEXT_TURTLE_TYPE).
		languages(Locale.ENGLISH).
		add().build();

    private final MediaType mediaType;
    private final String topicUri;

    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig config,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("uri") String topicUri,
	    @QueryParam("accept") MediaType mediaType)
    {
	super(getOntology(uriInfo, config),
		uriInfo, request, httpHeaders, XHTML_VARIANTS,
		(config.getProperty(PROPERTY_CACHE_CONTROL) == null) ? null : CacheControl.valueOf(config.getProperty(PROPERTY_CACHE_CONTROL).toString()),
		limit, offset, orderBy, desc);
	
	this.mediaType = mediaType;
	this.topicUri = topicUri;
    }
    
    @Override
    public Response getResponse()
    {
	if (getTopicUri() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getTopicUri());

	    OntModel ontModel = ModelFactory.createOntologyModel();
	    ontModel.add(DataManager.get().loadModel(getTopicUri()));
	    
	    // use original Cache-Control? 
	    //LinkedDataResourceBase topic = new LinkedDataResourceBase(ontModel.createOntResource(getTopicUri()),
	    //	    getUriInfo(), getRequest(), getHttpHeaders(), getVariants(), getCacheControl());
	    
	    addProperty(FOAF.primaryTopic, ontModel.createOntResource(getTopicUri()));

	    return getResponse(ontModel);
	}	

	return super.getResponse();
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

    public String getTopicUri()
    {
	return topicUri;
    }

}