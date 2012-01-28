/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Adopted from Apache Stanbol project http://incubator.apache.org/stanbol/
 * Class org.apache.stanbol.reasoners.web.writers.JenaModelWriter
 * https://github.com/apache/stanbol/blob/trunk/reasoners/web/src/main/java/org/apache/stanbol/reasoners/web/writers/JenaModelWriter.java
 */

package org.graphity.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Writer for jena Model
 * 
 * @author enridaga
 *
 */
@Provider
@Produces({"application/rdf+xml", "text/turtle", "text/n3", "text/plain", "application/turtle"})
public class ModelWriter implements MessageBodyWriter<Model>
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Model.class.isAssignableFrom(type);
    }

    private ByteArrayOutputStream stream = null;

    @Override
    public long getSize(Model t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType)
    {
        log.debug("Called size of item");
        stream = toStream(t, mediaType.toString());
        log.debug("Returning {} bytes", stream.size());
        return Integer.valueOf(stream.toByteArray().length).longValue();
    }

    public ByteArrayOutputStream toStream(Model t, String mediaType) {
        log.debug("Serializing model to {}. Statements are {}", mediaType, t.listStatements().toSet().size());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (mediaType.equals("application/rdf+xml")) {
            t.write(stream);
        } else if (mediaType.equals("application/turtle")) {
            // t.write(stream, "TURTLE");
            RDFWriter writer = t.getWriter("TURTLE");
            log.debug("Writer for TURTLE: {}", writer);
            writer.write(t, stream, null);
        } else if (mediaType.equals("text/turtle")) {
            t.write(stream, "TURTLE");
        } else if (mediaType.equals("text/plain")) {
            t.write(stream, "TURTLE");
        } else if (mediaType.equals("text/n3")) {
            t.write(stream, "N3");
        }
        if(log.isDebugEnabled()){
            log.debug("Written {} bytes to stream", stream.toByteArray().length);
        }
        return stream;
    }

    @Override
    public void writeTo(Model t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String,Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        if (stream == null) {
            toStream(t, mediaType.toString()).writeTo(entityStream);
        } else {
            stream.writeTo(entityStream);
            stream = null;
        }
    }

}
