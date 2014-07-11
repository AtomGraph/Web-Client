/*
 * Copyright (C) 2014 Martynas
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

package org.graphity.processor.provider;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.model.Application;
import org.graphity.processor.model.ApplicationBase;
import org.graphity.processor.vocabulary.GP;
import org.graphity.server.model.SPARQLEndpoint;

/**
 *
 * @author Martynas
 */
@Provider
public class ApplicationProvider extends PerRequestTypeInjectableProvider<Context, Application> implements ContextResolver<Application>
{
    
    @Context UriInfo uriInfo;
    @Context ServletContext servletContext;
    @Context Providers providers;
    
    public ApplicationProvider()
    {
	super(Application.class);
    }

    public Providers getProviders()
    {
        return providers;
    }

    @Override
    public Injectable<Application> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<Application>()
	{
	    @Override
	    public Application getValue()
	    {
		return getApplication();
	    }
	};
    }

    @Override
    public Application getContext(Class<?> type)
    {
        return getApplication();
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public SPARQLEndpoint getSPARQLEndpoint()
    {
	ContextResolver<SPARQLEndpoint> cr = getProviders().getContextResolver(SPARQLEndpoint.class, null);
	return cr.getContext(SPARQLEndpoint.class);
    }

    public Application getApplication()
    {
        return getApplication(getSPARQLEndpoint(), getServletContext(), getUriInfo());
    }
    
    public Application getApplication(SPARQLEndpoint metaEndpoint, ServletContext servletContext, UriInfo uriInfo)
    {
        if (metaEndpoint == null) throw new IllegalArgumentException("SPARQLEndpoint cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");

        Model model = metaEndpoint.loadModel(getQuery(servletContext, uriInfo));
        Resource resource = getResource(model, GP.base, uriInfo.getBaseUri());
        
        if (resource == null) throw new WebApplicationException(new ConfigurationException("Graphity Processor application (gp:Application) not configured"));

        return new ApplicationBase(resource);
    }

    public Query getQuery(ServletContext servletContext, UriInfo uriInfo)
    {
        if (servletContext == null) throw new IllegalArgumentException("ServletContext cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");

        Object appQuery = servletContext.getInitParameter(GP.applicationQuery.getURI());
	if (appQuery == null) throw new ConfigurationException("Property '" + GP.applicationQuery.getURI() + "' needs to be set in ServletContext (web.xml)");

        ParameterizedSparqlString queryString = new ParameterizedSparqlString(appQuery.toString());
        queryString.setIri("baseUri", uriInfo.getBaseUri().toString());
        return queryString.asQuery();
    }
    
    public Resource getResource(Model model, Property property, URI baseURI)
    {
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        if (baseURI == null) throw new IllegalArgumentException("Base URI cannot be null");

        ResIterator it = model.listResourcesWithProperty(property, model.createResource(baseURI.toString()));
        
        if (it.hasNext()) return it.next();

        return null;
    }

}