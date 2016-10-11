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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class LinkedDataClient
{

    private final WebResource webResource;
    
    protected LinkedDataClient(WebResource webResource)
    {
        if (webResource == null) throw new IllegalArgumentException("WebResource cannot be null");
        
        this.webResource = webResource;        
    }
    
    public static LinkedDataClient create(WebResource webResource)
    {
        return new LinkedDataClient(webResource);
    }
    
    public ClientResponse get(MultivaluedMap<String, Object> headers, MediaType... acceptedTypes)
    {
        if (acceptedTypes == null) throw new IllegalArgumentException("MediaType... cannot be null");

        WebResource.Builder builder = getWebResource().getRequestBuilder();
        
        if (headers != null)
        {
            /*
            Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
            while (it.hasNext())
            {
                Entry<String, Object> header = it.next();
                builder.header(header.getKey(), header.getValue());
            }
            */
        }
        
        ClientResponse response = null;
        try
        {
            return builder.accept(acceptedTypes).
                get(ClientResponse.class);
        }
        finally
        {
            if (response != null) response.close();
        }
    }
    
    public WebResource getWebResource()
    {
        return webResource;
    }
    
}
