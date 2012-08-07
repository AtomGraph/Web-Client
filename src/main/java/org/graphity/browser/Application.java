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

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.graphity.browser.provider.xslt.ResourceXSLTWriter;
import org.graphity.browser.resource.SPARQLEndpoint;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends org.graphity.ldp.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();
    @Context private ServletContext context = null;

    @Override
    public void init()
    {
	super.init();
	
	DataManager.get().addLocatorFile(context.getRealPath("/WEB-INF/")); // to locate local ontology
    }
        
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(SPARQLEndpoint.class);
	
        classes.add(Resource.class); // handles the rest
	
        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	// generic/global
	singletons.addAll(super.getSingletons());

	// browser-specific
	try
	{
	    singletons.add(new ResourceXSLTWriter(new StreamSource(context.getRealPath("/WEB-INF/Resource.xsl")), DataManager.get()));
	}
	catch (TransformerConfigurationException ex)
	{
	    log.error("XSLT stylesheet error", ex);
	}

	return singletons;
    }

}