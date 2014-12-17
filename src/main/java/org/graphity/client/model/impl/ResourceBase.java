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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.client.vocabulary.GC;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of generic read-write Graphity Client resources.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class ResourceBase extends org.graphity.processor.model.impl.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final URI mode;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the current request
     * @param endpoint SPARQL endpoint of this resource
     * @param ontClass sitemap ontology model
     * @param request current request
     * @param servletContext webapp context
     * @param httpHeaders HTTP headers of the current request
     * @param resourceContext resource context
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntClass ontClass,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, endpoint, ontClass,
                request, servletContext, httpHeaders, resourceContext);

        if (getUriInfo().getQueryParameters().containsKey(GC.mode.getLocalName()))
            this.mode = URI.create(getUriInfo().getQueryParameters().getFirst(GC.mode.getLocalName()));
        else mode = null;
    }
    
    @Override
    public void init()
    {
        super.init();
       
        if (getMode() != null && getMatchedOntClass().hasSuperClass(LDP.Container) &&
            (getMode().equals(URI.create(GC.CreateMode.getURI())) || getMode().equals(URI.create(GC.EditMode.getURI()))) &&
            getQueryBuilder().getSubSelectBuilder() != null)
	{
            if (log.isDebugEnabled()) log.debug("Mode is {}, setting sub-SELECT LIMIT to zero", getMode());
            getQueryBuilder().getSubSelectBuilder().replaceLimit(Long.valueOf(0));
            getQueryBuilder().build();
        }
    }
    
    /**
     * Returns SPARQL endpoint resource.
     * If (X)HTML is requested, Linked Data resource is returned. Otherwise, SPARQL endpoint resource is returned.
     * 
     * @return endpoint resource
     */
    @Path("sparql")
    @Override
    public Object getSPARQLResource()
    {
        List<Variant> variants = getVariants();
        variants.addAll(SPARQLEndpoint.RESULT_SET_VARIANTS);
        Variant variant = getRequest().selectVariant(variants);

        if (!variant.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE) &&
                !variant.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            return super.getSPARQLResource();
        
        return this;
    }

    /**
     * Builds a list of acceptable response variants
     * 
     * @return supported variants
     */
    @Override
    public List<Variant> getVariants()
    {
        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.APPLICATION_XHTML_XML_TYPE, null, null));

        if (getMode() != null && getMode().equals(URI.create(GC.MapMode.getURI())))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Google Maps workaround", getMode());
            list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null));
	}

        return list;
    }
    
    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * Redirects to document URI in <pre>gc:EditMode</pre>.
     * 
     * @param model RDF payload
     * @return response
     */
    @Override
    public Response put(Model model)
    {
        if (getMode() != null && getMode().equals(URI.create(GC.EditMode.getURI())))
        {
            super.put(model);
            
            Resource document = getURIResource(model, FOAF.Document);
	    if (log.isDebugEnabled()) log.debug("Mode is {}, redirecting to document URI {} after PUT", getMode(), document.getURI());
            return Response.seeOther(URI.create(document.getURI())).build();
        }
        
        return super.put(model);
    }
    
    /**
     * Returns the layout mode query parameter value.
     * 
     * @return mode URI
     */
    public URI getMode()
    {
	return mode;
    }

    /**
     * Returns page URI builder.
     * 
     * @return URI builder
     */
    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getMode() != null) return super.getPageUriBuilder().queryParam(GC.mode.getLocalName(), getMode());
	
	return super.getPageUriBuilder();
    }

    /**
     * Returns previous page URI builder.
     * 
     * @return URI builder
     */
    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getMode() != null) return super.getPreviousUriBuilder().queryParam(GC.mode.getLocalName(), getMode());
	
	return super.getPreviousUriBuilder();
    }

    /**
     * Returns next page URI builder.
     * 
     * @return URI builder
     */
    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getMode() != null) return super.getNextUriBuilder().queryParam(GC.mode.getLocalName(), getMode());
	
	return super.getNextUriBuilder();
    }
    
}