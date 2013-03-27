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

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.locator.LocatorGRDDL;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.client.locator.grddl.LocatorAtom;
import org.graphity.client.model.LocalResourceBase;
import org.graphity.client.model.SPARQLResourceBase;
import org.graphity.client.reader.RDFPostReader;
import org.graphity.client.util.DataManager;
import org.graphity.client.writer.ModelXSLTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.system.SPINModuleRegistry;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends org.graphity.server.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();

    public Application()
    {
	classes.add(LocalResourceBase.class); // handles all
	classes.add(SPARQLResourceBase.class); // handles /sparql queries

	singletons.addAll(super.getSingletons());
	singletons.add(new RDFPostReader());

	if (log.isDebugEnabled()) log.debug("Adding master XSLT @Provider");
	singletons.add(new ModelXSLTWriter(DataManager.get())); // writes XHTML responses
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators
     * 
     * @see org.graphity.util.manager.DataManager
     * @see org.graphity.util.locator
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html">LocationMapper</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html">Locator</a>
     */
    @Override
    public void init()
    {
	super.init();
	
	if (log.isDebugEnabled()) log.debug("Application.init() with ResourceConfig: {} and SerlvetContext: {}", getResourceConfig(), getServletContext());
	if (log.isDebugEnabled()) log.debug("Root resource classes: {} Root resource singletons: {}", getResourceConfig().getRootResourceClasses(), getResourceConfig().getRootResourceSingletons());
	if (log.isDebugEnabled()) log.debug("Explicit root resources: {} Classes: {}", getResourceConfig().getExplicitRootResources(), getResourceConfig().getClasses());
	SPINModuleRegistry.get().init(); // needs to be called before any SPIN-related code

	// initialize locally cached ontology mapping
	LocationMapper mapper = new PrefixMapper("location-mapping.ttl");
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled())
	{
	    log.debug("LocationMapper.get(): {}", LocationMapper.get());
	    log.debug("FileManager.get().getLocationMapper(): {}", FileManager.get().getLocationMapper());
	}
	
	DataManager.get().setLocationMapper(mapper);
	// WARNING! ontology caching can cause concurrency/consistency problems
	DataManager.get().setModelCaching(false);
	if (log.isDebugEnabled())
	{
	    log.debug("FileManager.get(): {} DataManager.get(): {}", FileManager.get(), DataManager.get());
	    log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	}
	
	OntDocumentManager.getInstance().setFileManager(DataManager.get());
	if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance(): {} OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance(), OntDocumentManager.getInstance().getFileManager());

	try
	{
	    DataManager.get().addLocator(new LocatorAtom(getSource("org/graphity/client/locator/grddl/atom-grddl.xsl")));
	    DataManager.get().addLocator(new LocatorGRDDL(getSource("org/graphity/client/locator/grddl/twitter-grddl.xsl")));
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
    }

    /**
     * Provides JAX-RS root resource classes.
     *
     * @return set of root resource classes
     * @see org.graphity.server.model
     * @see <a
     * href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getClasses()">Application.getClasses()</a>
     */
    @Override
    public Set<Class<?>> getClasses()
    {
	return classes;
    }

    /**
     * Provides JAX-RS singleton objects (e.g. resources or Providers)
     * 
     * @return set of singleton objects
     * @see org.graphity.server.provider
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getSingletons()">Application.getSingletons()</a>
     */
    @Override
    public Set<Object> getSingletons()
    {
	return singletons;
    }

    /**
     * Provides XML source from filename
     * 
     * @param filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public Source getSource(String filename) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	// using getResource() because getResourceAsStream() does not retain systemId
	//if (log.isDebugEnabled()) log.debug("Resource paths used to load Source: {} from filename: {}", getServletContext().getResourcePaths("/"), filename);
	//URL xsltUrl = getServletContext().getResource(filename);
	if (log.isDebugEnabled()) log.debug("ClassLoader {} used to load Source from filename: {}", getClass().getClassLoader(), filename);
	URL xsltUrl = getClass().getClassLoader().getResource(filename);
	if (xsltUrl == null) throw new FileNotFoundException("File '" + filename + "' not found");
	String xsltUri = xsltUrl.toURI().toString();
	if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
	return new StreamSource(xsltUri);
    }

}
