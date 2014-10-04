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

import com.hp.hpl.jena.sparql.engine.http.Service;
import javax.servlet.ServletContext;
import javax.ws.rs.ext.Provider;
import org.graphity.server.model.GraphStoreOrigin;
import org.graphity.server.model.impl.GraphStoreOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for Graph Store origin.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.server.model.GraphStoreOrigin
 */
@Provider
public class GraphStoreOriginProvider extends org.graphity.server.provider.GraphStoreOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(GraphStoreOriginProvider.class);

     /**
     * Returns Graph Store for supplied webapp context configuration.
     * Sets <code>srv:queryAuthUser</code>/<code>srv:queryAuthPwd</code> 
     * context parameter values from web.xml as HTTP Basic authentication credentials.
     * 
     * @param servletContext webapp context
     * @param property config property indicating the graph store URI
     * @return graph store resource
     */
    @Override
    public GraphStoreOrigin getGraphStoreOrigin(ServletContext servletContext, String property)
    {
        if (servletContext == null) throw new IllegalArgumentException("ServletContext cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Object storeUri = servletContext.getInitParameter(property);
        if (storeUri != null)
        {
            String authUser = (String)servletContext.getInitParameter(Service.queryAuthUser.getSymbol());
            String authPwd = (String)servletContext.getInitParameter(Service.queryAuthPwd.getSymbol());
            if (authUser != null && authPwd != null)
                getDataManager().putAuthContext(storeUri.toString(), authUser, authPwd);

            return new GraphStoreOriginBase(storeUri.toString());
        }

        return null;
    }
    
}
