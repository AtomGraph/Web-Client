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
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.FileManager;
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
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.processor.vocabulary.GP;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.client.util.DataManager;
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

    public Dataset getDataset()
    {
	ContextResolver<Dataset> cr = getProviders().getContextResolver(Dataset.class, null);
	return cr.getContext(Dataset.class);
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

    public SPARQLEndpoint getSPARQLEndpoint()
    {
	//ContextResolver<SPARQLEndpoint> cr = getProviders().getContextResolver(SPARQLEndpoint.class, null);
	//return cr.getContext(SPARQLEndpoint.class);
        return SPARQLEndpointFactory.create(getRequest(), getServletContext(), getDataset(), (DataManager)FileManager.get());
    }

    public OntModel getOntModel()
    {
        try
        {
            return getOntModel((DataManager)FileManager.get(), getUriInfo(), getServletContext());
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
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
    public OntModel getOntModel(DataManager dataManager, UriInfo uriInfo, ServletContext servletContext) throws ConfigurationException
    {
	
        OntModel ontModel;

        Query query = getQuery(getServletContext(), getUriInfo(), GP.ontologyQuery);
        if (query != null)
        {
            if (servletContext.getInitParameter(GP.datasetEndpoint.getURI()) != null)
            {
                Object ontologyEndpoint = servletContext.getInitParameter(GP.datasetEndpoint.getURI());
                if (log.isDebugEnabled()) log.debug("Reading ontology from remote SPARQL endpoint {}", ontologyEndpoint);

                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                        dataManager.loadModel(ontologyEndpoint.toString(), query));
            }
            else
            {
                if (log.isDebugEnabled()) log.debug("Reading ontology from  SPARQL endpoint");
                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                        getSPARQLEndpoint().loadModel(query));
            }
        }
        else
        {
            if (servletContext.getInitParameter(GP.ontology.getURI()) != null)
            {
                Object ontology = servletContext.getInitParameter(GP.ontology.getURI());
                if (log.isDebugEnabled()) log.debug("Reading ontology from default graph in SPARQL endpoint {}", ontology);

                //ontModel = ModelFactory.createDefaultModel().read(ontology.toString());
                ontModel = OntDocumentManager.getInstance().getOntology(ontology.toString(), OntModelSpec.OWL_MEM);
                //ontModel.read(ontology.toString());
            }
            else
            {
                if (log.isErrorEnabled()) log.error("Sitemap ontology URI (gp:ontology) not configured in web.");
                throw new ConfigurationException("Sitemap ontology URI (gp:ontology) not configured in web.xml");
            }
        }
        
        if (ontModel.isEmpty())
        {
            if (log.isErrorEnabled()) log.error("Sitemap ontology is empty; processing aborted");
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        //OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, ontModel);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

    public Query getQuery(ServletContext servletContext, UriInfo uriInfo, Property property) throws ConfigurationException
    {
        if (servletContext == null) throw new IllegalArgumentException("ServletContext cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        
	Object ontologyQuery = servletContext.getInitParameter(property.getURI());
	//if (ontologyQuery == null) throw new ConfigurationException("Property '" + property.getURI() + "' needs to be set in ServletContext (web.xml)");
        if (ontologyQuery != null)
        {
            ParameterizedSparqlString queryString = new ParameterizedSparqlString(ontologyQuery.toString());
            queryString.setIri("baseUri", uriInfo.getBaseUri().toString());
            return queryString.asQuery();
        }
        
        return null;
    }

    public Providers getProviders()
    {
        return providers;
    }
   
}