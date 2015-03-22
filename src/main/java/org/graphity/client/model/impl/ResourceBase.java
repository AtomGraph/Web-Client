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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
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
import org.graphity.processor.vocabulary.GP;
import org.graphity.core.model.GraphStore;
import org.graphity.core.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

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

    private final String queryString;
    
    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param servletConfig webapp context
     * @param endpoint SPARQL endpoint of this resource
     * @param graphStore Graph Store of this resource
     * @param ontClass sitemap ontology model
     * @param httpHeaders HTTP headers of the current request
     * @param resourceContext resource context
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig,
            @Context SPARQLEndpoint endpoint, @Context GraphStore graphStore,
            @Context OntClass ontClass, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, request, servletConfig,
                endpoint, graphStore,
                ontClass, httpHeaders, resourceContext);
        
	if (getUriInfo().getQueryParameters().containsKey("query"))
            queryString = getUriInfo().getQueryParameters().getFirst("query");
        else queryString = null;
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
        if (getMatchedOntClass().equals(GP.SPARQLEndpoint))
        {
            List<MediaType> mediaTypes = getMediaTypes();
            mediaTypes.addAll(Arrays.asList(SPARQLEndpoint.RESULT_SET_MEDIA_TYPES));
            List<Variant> variants = getVariantListBuilder(mediaTypes, getLanguages(), getEncodings()).add().build();
            Variant variant = getRequest().selectVariant(variants);
            
            if (variant.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE) ||
                    variant.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
                return this;
        }
        
        return super.getSubResource();
    }
    
    /**
     * Adds run-time metadata to RDF description.
     * In case a container is requested, page resource with HATEOS previous/next links is added to the model.
     * 
     * @param model target RDF model
     * @return description model with metadata
     */
    @Override
    public Model addMetadata(Model model)
    {
        NodeIterator it = getMatchedOntClass().listPropertyValues(GC.supportedMode);
        try
        {
            while (it.hasNext())
            {
                RDFNode supportedMode = it.next();
                if (!supportedMode.isURIResource())
                {
                    if (log.isErrorEnabled()) log.error("Invalid Mode defined for template '{}' (gc:supportedMode)", getMatchedOntClass().getURI());
                    //throw new ConfigurationException("Invalid Mode defined for template '" + getMatchedOntClass().getURI() +"'");
                }
                else
                {
                    if (!supportedMode.equals(GP.ConstructMode))
                    {
                        if (getMatchedOntClass().equals(GP.Container))
                        {
                            String pageURI = getStateUriBuilder(getOffset(), getLimit(), getOrderBy(), getDesc(), null).build().toString();                            
                            String pageModeURI = getStateUriBuilder(getOffset(), getLimit(), getOrderBy(), getDesc(), URI.create(supportedMode.asResource().getURI())).build().toString();
                            createState(model.createResource(pageModeURI), getOffset(), getLimit(), getOrderBy(), getDesc(), supportedMode.asResource()).
                                addProperty(RDF.type, FOAF.Document).                                    
                                addProperty(RDF.type, GP.Page).
                                addProperty(GP.pageOf, this).
                                addProperty(GC.layoutOf, model.createResource(pageURI));
                        }
                        else
                        {
                            String modeURI = getStateUriBuilder(null, null, null, null, URI.create(supportedMode.asResource().getURI())).build().toString();
                            createState(model.createResource(modeURI), null, null, null, null, supportedMode.asResource()).
                                addProperty(RDF.type, FOAF.Document).
                                addProperty(GC.layoutOf, this);                            
                        }
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return super.addMetadata(model);
    }
        
    /**
     * Builds a list of acceptable response variants
     * 
     * @return supported variants
     */
    @Override
    public List<MediaType> getMediaTypes()
    {
        List<MediaType> list = super.getMediaTypes();
        list.add(0, MediaType.APPLICATION_XHTML_XML_TYPE);

        if (getMode() != null && getMode().equals(URI.create(GC.MapMode.getURI())))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Google Maps workaround", getMode());
            list.add(0, MediaType.TEXT_HTML_TYPE);
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

    @Override
    public Resource createState(Resource state, Long offset, Long limit, String orderBy, Boolean desc, Resource mode)
    {
        Resource superState = super.createState(state, offset, limit, orderBy, desc, mode);
        
	if (getQueryString() != null) superState.addProperty(SPIN.query,
                superState.getModel().createResource().addLiteral(SP.text, getQueryString()));
        
        return superState;
    }

    @Override
    public UriBuilder getStateUriBuilder(Long offset, Long limit, String orderBy, Boolean desc, URI mode)
    {
	UriBuilder builder = super.getStateUriBuilder(offset, limit, orderBy, desc, mode);
        
	if (getQueryString() != null) builder.queryParam(SPIN.query.getLocalName(), getQueryString());
        
        return builder;
    }
    
    public String getQueryString()
    {
        return queryString;
    }
    
}