/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.model.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.util.ArrayList;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import com.atomgraph.core.client.LinkedDataClient;
import com.atomgraph.client.exception.ClientErrorException;
import com.atomgraph.client.filter.RedirectFilter;
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.core.MediaTypes;
import com.atomgraph.core.exception.AuthenticationException;
import com.atomgraph.core.exception.NotFoundException;
import com.atomgraph.core.io.DatasetProvider;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.Link;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Variant;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
@Path("/")
public class ProxyResourceBase implements Resource
{
    private static final Logger log = LoggerFactory.getLogger(ProxyResourceBase.class);
    
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final MediaTypes mediaTypes;
    private final MediaType accept;
    private final MediaType[] readableMediaTypes;
    private final WebResource webResource;
    private final LinkedDataClient linkedDataClient;
    private final HttpServletRequest httpServletRequest;
    
    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information
     * @param request request
     * @param httpHeaders HTTP headers
     * @param mediaTypes supported media types
     * @param uri RDF resource URI
     * @param endpoint SPARQL endpoint URI
     * @param accept response media type
     * @param mode layout mode
     * @param client HTTP client
     * @param httpServletRequest HTTP request
     */
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("endpoint") URI endpoint, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode,
            @Context Client client, @Context HttpServletRequest httpServletRequest)
    {
        if (uri == null) throw new NotFoundException("Resource URI not supplied"); // TO-DO: BadRequestException
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        this.accept = accept;
        List<javax.ws.rs.core.MediaType> readableMediaTypesList = new ArrayList<>();
        //readableMediaTypesList.addAll(mediaTypes.getReadable(Dataset.class));
        readableMediaTypesList.addAll(mediaTypes.getReadable(Model.class));
        this.readableMediaTypes = readableMediaTypesList.toArray(new MediaType[readableMediaTypesList.size()]);
        
        // client.setFollowRedirects(true); // doesn't work: https://stackoverflow.com/questions/29955951/jersey-is-not-following-302-redirects/29957936
        if (uri.getFragment() != null)
            try
            {
                // strip #fragment as we don't want to use it in the request to server
                uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), null);
            }
            catch (URISyntaxException ex)
            {
                // should not happen
            }
        
        webResource = client.resource(uri);
        webResource.addFilter(new RedirectFilter());
        linkedDataClient = LinkedDataClient.create(webResource, mediaTypes);
        this.httpServletRequest = httpServletRequest;
    }
    
    @Override
    public URI getURI()
    {
        return getWebResource().getURI();
    }
    
    public ClientResponse getClientResponse(WebResource webResource, HttpHeaders httpHeaders)
    {
        return webResource.getRequestBuilder().
                accept(getReadableMediaTypes()).
                get(ClientResponse.class);
    }
    
    public MediaType[] getReadableMediaTypes()
    {
        return readableMediaTypes;
    }
    
    public List<MediaType> getWritableMediaTypes(Class clazz)
    {
        // restrict writable MediaTypes to the requested one (usually by RDF export feature)
        if (getAcceptMediaType() != null) return Arrays.asList(getAcceptMediaType());

        return getMediaTypes().getWritable(clazz);
    }
    
    /**
     * Forwards GET request and returns response from remote resource.
     * 
     * @return response
     */
    @GET
    @Override
    public Response get()
    {
        ClientResponse cr = null;
        try
        {
            cr = getClientResponse(getWebResource(), getHttpHeaders());

            if (cr.getStatusInfo().getFamily().equals(Status.Family.CLIENT_ERROR))
            {
                // forward WWW-Authenticate response header
                if (cr.getHeaders().containsKey(HttpHeaders.WWW_AUTHENTICATE))
                {
                    String header = cr.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
                    if (header.contains("Basic realm="))
                    {
                        int realmStart = header.indexOf("\"") + 1;
                        int realmEnd = header.lastIndexOf("\"");
                        
                        String realm = header.substring(realmStart, realmEnd);
                        throw new AuthenticationException("Login is required", realm);
                    }
                }

                if (cr.hasEntity())
                    throw new ClientErrorException(cr, DatasetFactory.create(cr.getEntity(Model.class)));
                else 
                    throw new ClientErrorException(cr);
            }

            cr.getHeaders().putSingle(DatasetProvider.REQUEST_URI_HEADER, getWebResource().getURI().toString()); // provide a base URI hint to ModelProvider/DatasetProvider
            
            if (log.isDebugEnabled()) log.debug("GETing Dataset from URI: {}", getWebResource().getURI());

            if (cr.getHeaders().get("Link") != null)
                for (String linkValue : cr.getHeaders().get("Link"))
                {
                    try
                    {
                        Link link = Link.valueOf(linkValue);
                        if (link.getRel().equals(LDT.ontology.getURI())) getHttpServletRequest().setAttribute(LDT.ontology.getURI(), link.getHref());
                        if (link.getRel().equals(LDT.base.getURI())) getHttpServletRequest().setAttribute(LDT.base.getURI(), link.getHref());
                        if (link.getRel().equals(LDT.template.getURI())) getHttpServletRequest().setAttribute(LDT.template.getURI(), link.getHref());
                    }
                    catch (URISyntaxException ex)
                    {
                        if (log.isErrorEnabled()) log.debug("Could parse Link URI: {}", ex.getInput());
                    }
                }

            Model description = cr.getEntity(Model.class);
            
            com.atomgraph.core.model.impl.Response response = com.atomgraph.core.model.impl.Response.fromRequest(getRequest());
            List<Variant> variants = response.getVariantListBuilder(getWritableMediaTypes(Dataset.class), new ArrayList(), new ArrayList()).add().build();

            Variant variant = getRequest().selectVariant(variants);
            if (variant == null)
            {
                variants = response.getVariantListBuilder(getWritableMediaTypes(Model.class), new ArrayList(), new ArrayList()).add().build(); // fallback to Model
                return response.getResponseBuilder(description, variants).build();
            }

            return response.getResponseBuilder(DatasetFactory.create(description), variants).build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

    /**
     * Forwards POST request with RDF dataset body and returns RDF response from remote resource.
     * 
     * @param dataset
     * @return response
     */
    @POST
    @Override
    public Response post(Dataset dataset)
    {
        if (log.isDebugEnabled()) log.debug("POSTing Dataset to URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = webResource.type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                post(ClientResponse.class, dataset);
            Response.ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Dataset.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

    /**
     * Forwards PUT request with RDF dataset body and returns response from remote resource.
     * 
     * @param dataset RDF payload
     * @return response
     */
    @PUT
    @Override
    public Response put(Dataset dataset)
    {
        if (log.isDebugEnabled()) log.debug("PUTting Dataset to URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = getWebResource().type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                put(ClientResponse.class, dataset);
            ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Dataset.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
    /**
     * Forwards DELETE request and returns response from remote resource.
     * @return response
     */
    @DELETE
    @Override
    public Response delete()
    {
        if (log.isDebugEnabled()) log.debug("DELETEing Dataset from URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = getWebResource().
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                delete(ClientResponse.class);
            ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Dataset.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
    public HttpHeaders getHttpHeaders()
    {
        return httpHeaders;
    }
    
    /**
     * Returns media type requested by the client ("accept" query string parameter).
     * This mechanism overrides the normally used content negotiation.
     * 
     * @return media type parsed from query param
     */
    public MediaType getAcceptMediaType()
    {
        return accept;
    }
    
    public final WebResource getWebResource()
    {
        return webResource;
    }

    public Request getRequest()
    {
        return request;
    }
    
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
    public LinkedDataClient getLinkedDataClient()
    {
        return linkedDataClient;
    }
    
    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }
    
}
