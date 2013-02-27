/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.client.model.ResourceBase;
import org.graphity.client.reader.RDFPostReader;
import org.graphity.client.writer.ModelXSLTWriter;
import org.graphity.platform.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends org.graphity.platform.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();

    /**
     * Configuration property for master XSLT stylesheet location (set in web.xml)
     * 
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
     */
    public static final String PROPERTY_XSLT_LOCATION = "org.graphity.client.writer.xslt-location";

    /**
     * Provides JAX-RS root resource classes.
     *
     * @return set of root resource classes
     * @see org.graphity.platform.model
     * @see <a
     * href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getClasses()">Application.getClasses()</a>
     */
    @Override
    public Set<Class<?>> getClasses()
    {
	classes.add(ResourceBase.class); // handles all

	return classes;
    }

    /**
     * Provides JAX-RS singleton objects (e.g. resources or Providers)
     * 
     * @return set of singleton objects
     * @see org.graphity.platform.provider
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getSingletons()">Application.getSingletons()</a>
     */
    @Override
    public Set<Object> getSingletons()
    {
	singletons.add(super.getSingletons());
	singletons.add(new RDFPostReader());

	if (getResourceConfig().getProperty(PROPERTY_XSLT_LOCATION) != null)
	    try
	    {
		singletons.add(new ModelXSLTWriter(getSource(getResourceConfig().getProperty(PROPERTY_XSLT_LOCATION).toString()), DataManager.get())); // writes XHTML responses
	    }
	    catch (TransformerConfigurationException ex)
	    {
		if (log.isErrorEnabled()) log.error("XSLT stylesheet error", ex);
	    }
	    catch (FileNotFoundException ex)
	    {
		if (log.isErrorEnabled()) log.error("XSLT stylesheet not found", ex);
	    }
	    catch (URISyntaxException ex)
	    {
		if (log.isErrorEnabled()) log.error("XSLT stylesheet URI error", ex);
	    }
	    catch (MalformedURLException ex)
	    {
		if (log.isErrorEnabled()) log.error("XSLT stylesheet URL error", ex);
	    }
	else
	    if (log.isWarnEnabled()) log.warn("Master XSLT stylesheet not configured in web.xml, no XHTML @Provider will be available");

	return singletons;
    }

}
