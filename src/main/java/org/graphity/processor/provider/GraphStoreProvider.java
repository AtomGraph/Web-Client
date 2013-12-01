/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.server.model.GraphStore;
import org.graphity.server.model.GraphStoreFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class GraphStoreProvider extends PerRequestTypeInjectableProvider<Context, GraphStore> implements ContextResolver<GraphStore>
{

    @Context Providers providers;
    @Context ResourceConfig resourceConfig;
    @Context UriInfo uriInfo;
    @Context Request request;

    public GraphStoreProvider()
    {
	super(GraphStore.class);
    }

    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Providers getProviders()
    {
	return providers;
    }

    public Request getRequest()
    {
	return request;
    }

    @Override
    public Injectable<GraphStore> getInjectable(ComponentContext cc, Context a)
    {
	//if (log.isDebugEnabled()) log.debug("GraphStoreProvider UriInfo: {} ResourceConfig.getProperties(): {}", uriInfo, resourceConfig.getProperties());
	
	return new Injectable<GraphStore>()
	{
	    @Override
	    public GraphStore getValue()
	    {
		return getGraphStore();
	    }

	};
    }

    @Override
    public GraphStore getContext(Class<?> type)
    {
	return getGraphStore();
    }

    public GraphStore getGraphStore()
    {
	return GraphStoreFactory.createGraphStore(getUriInfo(), getRequest(), getResourceConfig());
    }

}