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
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.resource.Singleton;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML})
public class ResourceXHTMLWriter implements MessageBodyWriter<Resource>
{
    private static final Logger log = LoggerFactory.getLogger(ResourceXHTMLWriter.class);

    //@Context private ServletContext context;
    @Context private UriInfo uriInfo;

    private URIResolver resolver = DataManager.get(); // OntDataManager.getInstance(); // XML-only resolving is not good enough, needs to work on RDF Models
       
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return Resource.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	/*
	if (bos == null)
	{
	    bos = new ByteArrayOutputStream();
	    resource.getModel().write(bos);
	}
	*/
	//return bos.size(); // is this the right value?
	//return Integer.valueOf(stream.toByteArray().length).longValue();
	return -1;
    }

    @Override
    public void writeTo(Resource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
	log.trace("Writing RDFResource with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);

	try
	{
	    ByteArrayOutputStream modelStream = new ByteArrayOutputStream();
	    resource.getModel().write(modelStream);

	    XSLTBuilder rdf2xhtml = getXSLTBuilder().
		parameter("uri", UriBuilder.fromUri(resource.getURI()).build()).
		parameter("http-headers", httpHeaders.toString()).
		result(new StreamResult(entityStream));
	    
	    if (resource.getSPINResource() != null)
	    {
		if (resource.getSPINResource().isURIResource())
		    rdf2xhtml.parameter("query-uri", UriBuilder.fromPath(resource.getSPINResource().getURI()).build());
		if (resource.getSPINResource().isAnon())
		    rdf2xhtml.parameter("query-bnode-id", resource.getSPINResource().getId().getLabelString());
		
		ByteArrayOutputStream queryStream = new ByteArrayOutputStream();
		//resource.getQueryBuilder().buildSPIN().getModel().write(queryStream); // don't need the whole ontology
		ResourceUtils.reachableClosure(resource.getQueryBuilder().buildSPIN()).write(queryStream);
		
		rdf2xhtml.parameter("query-model", new StreamSource(new ByteArrayInputStream(queryStream.toByteArray())));
		queryStream.close();
	    }

	    XSLTBuilder.fromStylesheet(getStylesheet("group-triples.xsl")).
		document(new ByteArrayInputStream(modelStream.toByteArray())).
		result(rdf2xhtml).
		transform();
	    
	    modelStream.close();
	    
	    //getXSLTBuilder(resource).transform(entityStream); // no preprocessing
	}
	catch (TransformerException ex)
	{
	    log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }
    
    public XSLTBuilder getXSLTBuilder() throws TransformerConfigurationException
    {
	XSLTBuilder rdf2xhtml = XSLTBuilder.fromStylesheet(getStylesheet("Resource.xsl")).
	    resolver(resolver).
	    parameter("base-uri", uriInfo.getBaseUri()).
	    parameter("absolute-path", uriInfo.getAbsolutePath()); // is base URI necessary?

	// set all query string &parameters as XSLT $parameters (using the first value)
	/*
	Iterator<String> it = resource.getUriInfo().getQueryParameters().keySet().iterator();
	while (it.hasNext())
	{
	    String key = it.next();
	    rdf2xhtml.parameter(key, resource.getUriInfo().getQueryParameters().getFirst(key));
	}
	*/ 

	if (uriInfo.getQueryParameters().getFirst("service-uri") != null)
	    rdf2xhtml.parameter("service-uri", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("service-uri")).build());
	if (uriInfo.getQueryParameters().getFirst("query") != null)
	    rdf2xhtml.parameter("query", uriInfo.getQueryParameters().getFirst("query"));
	if (uriInfo.getQueryParameters().getFirst("mode") != null)
	    rdf2xhtml.parameter("mode", UriBuilder.fromUri(uriInfo.getQueryParameters().getFirst("mode")).build());
	if (uriInfo.getQueryParameters().getFirst("lang") != null)
	    rdf2xhtml.parameter("lang", uriInfo.getQueryParameters().getFirst("lang"));
	if (uriInfo.getQueryParameters().getFirst("offset") != null)
	    rdf2xhtml.parameter("offset", Integer.parseInt(uriInfo.getQueryParameters().getFirst("offset")));
	if (uriInfo.getQueryParameters().getFirst("limit") != null)
	    rdf2xhtml.parameter("limit", Integer.parseInt(uriInfo.getQueryParameters().getFirst("limit")));
	if (uriInfo.getQueryParameters().getFirst("order-by") != null)
	    rdf2xhtml.parameter("order-by", uriInfo.getQueryParameters().getFirst("order-by"));
	if (uriInfo.getQueryParameters().getFirst("desc") != null)
	    rdf2xhtml.parameter("desc", Boolean.parseBoolean(uriInfo.getQueryParameters().getFirst("desc")));

	if (uriInfo.getQueryParameters().getFirst("rdf:type") != null)
	    rdf2xhtml.parameter("{" + RDF.getURI()+ "}type", uriInfo.getQueryParameters().getFirst("rdf:type"));
	    
	return rdf2xhtml;
    }
    
    public Source getStylesheet(String filename)
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	try
	{
	    URL xsltUrl = this.getClass().getResource(filename);  //context.getResource(path);
	    if (xsltUrl == null) throw new FileNotFoundException();
	    String xsltUri = xsltUrl.toURI().toString();
	    log.debug("XSLT stylesheet URI: {}", xsltUri);
	    return new StreamSource(xsltUri);
	}
	catch (IOException ex)
	{
	    log.error("Cannot read internal XSLT stylesheet resource: {}", filename);
	    return null;
	}
	catch (URISyntaxException ex)
	{
	    log.error("Cannot read internal XSLT stylesheet resource: {}", filename);
	    return null;
	}
	//return null;
    }
    
}
