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
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.resource.Singleton;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.platform.provider.ModelProvider;
import org.graphity.util.XSLTBuilder;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms RDF with XSLT stylesheet and writes XHTML/XML result to response
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html">Model</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyWriter.html">MessageBodyWriter</a>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_XHTML_XML})
public class ModelXSLTWriter extends ModelProvider // implements RDFWriter
{
    private static final Logger log = LoggerFactory.getLogger(ModelXSLTWriter.class);

    /**
     * Configuration property for master XSLT stylesheet location (set in web.xml)
     * 
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
     */
    public static final String PROPERTY_XSLT_LOCATION = "org.graphity.client.writer.xslt-location";

    private Source stylesheet = null;
    private URIResolver resolver = null;
	
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    @Context private ResourceConfig resourceConfig;
    
    /**
     * Constructs from stylesheet source and URI resolver
     * 
     * @param stylesheet the source of the XSLT transformation
     * @param resolver URI resolver to be used in the transformation
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public ModelXSLTWriter(Source stylesheet, URIResolver resolver)
    {
	if (stylesheet == null) throw new IllegalArgumentException("XSLT stylesheet Source cannot be null");
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.stylesheet = stylesheet;
	this.resolver = resolver;
    }

    public ModelXSLTWriter(URIResolver resolver)
    {
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.resolver = resolver;
    }

    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
	if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

	try
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    model.write(baos, WebContent.langRDFXML);

	    // injecting Resource to get its OntModel. Is there a better way to do this?
	    OntResource ontResource = (OntResource)getUriInfo().getMatchedResources().get(0);
	    if (log.isDebugEnabled()) log.debug("Matched Resource: {}", ontResource);

	    // create XSLTBuilder per request output to avoid document() caching
	    getXSLTBuilder(new ByteArrayInputStream(baos.toByteArray()),
		    headerMap, ontResource.getOntModel(), entityStream).transform();
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
	catch (FileNotFoundException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not found", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
	catch (URISyntaxException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URI error", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
	catch (MalformedURLException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URL error", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }
    public Source getSource(OntModel ontModel)
    {
	return getSource(ontModel, false);
    }
    
    public Source getSource(OntModel ontModel, boolean writeAll)
    {
	if (log.isDebugEnabled()) log.debug("Number of OntModel stmts read: {}", ontModel.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	if (writeAll) ontModel.writeAll(stream, null, WebContent.langRDFXML);
	else ontModel.write(stream);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }

    /**
     * Provides XML source from filename
     * 
     * @param filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public Source getSource(String filename) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	//if (log.isDebugEnabled()) log.debug("Resource paths used to load Source: {} from filename: {}", getServletContext().getResourcePaths("/"), filename);
	//URL xsltUrl = getServletContext().getResource(filename);
	if (log.isDebugEnabled()) log.debug("ClassLoader {} used to load Source from filename: {}", getClass().getClassLoader(), filename);
	URL xsltUrl = getClass().getClassLoader().getResource(filename);
	if (xsltUrl == null) throw new FileNotFoundException("File '" + filename + "' not found");
	String xsltUri = xsltUrl.toURI().toString();
	if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
	return new StreamSource(xsltUri);
    }

    public URIResolver getURIResolver()
    {
	return resolver;
    }

    public Source getStylesheet()
    {
	return stylesheet;
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public HttpHeaders getHttpHeaders()
    {
	return httpHeaders;
    }

    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
    }

    public XSLTBuilder getXSLTBuilder(InputStream is, MultivaluedMap<String, Object> headerMap, OntModel ontModel, OutputStream os) throws TransformerConfigurationException, FileNotFoundException, URISyntaxException, MalformedURLException
    {
	Source styleSource;
	if (getStylesheet() == null)
	{
	    if (getResourceConfig().getProperty(PROPERTY_XSLT_LOCATION) == null)
		throw new IllegalStateException("XSLT stylesheet source not specified (either in constructor or in web.xml)");

	    styleSource = getSource(getResourceConfig().getProperty(PROPERTY_XSLT_LOCATION).toString());
	}
	else
	    styleSource = getStylesheet();
	
	XSLTBuilder builder = XSLTBuilder.fromStylesheet(styleSource).
	    resolver(getURIResolver()).
	    document(is).
	    parameter("base-uri", getUriInfo().getBaseUri()).
	    parameter("absolute-path", getUriInfo().getAbsolutePath()).
	    parameter("request-uri", getUriInfo().getRequestUri()).
	    parameter("http-headers", headerMap.toString()).
	    parameter("ont-model", getSource(ontModel, true)). // $ont-model from the current Resource (with imports)
	    result(new StreamResult(os));

	if (!getHttpHeaders().getAcceptableLanguages().isEmpty())
	    builder.parameter("lang", getHttpHeaders().getAcceptableLanguages().get(0).toLanguageTag());
	if (getUriInfo().getQueryParameters().getFirst("mode") != null)
	    builder.parameter("mode", UriBuilder.fromUri(getUriInfo().getQueryParameters().getFirst("mode")).build());
	if (getUriInfo().getQueryParameters().getFirst("query") != null)
	    builder.parameter("query", getUriInfo().getQueryParameters().getFirst("query"));
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	    builder.parameter("uri", UriBuilder.fromUri(getUriInfo().getQueryParameters().getFirst("uri")).build());
	
	return builder;
    }

}