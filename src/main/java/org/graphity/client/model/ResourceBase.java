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
import com.hp.hpl.jena.update.UpdateRequest;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.server.model.LinkedDataResource;
import org.graphity.processor.update.ModifyBuilder;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Base class of generic read-write Graphity Client resources
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("{path: .*}")
public class ResourceBase extends org.graphity.processor.model.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    public static List<Variant> XHTML_VARIANTS = Variant.VariantListBuilder.newInstance().
		mediaTypes(MediaType.APPLICATION_XHTML_XML_TYPE,
		    //MediaType.TEXT_HTML_TYPE,
		    org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE,
		    org.graphity.server.MediaType.TEXT_TURTLE_TYPE).
		add().build();

    private final MediaType mediaType;
    private final String topicUri;

    public ResourceBase(@Context UriInfo uriInfo, @Context Request request,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("uri") String topicUri,
	    @QueryParam("accept") MediaType mediaType)
    {
	super(uriInfo, request, resourceConfig, resourceContext,
		limit, offset, orderBy, desc);
	
	this.mediaType = mediaType;
	this.topicUri = topicUri;
    }

    @Override
    public Model describe()
    {
	Model description = super.describe();

	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addRDFType(getMatchedOntClass());
	
	// set metadata properties after description query is executed
	if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, getQueryBuilder());
	setPropertyValue(SPIN.query, getQueryBuilder());

	return description;
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

    @Override
    public Response.ResponseBuilder getResponseBuilder(Model model)
    {
	return getEndpoint().getResponseBuilder(model, getVariants()).
		cacheControl(getCacheControl());
    }

    public List<Variant> getVariants()
    {
	if (getMediaType() != null)
	    return Variant.VariantListBuilder.newInstance().
		    mediaTypes(getMediaType()).
		    add().build();

	return XHTML_VARIANTS;
    }

    public MediaType getMediaType()
    {
	return mediaType;
    }

    public String getTopicUri()
    {
	return topicUri;
    }

    @Override
    public Response post(Model postedModel)
    {
	if (log.isDebugEnabled()) log.debug("Returning @POST Response of the POSTed Model");
	
	Model deleteDiff = describe(false).difference(postedModel);
	if (log.isDebugEnabled()) log.debug("DESCRIBE Model minus POSTed Model: {} size: {}", deleteDiff, deleteDiff.size());
	Model insertDiff = postedModel.difference(describe(false));
	if (log.isDebugEnabled()) log.debug("POSTed Model minus from DESCRIBE Model: {} size: {}", insertDiff, insertDiff.size());
	
	UpdateRequest request = ModifyBuilder.fromModify(getOntModel()).
		deletePattern(deleteDiff).
		insertPattern(insertDiff).
		where(getQueryBuilder().getWhere()).
		build();
	if (log.isDebugEnabled()) log.debug("DELETE/INSERT generated from the POSTed Model: {}", request);
	
	return getResponseBuilder(postedModel).build();
    }

}