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
import org.graphity.client.util.DataManager;
import org.graphity.server.model.LinkedDataResource;
import org.graphity.server.model.LinkedDataResourceBase;
import org.graphity.server.model.LinkedDataResourceFactory;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.vocabulary.GS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * The remote datasources can either be native-RDF Linked Data, or formats supported by Locators
 * (for example, Atom XML transformed to RDF/XML using GRDDL XSLT stylesheet). The ability to load remote
 * RDF data is crucial for generic Linked Data browser functionality.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see LinkedDataResourceBase
 * @see org.graphity.client.locator.LocatorLinkedData
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("{path: .*}")
public class GlobalResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GlobalResourceBase.class);

    private final MediaType mediaType;
    private final URI topicURI;

    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * The URI of the resource being created is the current request URI (note: this is different from Server).
     * The sitemap ontology model and the SPARQL endpoint resource are injected via JAX-RS providers.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param sitemap sitemap ontology
     * @param endpoint active SPARQL endpoint (used to execute queries)
     * @param limit pagination LIMIT ("limit" query string param)
     * @param offset pagination OFFSET ("offset" query string param)
     * @param orderBy pagination ORDER BY variable name ("order-by" query string param)
     * @param desc pagination DESC value ("desc" query string param)
     * @param mode "mode" query string param
     * @param topicURI remote URI to be loaded ("uri" query string param)
     * @param mediaType media type of the representation ("accept" query string param)
     * @see org.graphity.processor.provider.OntologyProvider
     * @see org.graphity.processor.provider.SPARQLEndpointProvider
     */
    public GlobalResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
	    @QueryParam("uri") URI topicURI,
	    @QueryParam("accept") MediaType mediaType)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		sitemap, endpoint,
		(resourceConfig.getProperty(GS.cacheControl.getURI()) == null) ?
		    null :
		    CacheControl.valueOf(resourceConfig.getProperty(GS.cacheControl.getURI()).toString()),
		limit, offset, orderBy, desc, graphURI, mode,
		XHTML_VARIANTS,
		topicURI, mediaType);	
    }

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the request
     * @param request current request
     * @param httpHeaders HTTP headers of current request
     * @param resourceConfig webapp configuration
     * @param ontModel sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param cacheControl cache control config
     * @param limit pagination LIMIT
     * @param offset pagination OFFSET
     * @param orderBy pagination ORDER BY variable name
     * @param desc pagination DESC value
     * @param mode "mode" query string param
     * @param variants representation variants
     * @param topicURI remote URI to be loaded
     * @param mediaType media type of the representation
     */
    protected GlobalResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntModel ontModel, SPARQLEndpoint endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI, URI mode, List<Variant> variants,
	    URI topicURI, MediaType mediaType)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		ontModel.createOntResource(uriInfo.getAbsolutePath().toString()),
		endpoint, cacheControl,
		limit, offset, orderBy, desc, graphURI, mode, variants,
		topicURI, mediaType);
    }

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the request
     * @param request current request
     * @param httpHeaders HTTP headers of current request
     * @param resourceConfig webapp configuration
     * @param ontResource this resource as RDF resource
     * @param endpoint SPARQL endpoint of this resource
     * @param cacheControl cache control config
     * @param limit pagination LIMIT
     * @param offset pagination OFFSET
     * @param orderBy pagination ORDER BY variable name
     * @param desc pagination DESC value
     * @param mode "mode" query string param
     * @param variants representation variants
     * @param topicURI remote URI to be loaded
     * @param mediaType media type of the representation
     */
    protected GlobalResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI, URI mode, List<Variant> variants,
	    URI topicURI, MediaType mediaType)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint, cacheControl, limit, offset, orderBy, desc, graphURI, mode, variants);
	
	this.mediaType = mediaType;
	this.topicURI = topicURI;
	if (log.isDebugEnabled()) log.debug("Constructing GlobalResourceBase with MediaType: {} topic URI: {}", mediaType, topicURI);
    }

    /**
     * Returns URI or remotely loaded resource ("uri" query string parameter)
     * 
     * @return remote URI
     */
    public URI getTopicURI()
    {
	return topicURI;
    }

    /**
     * Returns media type requested by the client ("accept" query string parameter).
     * This mechanism overrides the normally used content negotiation.
     * 
     * @return 
     */
    public MediaType getMediaType()
    {
	return mediaType;
    }

    /**
     * Returns a list of supported RDF representation variants.
     * 
     * @return variant list
     */
    @Override
    public List<Variant> getVariants()
    {
	if (getMediaType() != null)
	    return Variant.VariantListBuilder.newInstance().
		    mediaTypes(getMediaType()).
		    add().build();

	return super.getVariants();
    }

    /**
     * Handles GET request and returns response with RDF description of this or remotely loaded resource.
     * If "uri" query string parameter is present, resource is loaded from the specified remote URI and
     * its RDF representation is returned. Otherwise, local resource with request URI is used.
     * If "accept" query string parameter is present, the specified media type is used to serialize RDF
     * representation. Otherwise, normal content negotiation is used.
     * 
     * @return 
     */
    @Override
    public Response get()
    {
	if (getTopicURI() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getTopicURI());

	    Model model = DataManager.get().loadModel(getTopicURI().toString());
	    
	    // use original Cache-Control? 
	    LinkedDataResource topic = LinkedDataResourceFactory.createResource(model.createResource(getTopicURI().toString()),
		getCacheControl());
	    
	    addProperty(FOAF.primaryTopic, topic); // does this have any effect?

	    return getResponseBuilder(model, getVariants()).build();
	}	

	return super.get();
    }

}
