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

import org.graphity.core.model.SPARQLEndpointOrigin;
import org.graphity.core.vocabulary.SD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for SPARQL endpoint origin.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.core.model.SPARQLEndpointOrigin
 */
public class SPARQLEndpointOriginProvider extends org.graphity.core.provider.SPARQLEndpointOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointOriginProvider.class);

    /**
     * Returns configured SPARQL endpoint origin.
     * Uses <code>sd:endpoint</code> context parameter value as endpoint URI.
     * 
     * @return configured origin
     */
    @Override
    public SPARQLEndpointOrigin getSPARQLEndpointOrigin()
    {
        return getSPARQLEndpointOrigin(SD.endpoint, getDataManager()); // do not throw WebApplicationException is origin is not configured
    }

}
