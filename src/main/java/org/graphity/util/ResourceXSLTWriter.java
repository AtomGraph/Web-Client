/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.sun.jersey.spi.resource.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.RDFResource;
import org.xmlresolver.Resolver;

/**
 *
 * @author Pumba
 */
@Provider
@Singleton
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML})
public class ResourceXSLTWriter implements MessageBodyWriter<RDFResource>
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
    //public static final String STYLESHEET_PATH = XSLT_BASE + "ResourceReadView.xsl";
    
    private XSLTBuilder groupTriples = null;
    private XSLTBuilder rdf2xhtml = null;
    private URIResolver resolver = new Resolver();
    private static final Logger logger = Logger.getLogger(ResourceXSLTWriter.class.getName());
    
    @Context ServletContext context;
    //@Context UriInfo uriInfo;
    private ByteArrayOutputStream bos = null;
   

    public ResourceXSLTWriter()
    {
	try
	{
	    groupTriples = XSLTBuilder.fromStylesheet(getStylesheet(XSLT_BASE + "group-triples.xsl")).
		outputProperty(OutputKeys.INDENT, "yes");
	    rdf2xhtml = XSLTBuilder.fromStylesheet(getStylesheet(XSLT_BASE + "ResourceReadView.xsl")).
		//resolver(resolver).
		outputProperty(OutputKeys.INDENT, "yes");
	} catch (TransformerConfigurationException ex)
	{
	    logger.log(Level.SEVERE, null, ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }


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
	    groupTriples.document(new ByteArrayInputStream(bos.toByteArray())).
	    transform(getXSLTBuilder(resource).
		result(new StreamResult(entityStream)));

	    //getXSLTBuilder(resource).transform(entityStream); // no preprocessing
	} catch (TransformerException ex)
	{
	    logger.log(Level.SEVERE, null, ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }
    
    public XSLTBuilder getXSLTBuilder(RDFResource resource)
    {
	    rdf2xhtml.document(new ByteArrayInputStream(bos.toByteArray())).
	    parameter("uri", resource.getURI()).
	    parameter("base-uri", resource.getUriInfo().getBaseUri()); // is base uri necessary?
	    
	    if (resource.getServiceURI() != null)
		rdf2xhtml.parameter("service-uri", resource.getServiceURI());
	    
	    if (resource.getUriInfo().getQueryParameters().getFirst("view") != null)
		rdf2xhtml.parameter("view", resource.getUriInfo().getQueryParameters().getFirst("view"));
	    
	return rdf2xhtml;
    }
    
    public final Source getStylesheet(String path)
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	try
	{
	    URL xsltUrl = context.getResource(path);
	    if (xsltUrl == null) throw new FileNotFoundException("XSLT stylesheet resource not found");
	    String xsltUri = xsltUrl.toURI().toString();
	    logger.log(Level.FINE, "XSLT stylesheet URI: {0}", xsltUri);
	    return new StreamSource(xsltUri);
	}
	catch (IOException ex)
	{
	    logger.log(Level.SEVERE, "Cannot read internal XSLT stylesheet resource: {0}", path);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
	catch (URISyntaxException ex)
	{
	    logger.log(Level.SEVERE, null, ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
	//return null;
    }
    
}
