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

package com.atomgraph.client.filter.response;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.core.util.StateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewrites response Model URIs by putting them into a query parameter. That leads requests back to the Client.
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
@Provider
public class URIRewriteFilter implements ContainerResponseFilter // extends ClientFilter 
{
    private static final Logger log = LoggerFactory.getLogger(URIRewriteFilter.class);


    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (response == null) throw new IllegalArgumentException("ContainerResponse cannot be null");

        if (response.getMediaType() == null ||
                !(response.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE) ||
                response.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)))
            return response;
        
        Model model = getModel(response.getEntity());
        if (model == null) return response;

        //response.setEntity(rewrite(model, request, getBaseUriBuilder(request)), Model.class);
        response.setEntity(addStates(model.createResource(getBaseUri(request).toString()), model), Model.class);
        
        return response;
    }
    
    public URI getBaseUri(ContainerRequest request)
    {
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        
        return request.getBaseUri();
    }
    
    public Model addStates(Resource baseUri, Model model)
    {
        if (baseUri == null) throw new IllegalArgumentException("Resource cannot be null");
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        
        ResIterator subjectIt = model.listSubjects();
        try
        {
            while (subjectIt.hasNext())
            {
                Resource resource = subjectIt.next();
                if (resource.isURIResource()) addState(baseUri, resource);
            }
        }
        finally
        {
            subjectIt.close();
        }

        NodeIterator objectIt = model.listObjects();
        try
        {
            while (objectIt.hasNext())
            {
                RDFNode node = objectIt.next();
                if (node.isURIResource())
                    addState(baseUri, node.asResource());
            }
        }
        finally
        {
            objectIt.close();
        }
        
        return model;
    }

    public void addState(Resource baseUri, Resource resource)
    {
        if (baseUri == null) throw new IllegalArgumentException("Resource cannot be null");
        if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

        StateBuilder.fromResource(baseUri).
            property(AC.uri, resource).build();
    }
    
    public Model getModel(Object entity)
    {
        if (entity instanceof Model) return (Model)entity;
        
        return null;
    }
    
}
