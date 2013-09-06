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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
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

    /**
     * Configuration property for absolute ontology URI (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_URI = "org.graphity.platform.ontology.uri";

    /**
     * Configuration property for ontology SPARQL endpoint (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_ENDPOINT = "org.graphity.platform.ontology.endpoint";

    /**
     * Configuration property for ontology named graph URI (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_GRAPH = "org.graphity.platform.ontology.graph";

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
	return getOntModel(getResourceConfig());
    }
    /**
     * Reads ontology model from configured file and resolves against base URI of the request
     * @return ontology Model
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public OntModel getOntModel(ResourceConfig resourceConfig)
    {
	if (log.isDebugEnabled()) log.debug("web.xml properties: {}", resourceConfig.getProperties());
	Object ontologyPath = getResourceConfig().getProperty(GP.ontologyPath.getURI());
	if (ontologyPath == null) throw new IllegalArgumentException("Property '" + GP.ontologyPath.getURI() + "' needs to be set in ResourceConfig (web.xml)");
	
	String localUri = getUriInfo().getBaseUriBuilder().path(ontologyPath.toString()).build().toString();

	if (getResourceConfig().getProperty(PROPERTY_ONTOLOGY_ENDPOINT) != null)
	{
	    Object ontologyEndpoint = getResourceConfig().getProperty(PROPERTY_ONTOLOGY_ENDPOINT);
	    Object graphUri = getResourceConfig().getProperty(PROPERTY_ONTOLOGY_GRAPH);
	    Query query;
	    if (graphUri != null)
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from named graph {} in SPARQL endpoint {}", graphUri.toString(), ontologyEndpoint);
		query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphUri.toString() +  "> { ?s ?p ?o } }");
	    }
	    else
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from default graph in SPARQL endpoint {}", ontologyEndpoint);
		query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");		
	    }
    
	    OntDocumentManager.getInstance().addModel(localUri,
		    DataManager.get().loadModel(ontologyEndpoint.toString(), query),
		    true);	    
	}
	else
	{
	    if (getResourceConfig().getProperty(PROPERTY_ONTOLOGY_URI) != null)
	    {
		Object externalUri = getResourceConfig().getProperty(PROPERTY_ONTOLOGY_URI);
		if (log.isDebugEnabled()) log.debug("Reading ontology from remote file with URI: {}", externalUri);
		OntDocumentManager.getInstance().addModel(localUri,
			DataManager.get().loadModel(externalUri.toString()),
			true);
	    }
	    else
	    {
		Object ontologyLocation = getResourceConfig().getProperty(GP.ontologyLocation.getURI());
		if (ontologyLocation == null) throw new IllegalStateException("Ontology for this Graphity LDP Application is not configured properly. Check ResourceConfig and/or web.xml");
		if (log.isDebugEnabled()) log.debug("Mapping ontology to a local file: {}", ontologyLocation.toString());
		OntDocumentManager.getInstance().addAltEntry(localUri, ontologyLocation.toString());
	    }
	}
	OntModel ontModel = OntDocumentManager.getInstance().
		getOntology(localUri, OntModelSpec.OWL_MEM);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

}
