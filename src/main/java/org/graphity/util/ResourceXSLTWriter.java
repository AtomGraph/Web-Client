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
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
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
	    // http://stackoverflow.com/questions/1312406/efficient-xslt-pipeline-in-java-or-redirecting-results-to-sources
	    /*
	    Templates groupRdfXml = stf.newTemplates(new StreamSource(
	      context.getResource(XSLT_BASE + "group-triples.xsl").toURI().toString()));
	    Templates rdfXml2xhtml = stf.newTemplates(getStylesheet());

	    TransformerHandler th1 = stf.newTransformerHandler(groupRdfXml);
	    TransformerHandler th2 = stf.newTransformerHandler(rdfXml2xhtml);

	    th1.setResult(new SAXResult(th2));
	    th2.setResult(new StreamResult(entityStream));

	    Transformer t = stf.newTransformer();
	    t.transform(new StreamSource(new ByteArrayInputStream(bos.toByteArray())), new SAXResult(th1));
	     */
	    /*
	    XSLTBuilder pretransform = XSLTBuilder.fromStylesheet().
		document(new ByteArrayInputStream(bos.toByteArray())).
		transform(new StreamResult(entityStream));

	     */
		    
	    getXSLTBuilder(resource).transform(entityStream);
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
	    parameter("base-uri", resource.getUriInfo().getBaseUri()); // is base uri necessary?
	
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
