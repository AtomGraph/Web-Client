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
package org.graphity.processor.provider;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class OntologyProvider extends PerRequestTypeInjectableProvider<Context, OntModel> implements ContextResolver<OntModel>
{
    private static final Logger log = LoggerFactory.getLogger(OntologyProvider.class);

    @Context UriInfo uriInfo;
    @Context Request request;
    @Context ServletContext servletContext;
    @Context Providers providers;

    public OntologyProvider()
    {
	super(OntModel.class);
    }

    public ServletContext getServletContext()
    {
	return servletContext;
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Request getRequest()
    {
        return request;
    }

    @Override
    public Injectable<OntModel> getInjectable(ComponentContext cc, Context context)
    {
	//if (log.isDebugEnabled()) log.debug("OntologyProvider UriInfo: {} ResourceConfig.getProperties(): {}", uriInfo, resourceConfig.getProperties());
	
	return new Injectable<OntModel>()
	{
	    @Override
	    public OntModel getValue()
	    {
		return getOntModel();
	    }
	};
    }

    @Override
    public OntModel getContext(Class<?> type)
    {
	return getOntModel();
    }

    public OntModel getOntModel()
    {
        try
        {
            String ontologyURI = getOntologyURI(getServletContext(), GP.ontology.getURI());
            if (ontologyURI == null)
            {
                if (log.isErrorEnabled()) log.error("Sitemap ontology URI (gp:ontology) not configured in web.xml");
                throw new ConfigurationException("Sitemap ontology URI (gp:ontology) not configured in web.xml");
            }

            OntModel ontModel = getOntModel(ontologyURI);
            if (ontModel.isEmpty())
            {
                if (log.isErrorEnabled()) log.error("Sitemap ontology is empty; processing aborted");
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }

            if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
            return ontModel;
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
    }

    public String getOntologyURI(ServletContext servletContext, String property)
    {
        if (servletContext == null) throw new IllegalArgumentException("ServletContext cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Object ontology = servletContext.getInitParameter(property);
        if (ontology != null) return ontology.toString();
        
        return null;
    }
    
    /**
     * Reads ontology model from configured file and resolves against base URI of the request
     * 
     * @param dataManager RDF data manager for this provider
     * @param uriInfo URI information of the current request
     * @param servletContext webapp context
     * @return ontology Model
     * @throws javax.naming.ConfigurationException
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public OntModel getOntModel(String ontologyURI) throws ConfigurationException
    {
        if (ontologyURI == null) throw new IllegalArgumentException("URI cannot be null");

        if (log.isDebugEnabled()) log.error("Loading sitemap ontology from URI {}", ontologyURI);
        return OntDocumentManager.getInstance().getOntology(ontologyURI, OntModelSpec.OWL_MEM);
    }

    public Providers getProviders()
    {
        return providers;
    }

}