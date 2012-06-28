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
package org.graphity.browser;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.browser.provider.xslt.ResourceXHTMLWriter;
import org.graphity.browser.resource.OAuthResource;
import org.graphity.browser.resource.SPARQLResource;
import org.graphity.browser.resource.SearchResource;
import org.graphity.provider.ModelProvider;
import org.graphity.provider.RDFPostReader;
import org.graphity.provider.ResultSetWriter;
import org.graphity.util.locator.PrefixMapper;
import org.graphity.util.locator.grddl.LocatorAtom;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends javax.ws.rs.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();
    @Context private ServletContext context = null;
    
    @PostConstruct
    public void init() // initialize locally cached ontologies
    {
	log.debug("Application.init() ServletContext: {}", context);
	// http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms
	// http://incubator.apache.org/jena/documentation/ontology/#compound_ontology_documents_and_imports_processing

	LocationMapper mapper = new PrefixMapper("location-mapping.ttl");
	LocationMapper.setGlobalLocationMapper(mapper);
	log.debug("LocationMapper.get(): {}", LocationMapper.get());
	log.debug("FileManager.get().getLocationMapper(): {}", FileManager.get().getLocationMapper());
	FileManager.get().addLocatorFile(context.getRealPath("/WEB-INF/")); // necessary?
	
	DataManager.get().setLocationMapper(mapper);
	DataManager.get().setModelCaching(false);
	log.debug("FileManager.get(): {}", FileManager.get());
	log.debug("DataManager.get(): {}", DataManager.get());
	log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	
	OntDocumentManager.getInstance().setFileManager(DataManager.get());
	log.debug("OntDocumentManager.getInstance(): {}", OntDocumentManager.getInstance());
	log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());

	try
	{
	    DataManager.get().addLocator(new LocatorAtom());
	} catch (MalformedURLException ex)
	{
	    log.error("Malformed Locator stylesheet URL", ex);
	} catch (URISyntaxException ex)
	{
	    log.error("Malformed Locator stylesheet URI", ex);
	}
    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(OAuthResource.class);
        classes.add(SearchResource.class);
        classes.add(SPARQLResource.class);
	
        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	// generic/global
	singletons.add(new ModelProvider());
	singletons.add(new ResultSetWriter());
	singletons.add(new RDFPostReader());

	// browser-specific
	singletons.add(new ResourceXHTMLWriter());

	return singletons;
    }

}