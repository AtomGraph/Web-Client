/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.provider;

import javax.ws.rs.ext.Provider;
import org.graphity.core.model.GraphStoreOrigin;
import org.graphity.core.vocabulary.G;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for Graph Store origin.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.core.model.GraphStoreOrigin
 */
@Provider
public class GraphStoreOriginProvider extends org.graphity.core.provider.GraphStoreOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(GraphStoreOriginProvider.class);

    /**
     * Returns configured Graph Store origin.
     * Uses <code>gs:graphStore</code> context parameter value from web.xml as graph store URI.
     * 
     * @return graph store origin
     */
    @Override
    public GraphStoreOrigin getGraphStoreOrigin()
    {
        return getGraphStoreOrigin(G.graphStore, getDataManager()); // do not throw WebApplicationException is origin is not configured
    }

}
