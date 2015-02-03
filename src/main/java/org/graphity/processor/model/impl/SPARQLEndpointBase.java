/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.model.impl;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.graphity.core.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of SPARQL endpoints.
 * Implements SPARQL Protocol on Jena dataset.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol for RDF</a>
 */
public class SPARQLEndpointBase extends org.graphity.core.model.impl.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final Dataset dataset;
    private final DataManager dataManager;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * 
     * @param dataset ontology of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @param request current request
     * @param servletConfig webapp context
     */
    public SPARQLEndpointBase(@Context Request request, @Context ServletConfig servletConfig,
            @Context Dataset dataset, @Context DataManager dataManager)
    {
	super(request, servletConfig);
        if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
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
     * Loads RDF model by querying dataset..
     * 
     * @param query query object
     * @return loaded model
     */
    @Override
    public ResultSetRewindable select(Query query)
    {
        if (log.isDebugEnabled()) log.debug("Loading ResultSet from Dataset using Query: {}", query);
        return getDataManager().loadResultSet(getDataset(), query);
    }

    /**
     * Asks for boolean result by querying dataset.
     * 
     * @param query query object
     * @return boolean result
     */
    @Override
    public boolean ask(Query query)
    {
        if (log.isDebugEnabled()) log.debug("Loading Model from Dataset using Query: {}", query);
        return getDataManager().ask(getDataset(), query);
    }

    /**
     * Executes update on dataset.
     * 
     * @param updateRequest update request
     */
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