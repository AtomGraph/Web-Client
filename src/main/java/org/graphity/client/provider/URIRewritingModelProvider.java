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

package org.graphity.client.provider;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.vocabulary.GC;
import org.graphity.core.provider.ModelProvider;
import org.graphity.core.util.StateBuilder;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class URIRewritingModelProvider extends ModelProvider
{
    private final UriInfo uriInfo;
    
    public URIRewritingModelProvider(UriInfo uriInfo)
    {
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        this.uriInfo = uriInfo;
    }
    
    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        return process(super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream));
    }
    
    public Model process(Model model)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
        
        //StmtIterator it = model.listStatements();
        //Model newModel = ModelFactory.createDefaultModel();
        ResIterator it = model.listSubjects();
        
        try
        {
            while (it.hasNext())
            {
                //Statement stmt = it.next();
                Resource subject = it.next();

                /*
                if (stmt.getSubject().isURIResource())
                    ResourceUtils.renameResource(stmt.getSubject(), getStateBuilder(stmt.getSubject()).build().getURI());
                if (stmt.getObject().isURIResource())
                    ResourceUtils.renameResource(stmt.getObject().asResource(), getStateBuilder(stmt.getObject().asResource()).build().getURI());
                */
                if (subject.isURIResource())
                    ResourceUtils.renameResource(subject, getStateBuilder(subject).build().getURI());
            }
        }
        finally
        {
            it.close();
        }
        
        return model;
    }
    
    public StateBuilder getStateBuilder(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

        return StateBuilder.fromUri(getUriInfo().getBaseUriBuilder().
                queryParam(GC.uri.getLocalName(), resource.getURI()).
                build(),
            resource.getModel());
    }
    
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
}
