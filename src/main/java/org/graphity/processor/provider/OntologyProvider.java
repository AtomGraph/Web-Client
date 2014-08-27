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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
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
import org.graphity.server.util.DataManager;
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

    /*
    public DataManager getDataManager()
    {
        DataManager dataManager = new DataManager(LocationMapper.get(), ARQ.getContext(), getServletContext());
        DataManager.setStdLocators(dataManager);
        dataManager.setModelCaching(true);
        
        return dataManager;
    }
    */
    
    /**
     * Reads ontology model from configured file and resolves against base URI of the request
     * 
     * @param dataManager RDF data manager for this provider
     * @param uriInfo URI information of the current request
     * @param servletContext webapp context
     * @return ontology Model
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public OntModel getOntModel(DataManager dataManager, UriInfo uriInfo, ServletContext servletContext) throws ConfigurationException
    {
       Object ontologyQuery = servletContext.getInitParameter(GP.ontologyQuery.getURI());
        if (ontologyQuery == null) throw new IllegalStateException("Sitemap ontology query is not configured properly. Check ResourceConfig and/or web.xml");
	
        Model model;

	if (servletContext.getInitParameter(GP.datasetEndpoint.getURI()) != null)
	{
	    Object ontologyEndpoint = servletContext.getInitParameter(GP.datasetEndpoint.getURI());

            if (log.isDebugEnabled()) log.debug("Reading ontology from default graph in SPARQL endpoint {}", ontologyEndpoint);
            Query query = QueryFactory.create(ontologyQuery.toString());
    
            model = dataManager.loadModel(ontologyEndpoint.toString(), query);
	}
	else
	{
            model = getSPARQLEndpoint().loadModel(getQuery(getServletContext(), getUriInfo(), GP.ontologyQuery));
        }
        
        if (model.isEmpty())
        {
            if (log.isErrorEnabled()) log.error("Sitemap ontology is empty; processing aborted");
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

    public Query getQuery(ServletContext servletContext, UriInfo uriInfo, Property property) throws ConfigurationException
    {
        if (servletContext == null) throw new IllegalArgumentException("ServletContext cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        
	Object ontologyQuery = servletContext.getInitParameter(property.getURI());
	if (ontologyQuery == null) throw new ConfigurationException("Property '" + property.getURI() + "' needs to be set in ServletContext (web.xml)");

        ParameterizedSparqlString queryString = new ParameterizedSparqlString(ontologyQuery.toString());
        queryString.setIri("baseUri", uriInfo.getBaseUri().toString());
        return queryString.asQuery();
    }

    public Providers getProviders()
    {
        return providers;
    }
   
}