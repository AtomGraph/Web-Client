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

import org.apache.jena.rdf.model.Model;
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
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.core.MediaTypes;
import com.atomgraph.core.exception.AuthenticationException;
import com.atomgraph.core.exception.NotFoundException;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.Link;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
@Path("/")
public class ProxyResourceBase implements Resource
{
    private static final Logger log = LoggerFactory.getLogger(ProxyResourceBase.class);
    
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final MediaTypes mediaTypes;
    private final MediaType[] acceptable;
    private final WebResource webResource;
    private final LinkedDataClient linkedDataClient;
    
    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information
     * @param request request
     * @param httpHeaders HTTP headers
     * @param mediaTypes supported media types
     * @param uri RDF resource URI
     * @param accept response media type
     * @param mode layout mode
     * @param forClass instance class
     * @param client
     */
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode, @QueryParam("forClass") URI forClass,
            @Context Client client)
    {
        if (uri == null) throw new NotFoundException("Resource URI not supplied");
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        if (accept == null) this.acceptable = mediaTypes.getReadable(Model.class).toArray(new MediaType[0]);
        else this.acceptable = new MediaType[]{accept}; // overrides Accept value

        //client.setFollowRedirects(false);
        webResource = client.resource(uri);
        linkedDataClient = LinkedDataClient.create(webResource, mediaTypes);
    }
    
    @Override
    public URI getURI()
    {
        return getWebResource().getURI();
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
    public MediaType[] getAcceptable()
    {
	return acceptable;
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
    
    public ClientResponse getClientResponse(WebResource webResource, HttpHeaders httpHeaders)
    {
        return webResource.getRequestBuilder().
                accept(getAcceptable()).
                get(ClientResponse.class);
    }
    
    /**
     * Handles GET request and returns response with RDF description of this or remotely loaded resource.
     * If <samp>uri</samp> query string parameter is present, resource is loaded from the specified remote URI and
     * its RDF representation is returned. Otherwise, local resource with request URI is used.
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
                    throw new ClientErrorException(cr, cr.getEntity(Model.class));
                else 
                    throw new ClientErrorException(cr);
            }

            if (log.isDebugEnabled()) log.debug("GETing Model from URI: {}", getWebResource().getURI());
            Model description = cr.getEntity(Model.class);

            com.atomgraph.core.model.impl.Response response = com.atomgraph.core.model.impl.Response.fromRequest(getRequest());
            ResponseBuilder rb = response.getResponseBuilder(description,
                    response.getVariantListBuilder(getMediaTypes().getWritable(Model.class), new ArrayList(), new ArrayList()).
                            add().build());

            // move headers to HypermediaFilter?
            if (cr.getHeaders().get("Link") != null)
                for (String linkValue : cr.getHeaders().get("Link"))
                {
                    try
                    {
                        Link link = Link.valueOf(linkValue);
                        if (link.getRel().equals(LDT.ontology.getURI()) || link.getRel().equals(LDT.base.getURI())) // only recognized relationships are forwarded
                            rb.header("Link", linkValue);
                    } catch (URISyntaxException ex)
                    {
                        if (log.isErrorEnabled()) log.debug("Could parse Link URI: {}", ex.getInput());
                    }
                }
            /*
            if (resp.getHeaders().get("Rules") != null)
                for (String linkValue : resp.getHeaders().get("Rules"))
                    rb.header("Rules", linkValue);
            */

            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

    @POST
    @Override
    public Response post(Model model)
    {
        if (log.isDebugEnabled()) log.debug("POSTing Model to URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = webResource.type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
                accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
                post(ClientResponse.class, model);
            Response.ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Model.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @return response
     */
    @PUT
    @Override
    public Response put(Model model)
    {
        if (log.isDebugEnabled()) log.debug("PUTting Model to URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = getWebResource().type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
                accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
                put(ClientResponse.class, model);
            ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Model.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
    @DELETE
    @Override
    public Response delete()
    {
        if (log.isDebugEnabled()) log.debug("DELETEing Model from URI: {}", getWebResource().getURI());
        ClientResponse cr = null;
        try
        {
            cr = getWebResource().
                accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
                delete(ClientResponse.class);
            ResponseBuilder rb = Response.status(cr.getStatusInfo());
            if (cr.hasEntity()) rb.entity(cr.getEntity(Model.class)); // cr.getEntityInputStream()
            return rb.build();
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

}
