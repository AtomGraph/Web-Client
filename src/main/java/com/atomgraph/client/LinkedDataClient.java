/*
 * Copyright 2016 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atomgraph.client;

import com.atomgraph.core.MediaType;
import com.atomgraph.core.MediaTypes;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.Iterator;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

/**
 * Linked Data client.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class LinkedDataClient
{

    private final WebResource webResource;
    private final MediaTypes mediaTypes;
    
    protected LinkedDataClient(WebResource webResource, MediaTypes mediaTypes)
    {
        if (webResource == null) throw new IllegalArgumentException("WebResource cannot be null");
        if (mediaTypes == null) throw new IllegalArgumentException("MediaTypes cannot be null");
        
        this.webResource = webResource;
        this.mediaTypes = mediaTypes;
    }
    
    public static LinkedDataClient create(WebResource webResource, MediaTypes mediaTypes)
    {
        return new LinkedDataClient(webResource, mediaTypes);
    }
    
    protected WebResource.Builder setHeaders(WebResource.Builder builder, Map<String, Object> headers)
    {
	if (builder == null) throw new IllegalArgumentException("WebResource.Builder must be not null");
	if (headers == null) throw new IllegalArgumentException("Map<String, Object> must be not null");

        Iterator<Map.Entry<String, Object>> it = headers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, Object> entry = it.next();
            builder.header(entry.getKey(), entry.getValue());
        }
        
        return builder;
    }
    
    public ClientResponse get(javax.ws.rs.core.MediaType[] acceptedTypes)
    {
        return get(acceptedTypes, null);
    }
    
    public ClientResponse get(javax.ws.rs.core.MediaType[] acceptedTypes, Map<String, Object> headers)
    {
        if (acceptedTypes == null) throw new IllegalArgumentException("MediaType... cannot be null");

        WebResource.Builder builder = getWebResource().getRequestBuilder();        
        if (headers != null) setHeaders(builder, headers);
        
        return builder.accept(acceptedTypes).
            get(ClientResponse.class);
    }
    
    public Model get()
    {
        return get(getReadableMediaTypes(Model.class)).getEntity(Model.class);
    }
    
    public ClientResponse post(MediaType contentType, Model model)
    {
        if (contentType == null) throw new IllegalArgumentException("MediaType cannot be null");
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        
        return getWebResource().
            type(contentType).
            post(ClientResponse.class, model);
    }
    
    public void post(Model model)
    {
        ClientResponse cr = null;
        
        try
        {
            cr = post(getDefaultMediaType(), model);
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
    public ClientResponse put(MediaType contentType, Model model)
    {
        if (contentType == null) throw new IllegalArgumentException("MediaType cannot be null");
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        
        return getWebResource().
            type(contentType).
            put(ClientResponse.class, model);
    }
    
    public void put(Model model)
    {
        ClientResponse cr = null;
        
        try
        {
            cr = put(getDefaultMediaType(), model);
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }

    public ClientResponse delete(boolean dummy) // dummy param to avoid method clash
    {
        return getWebResource().delete(ClientResponse.class);
    }
    
    public void delete()
    {
        ClientResponse cr = null;
        
        try
        {
            cr = delete(true);
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
    public WebResource getWebResource()
    {
        return webResource;
    }
    
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
    public javax.ws.rs.core.MediaType[] getReadableMediaTypes(Class clazz)
    {
        return getMediaTypes().getReadable(clazz).toArray(new javax.ws.rs.core.MediaType[0]);
    }

    public MediaType getDefaultMediaType()
    {
        return MediaType.TEXT_NTRIPLES_TYPE;
    }

}
