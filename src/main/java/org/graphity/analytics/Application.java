/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import com.hp.hpl.jena.util.FileManager;
import java.util.HashSet;
import java.util.Set;
import org.graphity.provider.ModelWriter;
import org.graphity.provider.RDFResourceXSLTWriter;
import org.graphity.util.LocatorLinkedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
public class Application extends javax.ws.rs.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(LocatorLinkedData.class);
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();

    public Application()
    {
	log.debug("Initializing application {}", this.getClass().getCanonicalName());
	FileManager.get().addLocator(new LocatorLinkedData());
    }

    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(FrontPageResource.class);
	
	classes.add(ModelWriter.class);
	//classes.add(ResourceXSLTWriter.class);
	
        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	//singletons.add(new FrontPageResource());
	
	singletons.add(new RDFResourceXSLTWriter());

	return singletons;
    }
}
