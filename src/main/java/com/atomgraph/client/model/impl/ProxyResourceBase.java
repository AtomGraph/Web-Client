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
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeader;
import com.atomgraph.client.LinkedDataClient;
import com.atomgraph.client.exception.ClientErrorException;
import com.atomgraph.core.MediaTypes;
import com.atomgraph.core.exception.AuthenticationException;
import com.atomgraph.core.exception.NotFoundException;
import com.atomgraph.core.provider.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that can publish Linked Data and (X)HTML as well as load RDF from remote sources.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
@Path("/")
public class ProxyResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ProxyResourceBase.class);

    private final UriInfo uriInfo;
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final MediaTypes mediaTypes;
    private final MediaType mediaType;
    private final WebResource webResource;
    private final LinkedDataClient linkedDataClient;
    private final URI mode, forClass;
    
    /**
     * JAX-RS compatible resource constructor with injected initialization objects.
     * 
     * @param uriInfo URI information
     * @param request request
     * @param httpHeaders HTTP headers
     * @param mediaTypes supported media types
     * @param uri RDF resource URI
     * @param mediaType response media type
     * @param mode layout mode
     * @param forClass instance class
     */
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("accept") MediaType mediaType, @QueryParam("mode") URI mode, @QueryParam("forClass") URI forClass)
    {
        if (uri == null) throw new NotFoundException("Resource URI not supplied");        
        this.uriInfo = uriInfo;
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        this.mediaType = mediaType;
        this.mode = mode;
        this.forClass = forClass;

        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new ModelProvider());
        Client client = Client.create(cc);
        //client.setFollowRedirects(false);
        webResource = client.resource(uri);
        linkedDataClient = LinkedDataClient.create(webResource);
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
    public MediaType getMediaType()
    {
	return mediaType;
    }
    
    public WebResource getWebResource()
    {
        return webResource;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Request getRequest()
    {
        return request;
    }
    
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
    public URI getMode()
    {
        return mode;
    }
    
    public URI getForClass()
    {
        return forClass;
    }

    public LinkedDataClient getLinkedDataClient()
    {
        return linkedDataClient;
    }
    
    public ClientResponse getClientResponse(WebResource webResource, HttpHeaders httpHeaders, MediaType... mediaTypes)
    {
        WebResource.Builder builder = webResource.getRequestBuilder();

        // forward Authorization request header
        List<String> authHeaders = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty())
            builder = getWebResource().header(HttpHeaders.AUTHORIZATION, authHeaders.get(0));

        return builder.accept(mediaTypes).
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
    public Response get()
    {                
        ClientResponse resp = getClientResponse(getWebResource(), getHttpHeaders(),
                com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE, com.atomgraph.core.MediaType.APPLICATION_RDF_XML_TYPE);

        /*
        if (resp.getStatusInfo().getFamily().equals(Status.Family.REDIRECTION))
            // redirect to Location
            if (resp.getHeaders().containsKey(HttpHeaders.LOCATION))
            {
                URI location = URI.create(resp.getHeaders().getFirst(HttpHeaders.LOCATION));
                // TO-DO: use StateBuilder?
                UriBuilder uriBuilder = getUriInfo().getBaseUriBuilder().
                        queryParam(GC.uri.getLocalName(), location);
                if (getMediaType() != null) uriBuilder.queryParam(GC.accept.getLocalName(), getMediaType());
                if (getMode() != null) uriBuilder.queryParam(GC.accept.getLocalName(), getMode());
                if (getForClass() != null) uriBuilder.queryParam(GC.forClass.getLocalName(), getForClass());
                
                return Response.seeOther(uriBuilder.build()).build();
            }
        */
        
        if (resp.getStatusInfo().getFamily().equals(Status.Family.CLIENT_ERROR))
        {
            // forward WWW-Authenticate response header
            if (resp.getHeaders().containsKey(HttpHeaders.WWW_AUTHENTICATE))
            {
                Header wwwAuthHeader = new BasicHeader(HttpHeaders.WWW_AUTHENTICATE, resp.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
                String realm = null;
                for (HeaderElement element : wwwAuthHeader.getElements())
                    if (element.getName().equals("Basic realm")) realm = element.getValue();

                // TO-DO: improve handling of missing realm
                if (realm != null) throw new AuthenticationException("Login is required", realm);
            }

            throw new ClientErrorException(resp);
        }

        if (log.isDebugEnabled()) log.debug("GETing Model from URI: {}", getWebResource().getURI());
        Model description = resp.getEntity(Model.class);

        com.atomgraph.core.model.impl.Response response = com.atomgraph.core.model.impl.Response.fromRequest(getRequest());
        ResponseBuilder bld = response.getResponseBuilder(description,
                response.getVariantListBuilder(getMediaTypes().getWritable(Model.class), new ArrayList(), new ArrayList()).
                        add().build());
        
        // move headers to HypermediaFilter?
        if (resp.getHeaders().get("Link") != null)
            for (String linkValue : resp.getHeaders().get("Link"))
                bld.header("Link", linkValue);
        if (resp.getHeaders().get("Rules") != null)
            for (String linkValue : resp.getHeaders().get("Rules"))
                bld.header("Rules", linkValue);
        
        if (getMediaType() != null) bld.type(getMediaType()); // should do RDF export
        
        return bld.build();
    }

    @POST
    public Model post(Model model)
    {
        if (log.isDebugEnabled()) log.debug("POSTing Model to URI: {}", getWebResource().getURI());
        return getWebResource().type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
                post(Model.class, model);

    }

    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @return response
     */
    @PUT
    public Model put(Model model)
    {
        if (log.isDebugEnabled()) log.debug("PUTting Model to URI: {}", getWebResource().getURI());
        return getWebResource().type(com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE).
            put(Model.class, model);
    }
    
    @DELETE
    public void delete()
    {
        if (log.isDebugEnabled()) log.debug("DELETEing Model from URI: {}", getWebResource().getURI());
        getWebResource().delete();
    }

}
