/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.provider;

import com.sun.jersey.spi.resource.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.RDFResource;
import org.graphity.util.XSLTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
@Provider
@Singleton
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML})
public class RDFResourceXSLTWriter implements MessageBodyWriter<RDFResource>
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
    private static final Logger log = LoggerFactory.getLogger(RDFResourceXSLTWriter.class) ;
    
    //private XSLTBuilder groupTriples = null;
    //private XSLTBuilder rdf2xhtml = null;
    // private URIResolver resolver = new Resolver(); // XML-only resolving is not good enough, needs to work on RDF Models
    
    @Context private ServletContext context;
    //@Context private UriInfo uriInfo;
    //private ByteArrayOutputStream baos = null;
   
    /*
    @PostConstruct
    public void init()
    {
	log.debug("@PostConstruct with @Context ServletContext: {}", context);
	try
	{
	    groupTriples = XSLTBuilder.fromStylesheet(getStylesheet(context, XSLT_BASE + "group-triples.xsl")).
		outputProperty(OutputKeys.INDENT, "yes");
	    rdf2xhtml = XSLTBuilder.fromStylesheet(getStylesheet(context, XSLT_BASE + "ResourceReadView.xsl")).
		//resolver(resolver).
		outputProperty(OutputKeys.INDENT, "yes");
	} catch (TransformerConfigurationException ex)
	{
	    log.error("Could not initialize XSLT stylesheet", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
if (groupTriples == null) log.debug("groupTriples == null");
if (rdf2xhtml == null) log.debug("rdf2xhtml == null");
    }
    */

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
	log.trace("Writing RDFResource with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);
	//if (bos == null)
	{
	}
	// can we avoid buffering here? I guess not...
	try
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    resource.getModel().write(baos);

	    XSLTBuilder.fromStylesheet(getStylesheet(context, XSLT_BASE + "group-triples.xsl")).
		document(new ByteArrayInputStream(baos.toByteArray())).
		result(getXSLTBuilder(resource).
		    result(new StreamResult(new OutputStreamWriter(entityStream, "UTF-8")))).
		transform();
	    
	    baos.close();
	    
	    //getXSLTBuilder(resource).transform(entityStream); // no preprocessing
	} catch (TransformerException ex)
	{
	    log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }
    
    public XSLTBuilder getXSLTBuilder(RDFResource resource) throws TransformerConfigurationException
    {
	XSLTBuilder rdf2xhtml = XSLTBuilder.fromStylesheet(getStylesheet(context, XSLT_BASE + "ResourceReadView.xsl")).
	    //document(new ByteArrayInputStream(baos.toByteArray())).
	    parameter("uri", resource.getURI()).
	    parameter("base-uri", resource.getUriInfo().getBaseUri()); // is base uri necessary?
	    
	if (resource.getServiceURI() != null)
	    rdf2xhtml.parameter("service-uri", resource.getServiceURI());

	if (resource.getUriInfo().getQueryParameters().getFirst("view") != null)
	    rdf2xhtml.parameter("view", resource.getUriInfo().getQueryParameters().getFirst("view"));
	    
	return rdf2xhtml;
    }
    
    public Source getStylesheet(ServletContext context, String path)
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	try
	{
	    URL xsltUrl = context.getResource(path);
	    if (xsltUrl == null) throw new FileNotFoundException();
	    String xsltUri = xsltUrl.toURI().toString();
	    log.debug("XSLT stylesheet URI: {}", xsltUri);
	    return new StreamSource(xsltUri);
	}
	catch (IOException ex)
	{
	    log.error("Cannot read internal XSLT stylesheet resource: {}", path);
	    return null;
	}
	catch (URISyntaxException ex)
	{
	    log.error("Cannot read internal XSLT stylesheet resource: {}", path);
	    return null;
	}
	//return null;
    }
    
}
