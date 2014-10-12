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
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.processor.util.DataManager;
import org.graphity.server.model.impl.LinkedDataResourceBase;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.model.SPARQLEndpointFactory;
import org.graphity.server.model.SPARQLEndpointOrigin;
import org.graphity.server.model.impl.SPARQLEndpointOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * The remote datasources can either be native-RDF Linked Data, or formats supported by Locators
 * (for example, Atom XML transformed to RDF/XML using GRDDL XSLT stylesheet). The ability to load remote
 * RDF data is crucial for generic Linked Data browser functionality.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see LinkedDataResourceBase
 * @see org.graphity.client.locator.LocatorLinkedData
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class GlobalResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(GlobalResourceBase.class);

    private final DataManager dataManager;
    private MediaType mediaType;
    private URI topicURI, endpointURI;

    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the request
     * @param endpoint SPARQL endpoint of this resource
     * @param ontClass sitemap ontology model
     * @param request current request
     * @param servletContext webapp context
     * @param httpHeaders HTTP headers of current request
     * @param resourceContext resource context
     * @param dataManager data manager for this resource
     */
    public GlobalResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntClass ontClass,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
            @Context DataManager dataManager)
    {
	super(uriInfo, endpoint, ontClass,
                request, servletContext, httpHeaders, resourceContext);
	if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        this.dataManager = dataManager;
    }

    /**
     * Post-constructor initialization of class members.
     * super.init() needs to be called first in subclasses (just like super() constructor).
     */
    @Override
    public void init()
    {
        super.init();
        
	if (getUriInfo().getQueryParameters().containsKey("accept"))
            mediaType = MediaType.valueOf(getUriInfo().getQueryParameters().getFirst("accept"));
        else mediaType = null;
        if (getUriInfo().getQueryParameters().containsKey("uri"))
            topicURI = URI.create(getUriInfo().getQueryParameters().getFirst("uri"));
        else topicURI = null;
        if (getUriInfo().getQueryParameters().containsKey("endpoint-uri"))
            endpointURI = URI.create(getUriInfo().getQueryParameters().getFirst("endpoint-uri"));
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
     * @return 
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
     * Returns a list of supported RDF representation variants.
     * If media type is specified in query string,that type is used to serialize RDF representation.
     * Otherwise, normal content negotiation is used.
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
     * 
     * @return 
     */
    @Override
    public Object getSPARQLResource()
    {
        if (getEndpointURI() != null)
        {
            List<Variant> variants = getVariants();
            variants.addAll(SPARQLEndpoint.RESULT_SET_VARIANTS);
            Variant variant = getRequest().selectVariant(variants);

            if (!variant.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE) &&
                    !variant.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            {
                if (log.isDebugEnabled()) log.debug("Using remote SPARQL endpoint URI: {}", getEndpointURI());
                SPARQLEndpointOrigin origin = new SPARQLEndpointOriginBase(getEndpointURI().toString());
                return SPARQLEndpointFactory.createProxy(getRequest(), getServletContext(), origin, getDataManager());
            }
        }
        
        return super.getSPARQLResource();
    }

}
