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
package org.graphity.client.reader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.server.provider.ModelProvider;
import org.graphity.client.util.XSLTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads RDF from XML by transforming it to RDF/XML using GRDDL (XSLT) stylesheet
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.util.locator.LocatorGRDDL
 */
@Consumes(MediaType.APPLICATION_XML)
public class ModelXSLTReader extends ModelProvider implements RDFReader
{
    private static final Logger log = LoggerFactory.getLogger(ModelXSLTReader.class);

    private Source stylesheet = null;
    private URIResolver resolver = null;
    protected RDFErrorHandler errorHandler = null ;

    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;

    /**
     * Constructs from stylesheet source and URI resolver
     * 
     * @param stylesheet the source of the XSLT transformation
     * @param resolver URI resolver to be used in the transformation
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public ModelXSLTReader(Source stylesheet, URIResolver resolver)
    {
	if (stylesheet == null) throw new IllegalArgumentException("XSLT stylesheet Source cannot be null");
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.stylesheet = stylesheet;
	this.resolver = resolver;
    }

    @Override
    public void read(Model model, Reader reader, String base)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void read(Model model, InputStream in, URI base)
    {
        Source doc = new StreamSource(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try
        {
            getXSLTBuilder(doc, base, baos).transform();
        }
	catch (TransformerConfigurationException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error in GRDDL XSLT transformer config", ex);
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error in GRDDL XSLT transformation", ex);
	}
        
        model.read(new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray())), base.toString());
    }
    
    @Override
    public void read(Model model, InputStream in, String base)
    {
	read(model, in, URI.create(base));
    }

    @Override
    public void read(Model model, String url)
    {
        try {
            URLConnection conn = new URL(url).openConnection();
            String encoding = conn.getContentEncoding();

            // Dispatch on MIME type.
            // Inc .gz streams.

            if ( encoding == null )
                read(model, new InputStreamReader(conn.getInputStream(), FileUtils.encodingUTF8), url);
            else
            {
                if ( ! encoding.equalsIgnoreCase("UTF-8") )
                    LoggerFactory.getLogger(this.getClass()).warn("URL content is not UTF-8") ;
                read(model, new InputStreamReader(conn.getInputStream(),encoding), url);
            }
        }
        catch (JenaException e)
        {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e) ;
        }
        catch (Exception ex)
        {
            if ( errorHandler == null ) throw new JenaException(ex) ;
            errorHandler.error(ex) ;
        }
    }

    @Override
    public Object setProperty(String string, Object o)
    {
	return null;
    }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
	RDFErrorHandler old = errorHandler ;
	errorHandler = errHandler ;
	return old ;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	if (log.isTraceEnabled()) log.trace("Reading model with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);
	
	Model model = ModelFactory.createDefaultModel();
	read(model, entityStream, getUriInfo().getAbsolutePath()); // extract base URI from UriInfo?

	return model;
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

    public XSLTBuilder getXSLTBuilder(Source doc, URI base, OutputStream baos) throws TransformerConfigurationException
    {
	return XSLTBuilder.fromStylesheet(getStylesheet()).
	    resolver(getURIResolver()).
	    document(doc).
	    parameter("base-uri", base).
	    result(new StreamResult(baos));
    }
}