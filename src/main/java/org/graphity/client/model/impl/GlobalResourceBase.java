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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.client.util.DataManager;
import org.graphity.server.model.impl.LinkedDataResourceBase;
import org.graphity.server.model.SPARQLEndpoint;
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

    private final MediaType mediaType;
    private final URI topicURI;

    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the request
     * @param endpoint SPARQL endpoint of this resource
     * @param ontModel sitemap ontology model
     * @param request current request
     * @param servletContext webapp context
     * @param httpHeaders HTTP headers of current request
     * @param resourceContext resource context
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @param mode <samp>mode</samp> query string param
     * @param topicURI remote URI to be loaded
     * @param mediaType media type of the representation
     */
    public GlobalResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntModel ontModel,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
	    @QueryParam("limit") Long limit,
	    @QueryParam("offset") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
	    @QueryParam("uri") URI topicURI,
	    @QueryParam("accept") MediaType mediaType)
    {
	super(uriInfo, endpoint, ontModel,
                request, servletContext, httpHeaders, resourceContext,
                limit, offset, orderBy, desc, graphURI, mode);
	
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
     * If "uri" query string parameter is present, resource is loaded from the specified remote URI and
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

	    Model model = DataManager.get().loadModel(getTopicURI().toString());
            Resource topic = getModel().createResource(getTopicURI().toString());
            
	    addProperty(FOAF.primaryTopic, topic); // does this have any effect?

	    return getResponse(model);
	}	

	return super.get();
    }

}
