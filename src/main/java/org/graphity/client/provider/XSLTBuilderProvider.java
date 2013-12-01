/*
 * Copyright (C) 2013 Martynas
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

package org.graphity.client.provider;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.core.ResourceConfig;
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
import javax.ws.rs.ext.Providers;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.util.XSLTBuilder;
import org.graphity.client.vocabulary.GC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class XSLTBuilderProvider extends PerRequestTypeInjectableProvider<Context, XSLTBuilder> implements ContextResolver<XSLTBuilder>
{

    private static final Logger log = LoggerFactory.getLogger(XSLTBuilderProvider.class);

    private URIResolver resolver = null;

    @Context private Providers providers;
    @Context private UriInfo uriInfo;
    @Context private ResourceConfig resourceConfig;
    @Context private ServletContext servletContext;

    /**
     * 
     * @param resolver 
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/javax/xml/transform/URIResolver.html">URIResolver</a>
     */
    public XSLTBuilderProvider(URIResolver resolver)
    {
	super(XSLTBuilder.class);
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	this.resolver = resolver;
    }
    
    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
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

    public URIResolver getURIResolver()
    {
	return resolver;
    }

    public OntModel getOntModel()
    {
	ContextResolver<OntModel> cr = getProviders().getContextResolver(OntModel.class, null);
	return cr.getContext(OntModel.class);
    }
    
    @Override
    public Injectable<XSLTBuilder> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<XSLTBuilder>()
	{
	    @Override
	    public XSLTBuilder getValue()
	    {
                return getXSLTBuilder(getUriInfo(), getOntModel(), GC.stylesheet);
	    }

	};
    }

    @Override
    public XSLTBuilder getContext(Class<?> type)
    {
        return getXSLTBuilder(getUriInfo(), getOntModel(), GC.stylesheet);
    }

    public XSLTBuilder getXSLTBuilder(UriInfo uriInfo, OntModel ontModel, Property property)
    {
        Resource stylesheet = ontModel.createResource(uriInfo.getBaseUri().toString()).
                    getPropertyResourceValue(property);
        try
        {
            if (stylesheet == null) throw new ConfigurationException("XSLT stylesheed not configured");

            return getXSLTBuilder(stylesheet, getUriInfo());
        }
        catch (ConfigurationException ex)
        {
    	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not configured", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    public XSLTBuilder getXSLTBuilder(Resource stylesheet, UriInfo uriInfo) throws ConfigurationException
    {
	if (stylesheet == null) throw new IllegalArgumentException("Stylesheet resource cannot be null");	
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");	

        try
        {            
            URI stylesheetUri = URI.create(stylesheet.getURI());
    	    if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", stylesheetUri);            
            // TO-DO: handle cases with remote URIs (not starting with base URI)
            stylesheetUri = uriInfo.getBaseUri().relativize(stylesheetUri);
            if (stylesheetUri == null) throw new ConfigurationException("Remote XSLT stylesheets not supported");
            
            Source styleSource = getSource(stylesheetUri.toString());

            return XSLTBuilder.fromStylesheet(styleSource).
                    resolver(getURIResolver());
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
     * Provides XML source from filename
     * 
     * @param filename
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