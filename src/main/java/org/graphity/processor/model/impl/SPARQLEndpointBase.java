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
package org.graphity.processor.model.impl;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of SPARQL endpoints proxies.
 * Given a remote endpoint, it functions as a proxy and forwards SPARQL HTTP protocol requests to a remote SPARQL endpoint.
 * Otherwise, the endpoint serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol for RDF</a>
 */
//@Path("/meta/sparql")
public class SPARQLEndpointBase extends org.graphity.server.model.impl.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final Dataset dataset;
    private final DataManager dataManager;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * Uses <code>void:sparqlEndpoint</code> parameter value from web.xml as endpoint URI, if present.
     * Otherwise, uses <code>@Path</code> annotation value for this class (usually <code>/sparql</code> to
     * build local endpoint URI.
     * 
     * @param dataset ontology of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param servletContext webapp context
     */
    public SPARQLEndpointBase(@Context Request request, @Context ServletContext servletContext,
            @Context Dataset dataset, @Context DataManager dataManager)
    {
	super(request, servletContext);
	//if (endpoint == null) throw new IllegalArgumentException("Resource cannot be null");
        if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        //this.resource = endpoint;
        this.dataset = dataset;
        this.dataManager = dataManager;
    }
    
    /**
     * Loads RDF model by querying RDF dataset.
     * 
     * @param query query object
     * @return loaded model
     */
    @Override
    public Model loadModel(Query query)
    {
        if (log.isDebugEnabled()) log.debug("Loading Model from Dataset using Query: {}", query);
        return getDataManager().loadModel(getDataset(), query);
    }

    /**
     * Loads RDF model by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param query query object
     * @return loaded model
     */
    @Override
    public ResultSetRewindable select(Query query)
    {
        if (log.isDebugEnabled()) log.debug("Loading ResultSet from Model using Query: {}", query);
        return getDataManager().loadResultSet(getDataset(), query);
    }

    /**
     * Asks for boolean result by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param query query object
     * @return boolean result
     */
    @Override
    public boolean ask(Query query)
    {
        if (log.isDebugEnabled()) log.debug("Loading Model from Model using Query: {}", query);
        return getDataManager().ask(getDataset(), query);
    }

    @Override
    public void update(UpdateRequest updateRequest)
    {
        if (log.isDebugEnabled()) log.debug("Attempting to update local Model, discarding UpdateRequest: {}", updateRequest);
    }

    public Dataset getDataset()
    {
        return dataset;
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }

}