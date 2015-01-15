/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.processor.provider;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Property;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.naming.ConfigurationException;
import javax.servlet.ServletConfig;
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
 * JAX-RS provider for ontology model.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see com.hp.hpl.jena.ontology.OntModel
 */
@Provider
public class OntologyProvider extends PerRequestTypeInjectableProvider<Context, OntModel> implements ContextResolver<OntModel>
{
    private static final Logger log = LoggerFactory.getLogger(OntologyProvider.class);

    @Context UriInfo uriInfo;
    @Context Request request;
    @Context ServletConfig servletConfig;
    @Context Providers providers;

    public OntologyProvider()
    {
	super(OntModel.class);
    }

    public ServletConfig getServletConfig()
    {
	return servletConfig;
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

    /**
     * Returns configured ontology model.
     * Uses <code>gp:sitemap</code> context parameter value from web.xml as dataset location.
     * 
     * @return ontology model
     */
    public OntModel getOntModel()
    {
        try
        {
            OntModel ontModel = getOntModel(getOntologyURI());

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

    public String getOntologyURI(Property property)
    {
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Object ontology = getServletConfig().getInitParameter(property.getURI());
        if (ontology != null) return ontology.toString();

        return null;
    }
    
    public String getOntologyURI() throws ConfigurationException
    {
        String ontologyURI = getOntologyURI(GP.sitemap);
        
        if (ontologyURI == null)
        {
            if (log.isErrorEnabled()) log.error("Sitemap ontology URI (gp:sitemap) not configured");
            throw new ConfigurationException("Sitemap ontology URI (gp:sitemap) not configured");
        }

        return ontologyURI;
    }
    
    /**
     * Reads ontology model from a file.
     * 
     * @param ontologyURI ontology location
     * @return ontology model
     */
    public OntModel getOntModel(String ontologyURI)
    {
        if (ontologyURI == null) throw new IllegalArgumentException("URI cannot be null");

        if (log.isDebugEnabled()) log.debug("Loading sitemap ontology from URI {}", ontologyURI);
        return OntDocumentManager.getInstance().getOntology(ontologyURI, OntModelSpec.OWL_MEM);
    }

    public Providers getProviders()
    {
        return providers;
    }

}