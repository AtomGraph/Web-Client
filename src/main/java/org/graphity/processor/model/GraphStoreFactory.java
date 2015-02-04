/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.processor.model;

import org.graphity.processor.model.impl.GraphStoreBase;
import com.hp.hpl.jena.query.Dataset;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Request;
import org.graphity.core.util.DataManager;
import org.graphity.core.model.GraphStore;

/**
 * Factory class for creating Graph Stores.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class GraphStoreFactory extends org.graphity.core.model.GraphStoreFactory
{

    /**
     * Creates new Graph Store instance backed by dataset.
     * 
     * @param request current request
     * @param servletConfig webapp context
     * @param dataset dataset of the store
     * @param dataManager RDF data manager for this graph store
     * @return graph store instance
     */
    public static GraphStore create(Request request, ServletConfig servletConfig,
            Dataset dataset, DataManager dataManager)
    {
	return new GraphStoreBase(request, servletConfig, dataset, dataManager);
    }

}
