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

package org.graphity.client.provider;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.graphity.client.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
@Provider
public class DataManagerProvider extends PerRequestTypeInjectableProvider<Context, DataManager> implements ContextResolver<DataManager>
{
    private static final Logger log = LoggerFactory.getLogger(DataManagerProvider.class);

    @Context ResourceConfig resourceConfig;
    @Context UriInfo uriInfo;
    
    public DataManagerProvider()
    {
        super(DataManager.class);
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public ResourceConfig getResourceConfig()
    {
        return resourceConfig;
    }

    @Override
    public Injectable<DataManager> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<DataManager>()
	{
	    @Override
	    public DataManager getValue()
	    {
		return getDataManager();
	    }
	};
    }

    @Override
    public DataManager getContext(Class<?> type)
    {
	return getDataManager();
    }

    public DataManager getDataManager()
    {
        return getDataManager(LocationMapper.get(), ARQ.getContext(), getResourceConfig(), getUriInfo());
    }
    
    public DataManager getDataManager(LocationMapper mapper, com.hp.hpl.jena.sparql.util.Context context, 
            ResourceConfig resourceConfig, UriInfo uriInfo)
    {
        DataManager dataManager = new DataManager(mapper, context, resourceConfig, uriInfo);
        FileManager.setStdLocators(dataManager);
	dataManager.addLocatorLinkedData();
	dataManager.removeLocatorURL();

        if (log.isDebugEnabled()) log.debug("DataManager LocationMapper: {}", dataManager.getLocationMapper());

        return dataManager;
    }

}
