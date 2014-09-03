/*
 * Copyright (C) 2014 Martynas
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

import com.hp.hpl.jena.sparql.engine.http.Service;
import javax.servlet.ServletContext;
import javax.ws.rs.ext.Provider;
import org.graphity.server.model.GraphStoreOrigin;
import org.graphity.server.model.impl.GraphStoreOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
@Provider
public class GraphStoreOriginProvider extends org.graphity.server.provider.GraphStoreOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(GraphStoreOriginProvider.class);

     /**
     * Returns Graph Store for supplied webapp context configuration.
     * Uses <code>gs:graphStore</code> context parameter value from web.xml as graph store URI.
     * 
     * @param servletContext webapp context
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
