/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Pumba
 */
@Provider
@Produces({"text/html", "application/xml", "application/xhtml+xml", "application/rdf+xml", "text/xml", "text/plain"})
public class ModelXSLTWriter implements MessageBodyWriter<Model>
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
 
    @Context ServletContext context;
    private ByteArrayOutputStream bos = null;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return Model.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	if (bos == null)
	{
	    bos = new ByteArrayOutputStream();
	    model.write(bos);
	}
	
	return bos.size();
	//return Integer.valueOf(stream.toByteArray().length).longValue();
    }

    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
	if (bos == null)
	{
	    bos = new ByteArrayOutputStream();
	    model.write(bos);
	}
	// can we avoid buffering here? I guess not...
	try
	{
	    XSLTBuilder.fromStylesheet(getStylesheet()).
		document(new ByteArrayInputStream(bos.toByteArray())).
		transform(entityStream);
	}
	catch (TransformerException ex)
	{
	    Logger.getLogger(ModelXSLTWriter.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public Source getStylesheet()
    {
	if (context == null) Logger.getLogger(ModelXSLTWriter.class.getName()).log(Level.INFO, "context == null");
	return new StreamSource(context.getResourceAsStream(XSLT_BASE + "ResourceReadView.xsl"));
    }
    
}
