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

import com.hp.hpl.jena.query.Dataset;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Request;
import javax.ws.rs.ext.ContextResolver;
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.core.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for SPARQL endpoint.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.core.model.SPARQLEndpoint
 */
public class SPARQLEndpointProvider extends org.graphity.core.provider.SPARQLEndpointProvider
{
    
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointProvider.class);

    public Dataset getDataset()
    {
	ContextResolver<Dataset> cr = getProviders().getContextResolver(Dataset.class, null);
	return cr.getContext(Dataset.class);
    }

    /**
     * This subclass provides a proxy if endpoint origin is configured, and a local dataset-backed endpoint if it is not.
     * 
     * @return endpoint instance
     */
    @Override
    public SPARQLEndpoint getSPARQLEndpoint()
    {
        if (getSPARQLEndpointOrigin() == null) // use local endpoint
            return getSPARQLEndpoint(getRequest(), getServletConfig(), getDataset(), getDataManager());

        return super.getSPARQLEndpoint();
    }
    
    public SPARQLEndpoint getSPARQLEndpoint(Request request, ServletConfig servletConfig, Dataset dataset, org.graphity.core.util.DataManager dataManager)
    {
        return SPARQLEndpointFactory.create(request, servletConfig, dataset, dataManager);
    }

}
