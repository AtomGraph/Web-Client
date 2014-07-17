/*
 * Copyright (C) 2013 Martynas
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
import com.hp.hpl.jena.rdf.model.Model;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of Graph Store proxies.
 * Given a remote store, it functions as a proxy and forwards SPARQL Graph Store protocol requests to a remote graph store.
 * Otherwise, the store serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a>
 */
@Path("/meta/service") // not standard
public class GraphStoreBase extends org.graphity.server.model.impl.GraphStoreBase
{
    private static final Logger log = LoggerFactory.getLogger(GraphStoreBase.class);

    private final Dataset dataset;
    private final DataManager dataManager;
        
    public GraphStoreBase(@Context Request request, @Context ServletContext servletContext,
            @Context Dataset dataset, @Context DataManager dataManager)
    {
        super(request, servletContext);
	if (dataset == null) throw new IllegalArgumentException("Dataset cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        this.dataset = dataset;
        this.dataManager = dataManager;

        /*
        if (graphStore.isURIResource() && !dataManager.hasServiceContext(graphStore))
        {
            if (log.isDebugEnabled()) log.debug("Adding service Context for local Graph Store with URI: {}", graphStore.getURI());
            dataManager.addServiceContext(graphStore);
        }
        */    
    }

    /**
     * Builds a list of acceptable response variants
     * 
     * @return supported variants
     */
    /*
    @Override
    public List<Variant> getVariants()
    {        
        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null)); // TO-DO: move this out to Client!
        return list;
    }
    */

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