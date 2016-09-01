/**
 *  Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.writer;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import com.atomgraph.client.util.XSLTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms SPARQL XML results with XSLT stylesheet and writes result to response.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/ResultSet.html">ResultSet</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyWriter.html">MessageBodyWriter</a>
 */
public class ResultSetXSLTWriter implements MessageBodyWriter<ResultSet>
{
    private static final Logger log = LoggerFactory.getLogger(ResultSetXSLTWriter.class);

    private Source stylesheet = null;
    private URIResolver resolver = null;

    /**
     * Constructs from stylesheet source and URI resolver
     * 
     * @param stylesheet the source of the XSLT transformation
     * @param resolver URI resolver to be used in the transformation
     * @throws TransformerConfigurationException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public ResultSetXSLTWriter(Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	if (stylesheet == null) throw new IllegalArgumentException("XSLT stylesheet Source cannot be null");
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.stylesheet = stylesheet;
	this.resolver = resolver;
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return (ResultSet.class.isAssignableFrom(type));
    }

    @Override
    public long getSize(ResultSet results, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return -1;
    }

    @Override
    public void writeTo(ResultSet results, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
	try
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ResultSetFormatter.outputAsXML(baos, results);
	    
	    // create XSLTBuilder per request output to avoid document() caching
	    getXSLTBuilder(new ByteArrayInputStream(baos.toByteArray()),
		    headerMap, entityStream).transform();
	}
	catch (TransformerException ex)
	{
	    log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }

    public URIResolver getURIResolver()
    {
	return resolver;
    }

    public Source getStylesheet()
    {
	return stylesheet;
    }

    public XSLTBuilder getXSLTBuilder(InputStream is, MultivaluedMap<String, Object> headerMap, OutputStream os) throws TransformerConfigurationException
    {
	return XSLTBuilder.fromStylesheet(getStylesheet()).
	    resolver(getURIResolver()).
	    document(is).
	    result(new StreamResult(os));
    }
}
