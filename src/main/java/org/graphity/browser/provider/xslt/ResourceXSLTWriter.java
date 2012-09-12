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

package org.graphity.browser.provider.xslt;

import com.hp.hpl.jena.util.ResourceUtils;
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
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.browser.Resource;
import org.graphity.util.XSLTBuilder;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML})
public class ResourceXSLTWriter implements MessageBodyWriter<Resource>
{
    private static final Logger log = LoggerFactory.getLogger(ResourceXSLTWriter.class);

    private XSLTBuilder builder = null;
	
    @Context private UriInfo uriInfo;

    public ResourceXSLTWriter(XSLTBuilder builder) throws TransformerConfigurationException
    {
	this.builder = builder;
    }

    public ResourceXSLTWriter(Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	this(XSLTBuilder.fromStylesheet(stylesheet).resolver(resolver));
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return Resource.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return -1;
    }

    @Override
    public void writeTo(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
	if (log.isTraceEnabled()) log.trace("Writing Resource with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);

	try
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    resource.getModel().write(baos, WebContent.langRDFXML);

	    builder.getTransformer().clearParameters(); // remove previously set param values

	    builder.document(new ByteArrayInputStream(baos.toByteArray())).
		parameter("base-uri", uriInfo.getBaseUri()).
		parameter("absolute-path", uriInfo.getAbsolutePath()).
		parameter("http-headers", httpHeaders.toString()).
		result(new StreamResult(entityStream));
	    
	    if (resource.getQueryBuilder() != null)
	    {
		ByteArrayOutputStream queryStream = new ByteArrayOutputStream();
		//resource.getQueryBuilder().buildSPIN().getModel().write(queryStream); // don't need the whole ontology
		ResourceUtils.reachableClosure(resource.getQueryBuilder().buildSPIN()).write(queryStream);
		
		builder.parameter("query-model", new StreamSource(new ByteArrayInputStream(queryStream.toByteArray())));
		queryStream.close();
	    }

	    if (uriInfo.getQueryParameters().getFirst("uri") != null)
		builder.parameter("uri", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("uri")).build());
	    if (uriInfo.getQueryParameters().getFirst("endpoint-uri") != null)
		builder.parameter("endpoint-uri", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("endpoint-uri")).build());
	    if (uriInfo.getQueryParameters().getFirst("query") != null)
		builder.parameter("query", uriInfo.getQueryParameters().getFirst("query"));
	    if (uriInfo.getQueryParameters().getFirst("mode") != null)
		builder.parameter("mode", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("mode")).build());
	    if (uriInfo.getQueryParameters().getFirst("lang") != null)
		builder.parameter("lang", uriInfo.getQueryParameters().getFirst("lang"));
	    if (uriInfo.getQueryParameters().getFirst("offset") != null)
		builder.parameter("offset", Integer.parseInt(uriInfo.getQueryParameters().getFirst("offset")));
	    if (uriInfo.getQueryParameters().getFirst("limit") != null)
		builder.parameter("limit", Integer.parseInt(uriInfo.getQueryParameters().getFirst("limit")));
	    if (uriInfo.getQueryParameters().getFirst("order-by") != null)
		builder.parameter("order-by", uriInfo.getQueryParameters().getFirst("order-by"));
	    if (uriInfo.getQueryParameters().getFirst("desc") != null)
		builder.parameter("desc", Boolean.parseBoolean(uriInfo.getQueryParameters().getFirst("desc")));

	    builder.transform();
	    baos.close();
	}
	catch (TransformerException ex)
	{
	    log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }

}
