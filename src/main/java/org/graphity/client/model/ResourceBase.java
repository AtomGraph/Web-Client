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
 * Base class of generic read-write Graphity Client resources.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("{path: .*}")
public class ResourceBase extends org.graphity.processor.model.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    /**
     * Media types that can be used for representation of RDF model, including XHTML
     */
    public static List<Variant> XHTML_VARIANTS = Variant.VariantListBuilder.newInstance().
		mediaTypes(MediaType.APPLICATION_XHTML_XML_TYPE,
		    //MediaType.TEXT_HTML_TYPE,
		    org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE,
		    org.graphity.server.MediaType.TEXT_TURTLE_TYPE).
		add().build();

    private final List<Variant> variants;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * The URI of the resource being created is the current request URI (note: this is different from Server).
     * The sitemap ontology model and the SPARQL endpoint resource are injected via JAX-RS providers.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param sitemap sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination LIMIT ("limit" query string param)
     * @param offset pagination OFFSET ("offset" query string param)
     * @param orderBy pagination ORDER BY variable name ("order-by" query string param)
     * @param desc pagination DESC value ("desc" query string param)
     * @see org.graphity.processor.provider.OntologyProvider
     * @see org.graphity.processor.provider.SPARQLEndpointProvider
     */
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

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param ontModel sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param cacheControl cache control config
     * @param limit pagination LIMIT ("limit" query string param)
     * @param offset pagination OFFSET ("offset" query string param)
     * @param orderBy pagination ORDER BY variable name ("order-by" query string param)
     * @param desc pagination DESC value ("desc" query string param)
     * @param variants representation variants
     */
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

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param ontResource this resource as OWL resource
     * @param endpoint SPARQL endpoint of this resource
     * @param cacheControl cache control config
     * @param limit pagination LIMIT ("limit" query string param)
     * @param offset pagination OFFSET ("offset" query string param)
     * @param orderBy pagination ORDER BY variable name ("order-by" query string param)
     * @param desc pagination DESC value ("desc" query string param)
     * @param variants representation variants
     */
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