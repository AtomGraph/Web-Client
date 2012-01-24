/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.RDFResource;

/**
 *
 * @author Pumba
 */
@Provider
@Produces({"text/html", "application/xml", "application/*+xml", "text/xml"})
public class ResourceXSLTWriter implements MessageBodyWriter<RDFResource>
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
 
    @Context ServletContext context;
    //@Context UriInfo uriInfo;
    private ByteArrayOutputStream bos = null;
    private SAXTransformerFactory stf = (SAXTransformerFactory)TransformerFactory.newInstance();


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return RDFResource.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(RDFResource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
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
    public void writeTo(RDFResource resource, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
	//if (bos == null)
	{
	    bos = new ByteArrayOutputStream();
	    resource.getModel().write(bos);
	}
	// can we avoid buffering here? I guess not...
	try
	{
	    XSLTBuilder.fromStylesheet(context.getResource(XSLT_BASE + "group-triples.xsl").toURI().toString()).
		document(new ByteArrayInputStream(bos.toByteArray())).
		outputProperty(OutputKeys.INDENT, "yes").
		transform(getXSLTBuilder(resource).
		    result(new StreamResult(entityStream)));

	    //getXSLTBuilder(resource).transform(entityStream); // no preprocessing
	}
	catch (URISyntaxException ex)
	{
	    Logger.getLogger(ResourceXSLTWriter.class.getName()).log(Level.SEVERE, null, ex);
	}
	catch (TransformerException ex)
	{
	    Logger.getLogger(ResourceXSLTWriter.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public XSLTBuilder getXSLTBuilder(RDFResource resource) throws TransformerConfigurationException, MalformedURLException, MalformedURLException, MalformedURLException, MalformedURLException, URISyntaxException
    {
	XSLTBuilder builder = XSLTBuilder.fromStylesheet(getStylesheet()).
	    document(new ByteArrayInputStream(bos.toByteArray())).
	    parameter("uri", resource.getURI()).
	    parameter("base-uri", resource.getUriInfo().getBaseUri()).
	    outputProperty(OutputKeys.INDENT, "yes"); // is base uri necessary?
	
	    if (resource.getServiceURI() != null)
		builder.parameter("service-uri", resource.getServiceURI());
	    
	    if (resource.getUriInfo().getQueryParameters().getFirst("view") != null)
		builder.parameter("view", resource.getUriInfo().getQueryParameters().getFirst("view"));
	    
	return builder;
    }
    
    public Source getStylesheet() throws MalformedURLException, URISyntaxException
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	return new StreamSource(context.getResource(XSLT_BASE + "ResourceReadView.xsl").toURI().toString());
    }
    
}
