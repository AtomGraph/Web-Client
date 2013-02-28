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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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

    private Source stylesheet = null;
    private URIResolver resolver = null;
	
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    
    /**
     * Constructs from stylesheet source and URI resolver
     * 
     * @param stylesheet the source of the XSLT transformation
     * @param resolver URI resolver to be used in the transformation
     * @throws TransformerConfigurationException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public ModelXSLTWriter(Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	if (stylesheet == null) throw new IllegalArgumentException("XSLT stylesheet Source cannot be null");
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.stylesheet = stylesheet;
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
	    OntResource ontResource = (OntResource)uriInfo.getMatchedResources().get(0);
	    if (log.isDebugEnabled()) log.debug("Matched Resource: {}", ontResource);

	    // create XSLTBuilder per request output to avoid document() caching
	    XSLTBuilder builder = XSLTBuilder.fromStylesheet(stylesheet).
		resolver(resolver).
		document(new ByteArrayInputStream(baos.toByteArray())).
		parameter("base-uri", uriInfo.getBaseUri()).
		parameter("absolute-path", uriInfo.getAbsolutePath()).
		parameter("request-uri", uriInfo.getRequestUri()).
		parameter("http-headers", headerMap.toString()).
		parameter("ont-model", getSource(ontResource.getOntModel())). // $ont-model from the current Resource
		result(new StreamResult(entityStream));

	    if (!httpHeaders.getAcceptableLanguages().isEmpty())
		builder.parameter("lang", httpHeaders.getAcceptableLanguages().get(0).toLanguageTag());
	    if (uriInfo.getQueryParameters().getFirst("mode") != null)
		builder.parameter("mode", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("mode")).build());
	    if (uriInfo.getQueryParameters().getFirst("query") != null)
		builder.parameter("query", uriInfo.getQueryParameters().getFirst("query"));

	    builder.transform();
	    baos.close();
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }

    public Source getSource(OntModel ontModel)
    {
	if (log.isDebugEnabled()) log.debug("Number of OntModel stmts read: {}", ontModel.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ontModel.write(stream);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }

}