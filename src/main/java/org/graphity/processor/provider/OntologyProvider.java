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
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.LocationMapper;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
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
	ContextResolver<SPARQLEndpoint> cr = getProviders().getContextResolver(SPARQLEndpoint.class, null);
	return cr.getContext(SPARQLEndpoint.class);
    }

    public OntModel getOntModel()
    {
	return getOntModel(getDataManager(), getUriInfo(), getServletContext());
    }
    
    public DataManager getDataManager()
    {
        DataManager dataManager = new DataManager(LocationMapper.get(), ARQ.getContext(), getServletContext());
        DataManager.setStdLocators(dataManager);
        dataManager.setModelCaching(true);
        
        return dataManager;
    }
    
    /**
     * Reads ontology model from configured file and resolves against base URI of the request
     * 
     * @param dataManager RDF data manager for this provider
     * @param uriInfo URI information of the current request
     * @param servletContext webapp context
     * @return ontology Model
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public OntModel getOntModel(DataManager dataManager, UriInfo uriInfo, ServletContext servletContext)
    {
        OntDocumentManager ontManager = new OntDocumentManager(dataManager, (String)null);
        ontManager.setFileManager(dataManager);

	Object ontologyPath = servletContext.getInitParameter(GP.ontologyPath.getURI());
	if (ontologyPath == null) throw new IllegalArgumentException("Property '" + GP.ontologyPath.getURI() + "' needs to be set as context-param in web.xml");
	
	String localUri = uriInfo.getBaseUriBuilder().path(ontologyPath.toString()).build().toString();

	if (servletContext.getInitParameter(GP.ontologyEndpoint.getURI()) != null)
	{
	    Object ontologyEndpoint = servletContext.getInitParameter(GP.ontologyEndpoint.getURI());
	    Object ontologyQuery = servletContext.getInitParameter(GP.ontologyQuery.getURI());
            if (ontologyQuery == null) throw new IllegalStateException("Sitemap ontology query is not configured properly. Check ResourceConfig and/or web.xml");

            if (log.isDebugEnabled()) log.debug("Reading ontology from default graph in SPARQL endpoint {}", ontologyEndpoint);
            Query query = QueryFactory.create(ontologyQuery.toString());
    
            Model model = dataManager.loadModel(ontologyEndpoint.toString(), query);
            if (model.isEmpty())
            {
                if (log.isErrorEnabled()) log.error("Sitemap ontology is empty; processing aborted");
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
            ontManager.addModel(localUri, model, true);	    
	}
	else
	{
            Object ontologyLocation = servletContext.getInitParameter(GP.ontologyLocation.getURI());
            if (ontologyLocation == null) throw new IllegalStateException("Sitemap ontology is not configured properly. Check ResourceConfig and/or web.xml");
            URI ontologyLocationURI = URI.create((String)ontologyLocation);
            /*
            if (ontologyLocationURI.isAbsolute())
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from remote file with URI: {}", ontologyLocationURI);
		ontManager.addModel(localUri,
			dataManager.loadModel(ontologyLocationURI.toString()),
			true);
	    }
	    else
	    {
		if (log.isDebugEnabled()) log.debug("Mapping ontology to a local file: {}", ontologyLocation.toString());
		ontManager.addAltEntry(localUri, ontologyLocation.toString());
	    }
            */
            
            ontManager.addModel(localUri, getModel(getSPARQLEndpoint()));
            //return getOntModel(getSPARQLEndpoint());
	}
        
	OntModel ontModel = ontManager.getOntology(localUri, OntModelSpec.OWL_MEM);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

    public final Model getModel(SPARQLEndpoint metaEndpoint)
    {
        String queryString = "PREFIX gp: <http://processor.graphity.org/ontology#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> DESCRIBE ?sitemap ?s WHERE { GRAPH ?sitemapGraph { ?sitemap a gp:Sitemap } GRAPH ?g { ?s rdfs:isDefinedBy ?sitemap } }";
        Query query = QueryFactory.create(queryString);
        return metaEndpoint.loadModel(query);
        //return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
    }

    public Providers getProviders()
    {
        return providers;
    }
   
}
