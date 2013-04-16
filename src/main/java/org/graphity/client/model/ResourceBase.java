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
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.vocabulary.GS;
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

    private final List<Variant> variants;

    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		sitemap, endpoint,
		(resourceConfig.getProperty(GS.cacheControl.getURI()) == null) ?
		    null :
		    CacheControl.valueOf(resourceConfig.getProperty(GS.cacheControl.getURI()).toString()),
		limit, offset, orderBy, desc,
		XHTML_VARIANTS);	
    }

    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntModel ontModel, SPARQLEndpoint endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc,
	    List<Variant> variants)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		ontModel.createOntResource(uriInfo.getRequestUri().toString()), endpoint, cacheControl,
		limit, offset, orderBy, desc,
		variants);
    }

    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders,ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc,
	    List<Variant> variants)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint,
		cacheControl, limit, offset, orderBy, desc);
	
	this.variants = variants;
    }

    @Override
    public Model describe()
    {
	Model description = super.describe();

	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addRDFType(getMatchedOntClass());
	
	// set metadata properties after description query is executed
	getQueryBuilder().build(); // sets sp:text value
	if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, getQueryBuilder());
	setPropertyValue(SPIN.query, getQueryBuilder());

	return description;
    }

    @Override
    public Response getResponse(Model model)
    {
	return getResponseBuilder(model).build();
    }

    @Override
    public Response.ResponseBuilder getResponseBuilder(Model model)
    {
	return getEndpoint().getResponseBuilder(model, getVariants()).
		cacheControl(getCacheControl());
    }

    public List<Variant> getVariants()
    {
	return variants;
    }

}