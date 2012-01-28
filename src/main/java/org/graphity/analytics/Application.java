/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import java.util.HashSet;
import java.util.Set;
import org.graphity.util.ModelWriter;
import org.graphity.util.ResourceXSLTWriter;

/**
 *
 * @author Pumba
 */
public class Application extends javax.ws.rs.core.Application
{
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();

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
	
	singletons.add(new ResourceXSLTWriter());

	return singletons;
    }
}
