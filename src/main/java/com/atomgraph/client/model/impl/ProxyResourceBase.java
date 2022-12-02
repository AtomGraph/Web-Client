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
import com.atomgraph.core.io.ResultSetProvider;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.ModelUtils;
import com.atomgraph.core.util.ResultSetUtils;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Variant;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
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
    
    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0"; // impersonate Firefox

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
        this.readableMediaTypes = readableMediaTypesList.toArray(MediaType[]::new);
        
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
            // special case for http <-> https 301/303 redirection
            if ((cr.getStatusInfo().toEnum().equals(Status.SEE_OTHER) || cr.getStatusInfo().toEnum().equals(Status.MOVED_PERMANENTLY)) &&
                ((target.getUri().getScheme().equals("http")  && cr.getLocation().getScheme().equals("https")) ||
                (target.getUri().getScheme().equals("https") && cr.getLocation().getScheme().equals("http"))) )
                    return get(getClient().target(cr.getLocation()));

            cr.getHeaders().putSingle(DatasetProvider.REQUEST_URI_HEADER, webTarget.getUri().toString()); // provide a base URI hint to ModelProvider
            cr.getHeaders().putSingle(HttpHeaders.USER_AGENT, USER_AGENT);

            if (log.isDebugEnabled()) log.debug("GETing Model from URI: {}", webTarget.getUri());

            Response response = getResponse(cr);
            
            List<Object> linkValues = cr.getHeaders().get(HttpHeaders.LINK);
            if (linkValues != null) setLinks(linkValues, response);
                
            return response;
        }
        catch (ProcessingException ex)
        {
            if (log.isErrorEnabled()) log.debug("Could not dereference URI: {}", webTarget.getUri());
            throw new BadGatewayException(ex);
        }
    }
    
    public Response getResponse(Response clientResponse)
    {
        // check if we got SPARQL results first
        if (ResultSetProvider.isResultSetType(clientResponse.getMediaType()))
        {
            ResultSetRewindable results = clientResponse.readEntity(ResultSetRewindable.class);
            return getResponse(results);
        }
        
        // fallback to RDF graph
        Model description = clientResponse.readEntity(Model.class);
        return getResponse(description);
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
                getLanguages(),
                getEncodings()).
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
     * Returns response for the given SPARQL results.
     * 
     * @param resultSet SPARQL results
     * @return response object
     */
    public Response getResponse(ResultSetRewindable resultSet)
    {
        long hash = ResultSetUtils.hashResultSet(resultSet);
        resultSet.reset();
        
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariantListBuilder(getWritableMediaTypes(ResultSet.class),
                getLanguages(),
                getEncodings()).
            add().
            build();

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                resultSet,
                null,
                new EntityTag(Long.toHexString(hash)),
                variants).
            getResponseBuilder().
            build();
    }
    
    /**
     * Forwards <code>Link</code> header values.
     * 
     * @param linkValues header values
     * @param response proxy response
     * @return the response
     */
    protected Response setLinks(List<Object> linkValues, Response response)
    {
        linkValues.forEach(linkValue -> {
            response.getHeaders().add(HttpHeaders.LINK, linkValue);
        });
        
        return response;
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
            accept(getMediaTypes().getReadable(Model.class).toArray(javax.ws.rs.core.MediaType[]::new)).
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
            accept(getMediaTypes().getReadable(Model.class).toArray(javax.ws.rs.core.MediaType[]::new)).
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
            accept(getMediaTypes().getReadable(Model.class).toArray(javax.ws.rs.core.MediaType[]::new)).
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
    
    /**
     * Returns a list of supported languages.
     * 
     * @return list of languages
     */
    public List<Locale> getLanguages()
    {
        return new ArrayList<>();
    }

    /**
     * Returns a list of supported HTTP encodings.
     * Note: this is different from content encodings such as UTF-8.
     * 
     * @return list of encodings
     */
    public List<String> getEncodings()
    {
        return new ArrayList<>();
    }
    
}
