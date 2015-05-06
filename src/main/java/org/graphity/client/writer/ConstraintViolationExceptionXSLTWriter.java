/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.client.writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.core.model.Resource;
import org.graphity.processor.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML,MediaType.TEXT_HTML}) // MediaType.APPLICATION_XML ?
public class ConstraintViolationExceptionXSLTWriter implements MessageBodyWriter<ConstraintViolationException>
{

    private static final Logger log = LoggerFactory.getLogger(ConstraintViolationExceptionXSLTWriter.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return ConstraintViolationException.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(ConstraintViolationException cve, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return -1;
    }

    @Override
    public void writeTo(ConstraintViolationException cve, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
        Resource match = (Resource)getUriInfo().getMatchedResources().get(0);
        Model model = (Model)match.get().getEntity();
        MessageBodyWriter<Model> mbw = getProviders().getMessageBodyWriter(Model.class, Model.class, annotations, mediaType);
        if (!(mbw instanceof ModelXSLTWriter))
        {
            throw new IllegalStateException("MessageBodyWriter is not ModelXSLTWriter");
        }        
        ModelXSLTWriter mxw = (ModelXSLTWriter)mbw;
            
	if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

        mxw.writeTo(cve.getModel().add(model), type, genericType, annotations, mediaType, headerMap, entityStream);
    }
    
}
