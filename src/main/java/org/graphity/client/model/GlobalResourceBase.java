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
import org.graphity.processor.model.Application;
import org.graphity.server.model.LinkedDataResourceBase;
import org.graphity.server.model.SPARQLEndpoint;
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
     * @param request current request
     * @param httpHeaders HTTP headers of current request
     * @param servletContext webapp context
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @param mode <samp>mode</samp> query string param
     * @param topicURI remote URI to be loaded
     * @param mediaType media type of the representation
     */
    public GlobalResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntModel ontModel, @Context Application application,
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
	super(uriInfo, endpoint, ontModel, application,
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
            Resource topic = getModel().createResource(getTopicURI().toString());
            
	    addProperty(FOAF.primaryTopic, topic); // does this have any effect?

	    return getResponse(model);
	}	

	return super.get();
    }

}
