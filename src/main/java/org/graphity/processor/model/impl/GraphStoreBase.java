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
import com.hp.hpl.jena.rdf.model.Model;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.graphity.core.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for dataset-backed Graph Stores.
 * Implementation of Graph Store Protocol on Jena dataset.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a>
 */
public class GraphStoreBase extends org.graphity.core.model.impl.GraphStoreBase
{
    private static final Logger log = LoggerFactory.getLogger(GraphStoreBase.class);

    private final Dataset dataset;
    private final DataManager dataManager;
        
    public GraphStoreBase(@Context Request request, @Context ServletConfig servletConfig,
            @Context Dataset dataset, @Context DataManager dataManager)
    {
        super(request, servletConfig);
	if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        this.dataset = dataset;
        this.dataManager = dataManager;
    }

    @Override
    public Model getModel()
    {
        return getDataset().getDefaultModel();
    }
    
    @Override
    public Model getModel(String uri)
    {
        return getDataset().getNamedModel(uri);
    }

    @Override
    public boolean containsModel(String uri)
    {
        return getDataset().containsNamedModel(uri);
    }

    @Override
    public void putModel(Model model)
    {
        getDataset().setDefaultModel(model);
    }

    @Override
    public void putModel(String uri, Model model)
    {
        getDataset().replaceNamedModel(uri, model);
    }

    @Override
    public void deleteDefault()
    {
        getDataset().setDefaultModel(null);
    }

    @Override
    public void deleteModel(String uri)
    {
        getDataset().removeNamedModel(uri);
    }

    @Override
    public void add(Model model)
    {
        getDataset().getDefaultModel().add(model);
    }

    @Override
    public void add(String uri, Model model)
    {
        getDataset().addNamedModel(uri, model);
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