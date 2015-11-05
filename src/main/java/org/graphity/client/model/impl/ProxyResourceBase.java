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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.client.exception.ClientErrorException;
import org.graphity.client.util.DataManager;
import org.graphity.core.MediaTypes;
import org.graphity.client.vocabulary.GC;
import org.graphity.core.model.SPARQLEndpoint;
import org.graphity.core.model.SPARQLEndpointFactory;
import org.graphity.core.model.SPARQLEndpointOrigin;
import org.graphity.core.model.impl.SPARQLEndpointOriginBase;
import org.graphity.core.provider.ModelProvider;
import org.graphity.core.util.Link;
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
public class ProxyResourceBase extends org.graphity.core.model.impl.QueriedResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ProxyResourceBase.class);

    private final MediaType mediaType;
    private final WebResource webResource;
    private final SPARQLEndpoint remoteEndpoint;
    private final Query userQuery;
    
    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information of the request
     * @param request current request
     * @param servletConfig servlet config
     * @param mediaTypes supported media types
     * @param endpoint SPARQL endpoint
     */
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig, @Context MediaTypes mediaTypes,
            @Context SPARQLEndpoint endpoint)
    {
	super(uriInfo, request, servletConfig, mediaTypes, endpoint);

	if (uriInfo.getQueryParameters().containsKey("accept"))
        {
            Map<String, String> utf8Param = new HashMap<>();
            utf8Param.put("charset", "UTF-8");            
            MediaType formatType = MediaType.valueOf(uriInfo.getQueryParameters().getFirst("accept"));
            mediaType = new MediaType(formatType.getType(), formatType.getSubtype(), utf8Param);
        }
        else mediaType = null;

        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new ModelProvider());
        Client client = Client.create(cc);        
        if (uriInfo.getQueryParameters().containsKey(GC.uri.getLocalName()))
        {
            URI uri = URI.create(uriInfo.getQueryParameters().getFirst(GC.uri.getLocalName()));
            webResource = client.resource(uri);
        }
        else
            webResource = null;
        
        if (uriInfo.getQueryParameters().containsKey(GC.endpointUri.getLocalName()) &&
                uriInfo.getQueryParameters().containsKey("query"))
        {
            userQuery = QueryFactory.create(uriInfo.getQueryParameters().getFirst("query"));
            
            URI endpointURI = URI.create(uriInfo.getQueryParameters().getFirst(GC.endpointUri.getLocalName()));
            List<MediaType> modelMediaTypes = new ArrayList<>(mediaTypes.getModelMediaTypes());
            modelMediaTypes.addAll(getMediaTypes().getResultSetMediaTypes());
            List<Variant> variants = getResponse().getVariantListBuilder(modelMediaTypes, getLanguages(), getEncodings()).add().build();
            Variant variant = getRequest().selectVariant(variants);

            if (!variant.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE) &&
                    !variant.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            {
                if (log.isDebugEnabled()) log.debug("Using remote SPARQL endpoint URI: {}", endpointURI);
                SPARQLEndpointOrigin origin = new SPARQLEndpointOriginBase(endpointURI.toString());
                this.remoteEndpoint = SPARQLEndpointFactory.createProxy(request, servletConfig, mediaTypes, origin, (DataManager)DataManager.get());
            }
            else this.remoteEndpoint = null;
        }
        else
        {
            this.remoteEndpoint = null;
            this.userQuery = null;
        }

        /*
        URI classURI = null;
        List<String> links = httpHeaders.getRequestHeader("Link");
        if (links != null)
        {
            Iterator<String> it = links.iterator();
            Link link = Link.valueOf(it.next());
            if (link.getType().equals(RDF.type.getLocalName())) classURI = link.getHref();
        }
        if (classURI != null) matchedOntClass = OntDocumentManager.getInstance().getOntology(classURI.toString(), OntModelSpec.OWL_MEM);
        else matchedOntClass = null;
        */
        
        //if (log.isDebugEnabled()) log.debug("Constructing GlobalResourceBase with MediaType: {} topic URI: {}", mediaType, uri);
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
     * Returns remote SPARQL endpoint (<samp>endpointUri</samp> query string parameter).
     * 
     * @return SPARQL endpoint URI
     */
    public SPARQLEndpoint getRemoteEndpoint()
    {
        return remoteEndpoint;
    }
    
    public Query getUserQuery()
    {
        return userQuery;
    }
    
    public WebResource getWebResource()
    {
        return webResource;
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
        if (getWebResource() != null)
        {
            ClientResponse resp = getWebResource().
                accept(org.graphity.core.MediaType.TEXT_NTRIPLES_TYPE, org.graphity.core.MediaType.APPLICATION_RDF_XML_TYPE).
                get(ClientResponse.class);

            if (resp.getStatusInfo().getFamily().equals(Status.Family.CLIENT_ERROR))
                throw new ClientErrorException(resp);
            
            Link link = null;
            if (resp.getHeaders().getFirst("Link") != null)
                try
                {
                    // TO-DO: add support for Rules and gp:ontology Link HTTP headers
                    link = Link.valueOf(resp.getHeaders().getFirst("Link"));
                }
                catch (URISyntaxException ex)   
                {
                    if (log.isDebugEnabled()) log.debug("'Link' header contains invalid URI: {}", resp.getHeaders().getFirst("Link"));
                }
            
            if (log.isDebugEnabled()) log.debug("Loading Model from URI: {}", getWebResource().getURI());
            Model description = resp.getEntity(Model.class);
            
            ResponseBuilder bld = getResponseBuilder(description);
            if (link != null) bld.header("Link", link.toString()); // move to HypermediaFilter?
            return bld.build(); // TO-DO: MediaTypes!
        }

        if (getRemoteEndpoint() != null && getUserQuery() != null)
        {
            Response response = getRemoteEndpoint().get(getUserQuery(), null, null);
            return response;
        }
        
        return super.get();
    }

    @Override
    public Response post(Model model)
    {
        if (log.isDebugEnabled()) log.debug("Submitting Model to URI: {}", getWebResource().getURI());
        return getResponse(getWebResource().post(Model.class, model));
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
        if (log.isDebugEnabled()) log.debug("Submitting Model to URI: {}", getWebResource().getURI());
        return getResponse(getWebResource().put(Model.class, model));

        /*
        if (getMode() != null && getMode().equals(URI.create(GC.EditMode.getURI())))
        {
            super.put(model);
            
            Resource document = getURIResource(model, RDF.type, FOAF.Document);
	    if (log.isDebugEnabled()) log.debug("Mode is {}, redirecting to document URI {} after PUT", getMode(), document.getURI());
            return Response.seeOther(URI.create(document.getURI())).build();
        }
        */
    }
    
    @Override
    public Response delete()
    {
        if (log.isDebugEnabled()) log.debug("Submitting Model to URI: {}", getWebResource().getURI());
        //return getWebResource().delete(ClientResponse.class);
        return null;
    }
    
}
