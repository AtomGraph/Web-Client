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
import com.hp.hpl.jena.util.FileManager;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.processor.vocabulary.GP;
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
    @Context ResourceConfig resourceConfig;

    public OntologyProvider()
    {
	super(OntModel.class);
    }

    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
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

    public OntModel getOntModel()
    {
	return getOntModel(getUriInfo(), getResourceConfig());
    }
    
    /**
     * Reads ontology model from configured file and resolves against base URI of the request
     * @param uriInfo URI information of the current request
     * @param resourceConfig webapp configuration
     * @return ontology Model
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public OntModel getOntModel(UriInfo uriInfo, ResourceConfig resourceConfig)
    {
        DataManager dataManager = new DataManager(new FileManager(new PrefixMapper("prefix-mapping.n3")), ARQ.getContext(), resourceConfig);
        DataManager.setStdLocators(dataManager);
        dataManager.setModelCaching(true);
        OntDocumentManager ontManager = new OntDocumentManager(DataManager.get(), (String)null);
        ontManager.setFileManager(dataManager);

	Object ontologyPath = resourceConfig.getProperty(GP.ontologyPath.getURI());
	if (ontologyPath == null) throw new IllegalArgumentException("Property '" + GP.ontologyPath.getURI() + "' needs to be set in ResourceConfig (web.xml)");
	
	String localUri = uriInfo.getBaseUriBuilder().path(ontologyPath.toString()).build().toString();

	if (resourceConfig.getProperty(GP.ontologyEndpoint.getURI()) != null)
	{
	    Object ontologyEndpoint = resourceConfig.getProperty(GP.ontologyEndpoint.getURI());
	    Object ontologyQuery = resourceConfig.getProperty(GP.ontologyQuery.getURI());
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
            Object ontologyLocation = resourceConfig.getProperty(GP.ontologyLocation.getURI());
            if (ontologyLocation == null) throw new IllegalStateException("Sitemap ontology is not configured properly. Check ResourceConfig and/or web.xml");
            URI ontologyLocationURI = URI.create((String)ontologyLocation);
            if (ontologyLocationURI.isAbsolute())
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from remote file with URI: {}", ontologyLocationURI);
		ontManager.addModel(localUri,
			DataManager.get().loadModel(ontologyLocationURI.toString()),
			true);
	    }
	    else
	    {
		if (log.isDebugEnabled()) log.debug("Mapping ontology to a local file: {}", ontologyLocation.toString());
		ontManager.addAltEntry(localUri, ontologyLocation.toString());
	    }
	}
        
	OntModel ontModel = ontManager.getOntology(localUri, OntModelSpec.OWL_MEM);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

}
