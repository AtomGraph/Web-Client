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
import com.atomgraph.client.util.HTMLMediaTypePredicate;
import com.atomgraph.client.vocabulary.AC;
import java.net.URI;
import java.util.ArrayList;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import com.atomgraph.core.exception.BadGatewayException;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.ModelUtils;
import com.atomgraph.core.util.ResultSetUtils;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.uri.UriComponent;
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
    
    private final UriInfo uriInfo;
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final MediaTypes mediaTypes;
    private final MediaType accept;
    private final MediaType[] readableMediaTypes;
    private final Client client;
    private final WebTarget webTarget;
    private final URI endpoint;
    private final String query;

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
     * @param query SPARQL query
     * @param accept response media type
     * @param mode layout mode
     * @param client HTTP client
     * @param httpServletRequest HTTP request
     */
    @Inject
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("endpoint") URI endpoint, @QueryParam("query") String query, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode,
            Client client, @Context HttpServletRequest httpServletRequest)
    {
        this.uriInfo = uriInfo;
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        this.endpoint = endpoint;
        this.query = query;
        this.accept = accept;
        this.client = client;
        List<jakarta.ws.rs.core.MediaType> readableMediaTypesList = new ArrayList<>();
        readableMediaTypesList.addAll(mediaTypes.getReadable(Model.class));
        readableMediaTypesList.addAll(mediaTypes.getReadable(ResultSet.class));
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
        if (target == null)
        {
            // if SPARQL endpoint and query are provided, build a SPARQL Protocol URI and then redirect to a URI that proxies it
            if (getEndpoint() != null && getQuery() != null)
            {
                if (log.isDebugEnabled()) log.debug("Redirecting from endpoint/query URL to a proxied URL");
                String encodedQuery = UriComponent.encode(getQuery(), UriComponent.Type.UNRESERVED); // manually encode query string because UriBuilder::build will complain about {}
                URI sparqlUrl = UriBuilder.fromUri(getEndpoint()).queryParam(AC.query.getLocalName(), encodedQuery).build();
                String encodedSparqlUrl = UriComponent.encode(sparqlUrl.toString(), UriComponent.Type.UNRESERVED); // manually encode URL
                URI uri = getUriInfo().getBaseUriBuilder().queryParam(AC.uri.getLocalName(), encodedSparqlUrl).build();
                
                return Response.seeOther(uri).build();
            }
            
            throw new NotFoundException("Resource URI not supplied");
        }
        
        return get(target, getBuilder(target));
    }
    
    public Invocation.Builder getBuilder(WebTarget target)
    {
        return target.request(getReadableMediaTypes());
    }
    
    public Response get(WebTarget target, Invocation.Builder builder)
    {
        if (target == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436

        try (Response cr = builder.get())
        {
            // special case for http <-> https 301/303 redirection
            if ((cr.getStatusInfo().toEnum().equals(Status.SEE_OTHER) || cr.getStatusInfo().toEnum().equals(Status.MOVED_PERMANENTLY)) &&
                ((target.getUri().getScheme().equals("http")  && cr.getLocation().getScheme().equals("https")) ||
                (target.getUri().getScheme().equals("https") && cr.getLocation().getScheme().equals("http"))) )
                    return get(getClient().target(cr.getLocation()));

            cr.getHeaders().putSingle(ModelProvider.REQUEST_URI_HEADER, target.getUri().toString()); // provide a base URI hint to ModelProvider

            if (log.isDebugEnabled()) log.debug("GETing response from URI: {}", target.getUri());

            Response response = getResponse(cr);
            
            List<Object> linkValues = cr.getHeaders().get(HttpHeaders.LINK);
            if (linkValues != null) setLinks(linkValues, response);
            
            return response;
        }
        catch (MessageBodyProviderNotFoundException ex)
        {
            if (log.isWarnEnabled()) log.debug("Dereferenced URI {} returned non-RDF media type", ex);
            throw new NotAcceptableException(ex);
        }
        catch (ProcessingException ex)
        {
            if (log.isWarnEnabled()) log.debug("Could not dereference URI: {}", webTarget.getUri());
            throw new BadGatewayException(ex);
        }
    }
    
    public Response getResponse(Response clientResponse)
    {
        MediaType formatType = new MediaType(clientResponse.getMediaType().getType(), clientResponse.getMediaType().getSubtype()); // discard charset param
        Lang lang = RDFLanguages.contentTypeToLang(formatType.toString());
        
        // check if we got SPARQL results first
        if (lang != null && ResultSetReaderRegistry.isRegistered(lang))
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
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariants(getWritableMediaTypes(Model.class),
                getLanguages(),
                getEncodings());

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                model,
                null,
                new EntityTag(Long.toHexString(ModelUtils.hashModel(model))),
                variants,
                new HTMLMediaTypePredicate()).
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
        
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariants(getWritableMediaTypes(ResultSet.class),
                getLanguages(),
                getEncodings());

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                resultSet,
                null,
                new EntityTag(Long.toHexString(hash)),
                variants,
                new HTMLMediaTypePredicate()).
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
            accept(getMediaTypes().getReadable(Model.class).toArray(jakarta.ws.rs.core.MediaType[]::new)).
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
            accept(getMediaTypes().getReadable(Model.class).toArray(jakarta.ws.rs.core.MediaType[]::new)).
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
            accept(getMediaTypes().getReadable(Model.class).toArray(jakarta.ws.rs.core.MediaType[]::new)).
            delete(Response.class);
    }
    
    public UriInfo getUriInfo()
    {
        return uriInfo;
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

    public final URI getEndpoint()
    {
        return endpoint;
    }
    
    public final String getQuery()
    {
        return query;
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
