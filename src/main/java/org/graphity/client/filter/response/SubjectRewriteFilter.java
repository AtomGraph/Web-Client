/*
 * Copyright 2016 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.client.filter.response;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import org.graphity.client.vocabulary.GC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewrites response Model URIs by putting them into a query parameter. That leads requests back to the Client.
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class SubjectRewriteFilter extends ClientFilter implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger(SubjectRewriteFilter.class);


    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (response == null) throw new IllegalArgumentException("ContainerResponse cannot be null");

        Model model = getModel(response.getEntity());
        if (model == null) return response;

        response.setEntity(rewrite(model, request.getBaseUriBuilder()), Model.class);
        
        return response;
    }
    
    Model rewrite(Model model, UriBuilder uriBuilder)
    {
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        if (uriBuilder == null) throw new IllegalArgumentException("UriBuilder cannot be null");
        
        ResIterator it = model.listSubjects();
        try
        {
            while (it.hasNext())
            {
                Resource resource = it.next();
                if (resource.isURIResource())
                {
                    URI newURI = uriBuilder.clone().queryParam(GC.uri.getLocalName(),
                            UriComponent.encode(resource.getURI(), UriComponent.Type.UNRESERVED)).
                            build();
                    ResourceUtils.renameResource(resource, newURI.toString());
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return model;
    }
    
    public Model getModel(Object entity)
    {
        if (entity instanceof Model) return (Model)entity;
        
        return null;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException
    {
        ClientResponse response = getNext().handle(request);

        /*
        if (response.hasEntity())
        {
            Model model = response.getEntity(Model.class);
            rewrite(model, UriBuilder.fromUri(request.getURI()));
            //InputStream stream = response.getEntityInputStream();            
            //response.setEntityInputStream(model.geti);
        }
        */
        
        return response;
    }
    
}
