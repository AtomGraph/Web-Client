/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.client.provider;

import com.hp.hpl.jena.rdf.model.Property;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.util.DataManager;
import org.graphity.client.util.XSLTBuilder;
import org.graphity.client.vocabulary.GC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for XSLT builder.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.client.util.XSLTBuilder
 */
@Provider
public class XSLTBuilderProvider extends PerRequestTypeInjectableProvider<Context, XSLTBuilder> implements ContextResolver<XSLTBuilder>
{

    private static final Logger log = LoggerFactory.getLogger(XSLTBuilderProvider.class);

    @Context private Providers providers;
    @Context private UriInfo uriInfo;
    @Context private ServletContext servletContext;
    private final XSLTBuilder builder = XSLTBuilder.newInstance();

    /**
     * 
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public XSLTBuilderProvider()
    {
	super(XSLTBuilder.class);
    }
    
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }
    
    public DataManager getDataManager()
    {
	ContextResolver<DataManager> cr = getProviders().getContextResolver(DataManager.class, null);
	return cr.getContext(DataManager.class);
    }

    @Override
    public Injectable<XSLTBuilder> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<XSLTBuilder>()
	{
	    @Override
	    public XSLTBuilder getValue()
	    {
                return getXSLTBuilder();
	    }
	};
    }

    @Override
    public XSLTBuilder getContext(Class<?> type)
    {
        return getXSLTBuilder();
    }

    /**
     * Returns configured XSLT stylesheet resource.
     * Uses <code>gc:stylesheet</code> context parameter value from web.xml as stylesheet location.
     * 
     * @param property
     * @return stylesheet URI
     * @throws URISyntaxException 
     */
    public URI getStylesheetURI(Property property) throws URISyntaxException
    {
        Object stylesheetURI = getServletContext().getInitParameter(property.getURI());
        if (stylesheetURI != null) return new URI(stylesheetURI.toString());
        
        return null;
    }
    
    public URI getStylesheetURI() throws ConfigurationException, URISyntaxException
    {
        URI stylesheetURI = getStylesheetURI(GC.stylesheet);
        
        if (stylesheetURI == null)
        {
            if (log.isErrorEnabled()) log.error("XSLT stylesheet (gc:stylesheet) not configured");
            throw new ConfigurationException("XSLT stylesheet (gc:stylesheet) not configured");
        }
        
        return stylesheetURI;
    }
    
    /**
     * Constructs XSLT builder from configuration.
     * 
     * @return XSLT builder object
     */
    public XSLTBuilder getXSLTBuilder()
    {
        try
        {
            //return XSLTBuilder.fromStylesheet(getSource(getStylesheetURI())).resolver(getDataManager());
            return builder.stylesheet(getSource(getStylesheetURI())).resolver(getDataManager());
        }
        catch (ConfigurationException ex)
        {
    	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not configured", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        catch (TransformerConfigurationException ex)
        {
	    if (log.isErrorEnabled()) log.error("XSLT transformer not configured property", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        catch (FileNotFoundException ex)
        {
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not found", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
        catch (URISyntaxException | MalformedURLException ex)
        {
    	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URL error", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Converts stylesheet resource into XML source.
     * 
     * @param stylesheetURI stylesheet URI
     * @return stylesheet XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException
     * @throws MalformedURLException 
     */
    public Source getSource(URI stylesheetURI) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	if (stylesheetURI == null) throw new IllegalArgumentException("Stylesheet URI name cannot be null");	

        if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", stylesheetURI);            
        return getSource(stylesheetURI.toString());
    }

    /**
     * Provides XML source from filename
     * 
     * @param filename stylesheet filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @throws java.net.MalformedURLException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public Source getSource(String filename) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	if (filename == null) throw new IllegalArgumentException("XML file name cannot be null");	

        if (log.isDebugEnabled()) log.debug("Resource paths used to load Source: {} from filename: {}", getServletContext().getResourcePaths("/"), filename);
        URL xsltUrl = getServletContext().getResource(filename);
	if (xsltUrl == null) throw new FileNotFoundException("File '" + filename + "' not found");
	String xsltUri = xsltUrl.toURI().toString();
	if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
	return new StreamSource(xsltUri);
    }
       
}