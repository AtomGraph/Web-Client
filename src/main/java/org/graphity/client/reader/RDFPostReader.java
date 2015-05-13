/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.client.reader;

import com.hp.hpl.jena.rdf.model.*;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.util.ReaderWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.graphity.processor.provider.SkolemizingModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS RDF/POST reader.
 * Reads RDF from RDF/POST-encoded request body
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.lsrn.org/semweb/rdfpost.html">RDF/POST Encoding for RDF</a>
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html">Model</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyReader.html">MessageBodyReader</a>
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RDFPostReader extends SkolemizingModelProvider // implements MessageBodyReader<Model>
{
    private static final Logger log = LoggerFactory.getLogger(RDFPostReader.class);
    
    @Context private HttpContext httpContext;
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	// getHttpContext().getRequest().getFormParameters() returns same info, but ordering is lost
        try
        {
            return process(parse(getHttpContext().getRequest().getEntity(String.class),
                    ReaderWriter.getCharset(mediaType).name()));
        }
        catch (URISyntaxException ex)
        {
            if (log.isErrorEnabled()) log.error("URI '{}' has syntax error in request with media type '{}'", ex.getInput(), mediaType);
            throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
        }
    }
        
    public Model parse(String body, String charsetName) throws URISyntaxException
    {
        List<String> keys = new ArrayList<>(), values = new ArrayList<>();

	String[] params = body.split("&");

	for (String param : params)
	{
	    if (log.isTraceEnabled()) log.trace("Parameter: {}", param);
	    
	    String[] array = param.split("=");
	    String key = null;
	    String value = null;

	    try
	    {
		key = URLDecoder.decode(array[0], charsetName);
		if (array.length > 1) value = URLDecoder.decode(array[1], charsetName);
	    } catch (UnsupportedEncodingException ex)
	    {
		if (log.isWarnEnabled()) log.warn("Unsupported encoding", ex);
	    }

            if (value != null) // && key != null
            {
                keys.add(key);
                values.add(value);
            }
	}

        return parse(keys, values);
    }

    public Model parse(List<String> k, List<String> v) throws URISyntaxException
    {
        return new org.graphity.core.riot.lang.RDFPostReader().parse(k, v); // getUriInfo().getBaseUri()
    }
    
    public HttpContext getHttpContext()
    {
	return httpContext;
    }

    
}