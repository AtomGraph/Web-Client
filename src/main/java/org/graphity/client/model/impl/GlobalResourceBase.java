/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.client.model.impl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.client.vocabulary.GC;
import org.graphity.processor.util.DataManager;
import org.graphity.core.model.GraphStore;
import org.graphity.core.model.SPARQLEndpoint;
import org.graphity.core.model.SPARQLEndpointFactory;
import org.graphity.core.model.SPARQLEndpointOrigin;
import org.graphity.core.model.impl.SPARQLEndpointOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * The remote datasources can either be native-RDF Linked Data, or formats supported by Locators
 * (for example, Atom XML transformed to RDF/XML using GRDDL XSLT stylesheet). The ability to load remote
 * RDF data is crucial for generic Linked Data browser functionality.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.locator.LocatorLinkedData
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class GlobalResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GlobalResourceBase.class);

    private final DataManager dataManager;
    private final MediaType mediaType;
    private final URI topicURI, endpointURI;

    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the request
     * @param request current request
     * @param servletConfig servlet config
     * @param endpoint SPARQL endpoint of this resource
     * @param graphStore Graph Store of this resource
     * @param ontClass sitemap ontology model
     * @param httpHeaders HTTP headers of current request
     * @param resourceContext resource context
     * @param dataManager data manager for this resource
     */
    public GlobalResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig,
            @Context SPARQLEndpoint endpoint, @Context GraphStore graphStore,
            @Context OntClass ontClass, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
            @Context DataManager dataManager)
    {
	super(uriInfo, request, servletConfig, 
                endpoint, graphStore,
                ontClass, httpHeaders, resourceContext);
	if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        this.dataManager = dataManager;

	if (getUriInfo().getQueryParameters().containsKey("accept"))
            mediaType = MediaType.valueOf(getUriInfo().getQueryParameters().getFirst("accept"));
        else mediaType = null;
        if (getUriInfo().getQueryParameters().containsKey(GC.uri.getLocalName()))
            topicURI = URI.create(getUriInfo().getQueryParameters().getFirst(GC.uri.getLocalName()));
        else topicURI = null;
        if (getUriInfo().getQueryParameters().containsKey(GC.endpointUri.getLocalName()))
            endpointURI = URI.create(getUriInfo().getQueryParameters().getFirst(GC.endpointUri.getLocalName()));
        else endpointURI = null;

        if (log.isDebugEnabled()) log.debug("Constructing GlobalResourceBase with MediaType: {} topic URI: {}", mediaType, topicURI);
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }

    /**
     * Returns URI of remote resource (<samp>uri</samp> query string parameter)
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
     * @return media type parsed from query param
     */
    public MediaType getMediaType()
    {
	return mediaType;
    }

    /**
     * Returns URI of remote SPARQL endpoint (<samp>endpoint-uri</samp> query string parameter).
     * 
     * @return SPARQL endpoint URI
     */
    public URI getEndpointURI()
    {
        return endpointURI;
    }
    
    /**
     * Returns a list of supported RDF media types.
     * If media type is specified in query string,that type is used to serialize RDF representation.
     * Otherwise, normal content negotiation is used.
     * 
     * @return variant list
     */
    @Override
    public List<MediaType> getMediaTypes()
    {
	if (getMediaType() != null)
        {
            List<MediaType> list = new ArrayList<>();
            list.add(getMediaType());
            return list;
        }
        
	return super.getMediaTypes();
    }

    /**
     * Handles GET request and returns response with RDF description of this or remotely loaded resource.
     * If <samp>uri</samp> query string parameter is present, resource is loaded from the specified remote URI and
     * its RDF representation is returned. Otherwise, local resource with request URI is used.
     * 
     * @return response
     */
    @Override
    public Response get()
    {
	if (getTopicURI() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getTopicURI());
	    return getResponse(getDataManager().loadModel(getTopicURI().toString()));
	}	

	return super.get();
    }

    /**
     * Returns sub-resource instance.
     * By default matches any path.
     * 
     * @return resource object
     */
    @Path("{path: .+}")
    @Override
    public Object getSubResource()
    {
        if (getEndpointURI() != null)
        {
            List<MediaType> mediaTypes = getMediaTypes();
            mediaTypes.addAll(Arrays.asList(SPARQLEndpoint.RESULT_SET_MEDIA_TYPES));
            List<Variant> variants = getVariantListBuilder(mediaTypes, getLanguages(), getEncodings()).add().build();
            Variant variant = getRequest().selectVariant(variants);

            if (!variant.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE) &&
                    !variant.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            {
                if (log.isDebugEnabled()) log.debug("Using remote SPARQL endpoint URI: {}", getEndpointURI());
                SPARQLEndpointOrigin origin = new SPARQLEndpointOriginBase(getEndpointURI().toString());
                return SPARQLEndpointFactory.createProxy(getRequest(), getServletConfig(), origin, getDataManager());
            }
        }
        
        return super.getSubResource();
    }

    @Override
    public Resource createState(Resource state, Long offset, Long limit, String orderBy, Boolean desc, Resource mode)
    {
        Resource superState = super.createState(state, offset, limit, orderBy, desc, mode);

	if (getTopicURI() != null) superState.addProperty(GC.uri, state.getModel().createResource(getTopicURI().toString()));
	if (getEndpointURI() != null) superState.addProperty(GC.endpointUri, state.getModel().createResource(getEndpointURI().toString()));
        
        return state;
    }

    @Override
    public UriBuilder getStateUriBuilder(Long offset, Long limit, String orderBy, Boolean desc, URI mode)
    {
        UriBuilder builder = super.getStateUriBuilder(offset, limit, orderBy, desc, mode);
        
	if (getTopicURI() != null) builder.queryParam(GC.uri.getLocalName(), getTopicURI().toString());
	if (getEndpointURI() != null) builder.queryParam(GC.endpointUri.getLocalName(), getEndpointURI().toString());
	
	return builder;
    }
    
}
