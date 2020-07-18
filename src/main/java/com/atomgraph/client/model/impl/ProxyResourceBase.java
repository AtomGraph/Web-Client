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
import com.atomgraph.core.client.LinkedDataClient;
import com.atomgraph.client.exception.ClientErrorException;
import com.atomgraph.client.filter.RedirectFilter;
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.core.io.DatasetProvider;
import com.atomgraph.core.model.Resource;
import com.atomgraph.core.util.Link;
import com.atomgraph.core.util.ModelUtils;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
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
    private final WebTarget webTarget;
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
    @Inject
    public ProxyResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, MediaTypes mediaTypes,
            @QueryParam("uri") URI uri, @QueryParam("endpoint") URI endpoint, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode,
            Client client, @Context HttpServletRequest httpServletRequest)
    {
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.mediaTypes = mediaTypes;
        this.accept = accept;
        List<javax.ws.rs.core.MediaType> readableMediaTypesList = new ArrayList<>();
        //readableMediaTypesList.addAll(mediaTypes.getReadable(Dataset.class));
        readableMediaTypesList.addAll(mediaTypes.getReadable(Model.class));
        this.readableMediaTypes = readableMediaTypesList.toArray(new MediaType[readableMediaTypesList.size()]);
        
        // client.setFollowRedirects(true); // doesn't work: https://stackoverflow.com/questions/29955951/jersey-is-not-following-302-redirects/29957936
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
            linkedDataClient = LinkedDataClient.create(webTarget, mediaTypes);
        }
        else
        {
            webTarget = null;
            linkedDataClient = null;
        }
        this.httpServletRequest = httpServletRequest;
    }
    
    @Override
    public URI getURI()
    {
        return getWebTarget().getUri();
    }
    
    public Response getResponse(WebTarget webResource, HttpHeaders httpHeaders)
    {
        return webResource.request(getReadableMediaTypes()).get();
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
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        try (Response cr = getResponse(getWebTarget(), getHttpHeaders()))
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

                if (cr.hasEntity())
                    throw new ClientErrorException(cr, DatasetFactory.create(cr.readEntity(Model.class)));
                else 
                    throw new ClientErrorException(cr);
            }

            cr.getHeaders().putSingle(DatasetProvider.REQUEST_URI_HEADER, getWebTarget().getUri().toString()); // provide a base URI hint to ModelProvider/DatasetProvider

            if (log.isDebugEnabled()) log.debug("GETing Dataset from URI: {}", getWebTarget().getUri());

            if (cr.getHeaders().containsKey(HttpHeaders.LINK)) setLinkAttributes(cr.getHeaders().get(HttpHeaders.LINK));

            Model description = cr.readEntity(Model.class);

            return getResponse(DatasetFactory.create(description));
        }
    }

    /**
     * Returns response for the given RDF dataset.
     * 
     * @param dataset RDF dataset
     * @return response object
     */
    public Response getResponse(Dataset dataset)
    {
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariantListBuilder(getWritableMediaTypes(Dataset.class),
                new ArrayList(),
                new ArrayList()).
            add().
            build();

        com.atomgraph.core.model.impl.Response response = new com.atomgraph.core.model.impl.Response(getRequest(),
                dataset,
                null,
                new EntityTag(Long.toHexString(com.atomgraph.core.model.impl.Response.hashDataset(dataset))),
                variants);

        Variant variant = getRequest().selectVariant(variants);
        if (variant == null || MediaTypes.isTriples(variant.getMediaType())) // fallback to Model
        {
            variants = com.atomgraph.core.model.impl.Response.getVariantListBuilder(getWritableMediaTypes(Model.class),
                    new ArrayList(),
                    new ArrayList()).
                add().
                build();
            
            response = new com.atomgraph.core.model.impl.Response(getRequest(),
                dataset.getDefaultModel(),
                    null,
                new EntityTag(Long.toHexString(ModelUtils.hashModel(dataset.getDefaultModel()))),
                variants);
        }

        return response.getResponseBuilder().build();
    }
    
    /**
     * Parses <code>Link</code> header values. If they belong to the LDT namespace, they are set as request attributes.
     * The attributes are used by the {@link com.atomgraph.client.writer.DatasetXSLTWriter}.
     * 
     * @param linkValues header values
     */
    protected void setLinkAttributes(List<Object> linkValues)
    {
        for (Object linkValue : linkValues)
        {
            try
            {
                Link link = Link.valueOf(linkValue.toString());
                if (link.getRel().equals(LDT.ontology.getURI())) getHttpServletRequest().setAttribute(LDT.ontology.getURI(), link.getHref());
                if (link.getRel().equals(LDT.base.getURI())) getHttpServletRequest().setAttribute(LDT.base.getURI(), link.getHref());
                if (link.getRel().equals(LDT.template.getURI())) getHttpServletRequest().setAttribute(LDT.template.getURI(), link.getHref());
            }
            catch (URISyntaxException ex)
            {
                if (log.isErrorEnabled()) log.debug("Could parse Link URI: {}", ex.getInput());
            }
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
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        if (log.isDebugEnabled()) log.debug("POSTing Dataset to URI: {}", getWebTarget().getUri());
        return getWebTarget().request().
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                post(Entity.entity(dataset, com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE));
        
//        Response.ResponseBuilder rb = Response.status(cr.getStatusInfo());
//        if (cr.hasEntity()) rb.entity(cr.readEntity(Dataset.class)); // cr.getEntityInputStream()
//        return rb.build();
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
        if (getWebTarget() == null) throw new NotFoundException("Resource URI not supplied"); // cannot throw Exception in constructor: https://github.com/eclipse-ee4j/jersey/issues/4436
        
        if (log.isDebugEnabled()) log.debug("PUTting Dataset to URI: {}", getWebTarget().getUri());
        return getWebTarget().request().
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                put(Entity.entity(dataset, com.atomgraph.core.MediaType.TEXT_NTRIPLES_TYPE));
        
//        ResponseBuilder rb = Response.status(cr.getStatusInfo());
//        if (cr.hasEntity()) rb.entity(cr.getEntity(Dataset.class)); // cr.getEntityInputStream()
//        return rb.build();
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
                accept(getMediaTypes().getReadable(Dataset.class).toArray(new javax.ws.rs.core.MediaType[0])).
                delete(Response.class);
        
//        ResponseBuilder rb = Response.status(cr.getStatusInfo());
//        if (cr.hasEntity()) rb.entity(cr.getEntity(Dataset.class)); // cr.getEntityInputStream()
//        return rb.build();
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
    
    public LinkedDataClient getLinkedDataClient()
    {
        return linkedDataClient;
    }
    
    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }
    
}
