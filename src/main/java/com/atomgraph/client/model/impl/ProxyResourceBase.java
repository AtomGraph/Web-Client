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

import com.atomgraph.client.MediaTypes;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import com.atomgraph.core.exception.BadGatewayException;
import com.atomgraph.core.io.DatasetProvider;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.ModelUtils;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Variant;
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
    private final Client client;
    private final WebTarget webTarget;
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
    @Inject
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("endpoint") URI endpoint, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode,
            Client client, @Context HttpServletRequest httpServletRequest)
    {
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        this.accept = accept;
        this.client = client;
        List<javax.ws.rs.core.MediaType> readableMediaTypesList = new ArrayList<>();
        readableMediaTypesList.addAll(mediaTypes.getReadable(Model.class));
        this.readableMediaTypes = readableMediaTypesList.toArray(new MediaType[readableMediaTypesList.size()]);
        
        if (uri != null)
        {
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
        
            webTarget = client.target(uri);
            //webTarget.register(new RedirectFilter()); // TO-DO
        }
        else
        {
            webTarget = null;
        }
        this.httpServletRequest = httpServletRequest;
    }
    
    @Override
    public URI getURI()
    {
        return getWebTarget().getUri();
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
        return get(getWebTarget());
    }
    
    public Response get(WebTarget target)
    {
        if (target == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        try (Response cr = target.request(getReadableMediaTypes()).get())
        {
            if (cr.getStatusInfo().getFamily().equals(Status.Family.CLIENT_ERROR))
            {
                // forward WWW-Authenticate response header
                if (cr.getHeaders().containsKey(HttpHeaders.WWW_AUTHENTICATE))
                {
                    String header = cr.getHeaderString(HttpHeaders.WWW_AUTHENTICATE);
                    if (header.contains("Basic realm="))
                    {
                        int realmStart = header.indexOf("\"") + 1;
                        int realmEnd = header.lastIndexOf("\"");

                        String realm = header.substring(realmStart, realmEnd);
                        throw new NotAuthorizedException("Login is required", realm);
                    }
                }

                // throw new ClientErrorException(cr); // this gives "java.lang.IllegalStateException: Entity input stream has already been closed."
                throw new ClientErrorException(cr.getStatus());
            }
            
            // special case for http <-> https 301/303 redirection
            if ((cr.getStatusInfo().toEnum().equals(Status.SEE_OTHER) || cr.getStatusInfo().toEnum().equals(Status.MOVED_PERMANENTLY)) &&
                ((target.getUri().getScheme().equals("http")  && cr.getLocation().getScheme().equals("https")) ||
                (target.getUri().getScheme().equals("https") && cr.getLocation().getScheme().equals("http"))) )
                    return get(getClient().target(cr.getLocation()));

            if (!cr.hasEntity()) throw new ProcessingException("Could not read RDF from '" + webTarget.getUri() + "'");

            cr.getHeaders().putSingle(DatasetProvider.REQUEST_URI_HEADER, webTarget.getUri().toString()); // provide a base URI hint to ModelProvider/DatasetProvider

            if (log.isDebugEnabled()) log.debug("GETing Dataset from URI: {}", webTarget.getUri());

            Model description = cr.readEntity(Model.class);
            Response response = getResponse(description);
            
            if (cr.getHeaders().containsKey(HttpHeaders.LINK)) setLinks(cr.getHeaders().get(HttpHeaders.LINK), response);

            return response;
        }
        catch (ProcessingException ex)
        {
            if (log.isErrorEnabled()) log.debug("Could not dereference URI: {}", webTarget.getUri());
            throw new BadGatewayException(ex);
        }
    }

    /**
     * Returns response for the given RDF model.
     * 
     * @param model RDF model
     * @return response object
     */
    public Response getResponse(Model model)
    {
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariantListBuilder(getWritableMediaTypes(Model.class),
                new ArrayList(),
                new ArrayList()).
            add().
            build();

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                model,
                null,
                new EntityTag(Long.toHexString(ModelUtils.hashModel(model))),
                variants).
            getResponseBuilder().
            build();
    }
    
    /**
     * Parses <code>Link</code> header values. If they belong to the LDT namespace, they are set as request attributes.
     * The attributes are used by the {@link com.atomgraph.client.writer.DatasetXSLTWriter}.
     * 
     * @param linkValues header values
     * @param response proxy response
     */
    protected void setLinks(List<Object> linkValues, Response response)
    {
        for (Object linkValue : linkValues)
        {
            try
            {
                Link link = Link.valueOf(linkValue.toString());
                response.getHeaders().add(HttpHeaders.LINK, link.getUri());
            }
            catch (IllegalArgumentException ex)
            {
                if (log.isWarnEnabled()) log.warn("Could not parse Link URI", ex);
            }
        }
    }
    
    /**
     * Forwards POST request with RDF dataset body and returns RDF response from remote resource.
     * 
     * @param model
     * @return response
     */
    @POST
    @Override
    public Response post(Model model)
    {
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        if (log.isDebugEnabled()) log.debug("POSTing Dataset to URI: {}", getWebTarget().getUri());
        return getWebTarget().request().
            accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
            post(Entity.entity(model, com.atomgraph.core.MediaType.APPLICATION_NTRIPLES_TYPE));
    }

    /**
     * Forwards PUT request with RDF dataset body and returns response from remote resource.
     * 
     * @param model RDF payload
     * @return response
     */
    @PUT
    @Override
    public Response put(Model model)
    {
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        if (log.isDebugEnabled()) log.debug("PUTting Dataset to URI: {}", getWebTarget().getUri());
        return getWebTarget().request().
            accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
            put(Entity.entity(model, com.atomgraph.core.MediaType.APPLICATION_NTRIPLES_TYPE));
    }
    
    /**
     * Forwards DELETE request and returns response from remote resource.
     * @return response
     */
    @DELETE
    @Override
    public Response delete()
    {
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        if (log.isDebugEnabled()) log.debug("DELETEing Dataset from URI: {}", getWebTarget().getUri());
        return getWebTarget().request().
            accept(getMediaTypes().getReadable(Model.class).toArray(new javax.ws.rs.core.MediaType[0])).
            delete(Response.class);
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
    
    public Client getClient()
    {
        return client;
    }
    
    public final WebTarget getWebTarget()
    {
        return webTarget;
    }

    public Request getRequest()
    {
        return request;
    }
    
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }
    
}
