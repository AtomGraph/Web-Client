/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client.writer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.spi.resource.Singleton;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.graphity.client.util.XSLTBuilder;
import org.graphity.processor.model.MatchedIndividual;
import org.graphity.server.provider.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html">Model</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyWriter.html">MessageBodyWriter</a>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML,MediaType.TEXT_HTML}) // MediaType.APPLICATION_XML ?
public class ModelXSLTWriter extends ModelProvider // implements RDFWriter
{
    private static final Logger log = LoggerFactory.getLogger(ModelXSLTWriter.class);

    public static List<String> RESERVED_PARAMS = Arrays.asList("base-uri", "absolute-path", "request-uri", "http-headers",
            "ont-model", "offset", "limit", "order-by", "desc", "lang", "mode", "uri", "endpoint-uri");

    private final XSLTBuilder builder;
 
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    @Context private ServletContext servletContext;
    @Context private Providers providers;
    
    /**
     * Constructs from XSLT builder.
     * 
     * @param builder XSLT builder for this writer
     * @see org.graphity.client.util.XSLTBuilder
     */
    public ModelXSLTWriter(XSLTBuilder builder)
    {
	if (builder == null) throw new IllegalArgumentException("XSLTBuilder cannot be null");
	this.builder = builder;
    }

    public ModelXSLTWriter()
    {
        this.builder = null;
    }
    
    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
	if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

	try
	{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    model.write(baos, RDFLanguages.RDFXML.getName());

	    // create XSLTBuilder per request output to avoid document() caching
	    getXSLTBuilder(new ByteArrayInputStream(baos.toByteArray()),
		    headerMap, entityStream).transform();
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Model.class.isAssignableFrom(type);
    }
    
    public Source getSource(Model model)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");	
	if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	model.write(stream, null, WebContent.langRDFXML);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }
    
    public Source getSource(OntModel ontModel, boolean writeAll)
    {
	if (!writeAll) return getSource(ontModel);
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");	
	
	if (log.isDebugEnabled()) log.debug("Number of OntModel stmts read: {}", ontModel.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ontModel.writeAll(stream, null, Lang.RDFXML.getName());

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }
    
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public HttpHeaders getHttpHeaders()
    {
	return httpHeaders;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }
    
    public Providers getProviders()
    {
        return providers;
    }

    public XSLTBuilder getXSLTBuilder()
    {
        if (builder != null) return builder;
        else
        {
            ContextResolver<XSLTBuilder> cr = getProviders().getContextResolver(XSLTBuilder.class, null);
            return cr.getContext(XSLTBuilder.class);
        }
    }
    
    public XSLTBuilder getXSLTBuilder(InputStream is, MultivaluedMap<String, Object> headerMap, OutputStream os) throws TransformerConfigurationException
    {
	// injecting Resource to get the final state of its OntModel. Is there a better way to do this?
	Object resource = getUriInfo().getMatchedResources().get(0);
	OntResource ontResource = (OntResource)resource;
	if (log.isDebugEnabled()) log.debug("Matched Resource: {}", ontResource);
	MatchedIndividual match = (MatchedIndividual)resource;

        XSLTBuilder bld = getXSLTBuilder().
	    document(is).
	    parameter("base-uri", getUriInfo().getBaseUri()).
	    parameter("absolute-path", getUriInfo().getAbsolutePath()).
	    parameter("request-uri", getUriInfo().getRequestUri()).
	    parameter("http-headers", headerMap.toString()).
	    parameter("matched-ont-class-uri", URI.create(match.getMatchedOntClass().getURI())).
            parameter("ont-model", getSource(ontResource.getOntModel())). // $ont-model from the current Resource (with imports)
	    result(new StreamResult(os));

	Object contentType = headerMap.getFirst(HttpHeaders.CONTENT_TYPE);
	if (contentType != null)
	{
	    if (log.isDebugEnabled()) log.debug("Writing Model using XSLT media type: {}", contentType);
	    bld.outputProperty(OutputKeys.MEDIA_TYPE, contentType.toString());
	}
	Object contentLanguage = headerMap.getFirst(HttpHeaders.CONTENT_LANGUAGE);
	if (contentLanguage != null)
	{
	    if (log.isDebugEnabled()) log.debug("Writing Model using language: {}", contentLanguage.toString());
	    bld.parameter("lang", contentLanguage.toString());
	}

        // pass HTTP query parameters into XSLT, ignore reserved param names (as params cannot be unset)
	Iterator<Entry<String, List<String>>> it = getUriInfo().getQueryParameters().entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, List<String>> entry = it.next();
            if (!getReservedParameterNames().contains(entry.getKey()))
            {
                bld.parameter(entry.getKey(), entry.getValue().get(0)); // set string value
                if (log.isDebugEnabled()) log.debug("Setting XSLT param \"{}\" from HTTP query string with value: {}", entry.getKey(), entry.getValue().get(0));
            }
            else
                if (log.isDebugEnabled()) log.debug("HTTP query string param \"{}\" is reserved in XSLT writer, ignoring", entry.getKey());
        }

        // override the reserved parameters that need special types
	if (getUriInfo().getQueryParameters().getFirst("mode") != null)
	    bld.parameter("mode", URI.create(getUriInfo().getQueryParameters().getFirst("mode")));
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	    bld.parameter("uri", URI.create(getUriInfo().getQueryParameters().getFirst("uri")));
	if (getUriInfo().getQueryParameters().getFirst("endpoint-uri") != null)
	    bld.parameter("endpoint-uri", URI.create(getUriInfo().getQueryParameters().getFirst("endpoint-uri")));

	return bld;
    }

    public List<String> getReservedParameterNames()
    {
        return RESERVED_PARAMS;
    }
    
}