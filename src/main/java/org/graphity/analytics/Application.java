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

package org.graphity.analytics;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.util.manager.DataManager;
import org.graphity.provider.ModelProvider;
import org.graphity.provider.RDFResourceXSLTWriter;
import org.graphity.util.locator.PrefixMapper;
import org.graphity.util.manager.OntDataManager;
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
    //@Context private UriInfo uriInfo = null;
    
    @PostConstruct
    public void init() // initialize locally cached ontologies
    {
	log.debug("Application.init() ServletContext: {}", context);
	// http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms
	// http://incubator.apache.org/jena/documentation/ontology/#compound_ontology_documents_and_imports_processing

//PrefixMapper pm = new PrefixMapper(); pm.addAltPrefixEntry("shit", "fuck");

	LocationMapper.setGlobalLocationMapper(new PrefixMapper("location-mapping.ttl"));
	//FileManager.get().setLocationMapper(new PrefixMapper());
	log.debug("LocationMapper.get(): {}", LocationMapper.get());
	log.debug("FileManager.get().getLocationMapper(): {}", FileManager.get().getLocationMapper());
	FileManager.get().addLocatorFile(context.getRealPath("/WEB-INF/"));
	FileManager.get().addLocatorFile(context.getRealPath("/WEB-INF/owl/"));

	//log.debug("FileManager.get(): {}", FileManager.get());
	log.debug("OntDataManager.getInstance().getFileManager(): {}", OntDataManager.getInstance().getFileManager());
	//FileManager.setGlobalFileManager(DataManager.get());
	//FileManager.get().addLocator(new LocatorLinkedData());
	//removeLocatorURL(DataManager.get()).addLocator(new LocatorLinkedData());
	DataManager.get().setModelCaching(true);
	OntDataManager.getInstance().setFileManager(DataManager.get());
	log.debug("OntDataManager is caching Models: {}", OntDataManager.getInstance().getCacheModels());
	log.debug("FileManager.get(): {}", FileManager.get());
	log.debug("DataManager.get(): {}", DataManager.get());
	log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	log.debug("OntDataManager.getInstance().getFileManager(): {}", OntDataManager.getInstance().getFileManager());
    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(FrontPageResource.class);
        classes.add(OAuthResource.class);
	
	//classes.add(ModelProvider.class);
	//classes.add(ResourceXSLTWriter.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	//singletons.add(new OAuthResource());
	singletons.add(new RDFResourceXSLTWriter());
	singletons.add(new ModelProvider());

	return singletons;
    }
}
