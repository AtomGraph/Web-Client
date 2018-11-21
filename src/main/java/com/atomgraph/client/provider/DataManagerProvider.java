/**
 *  Copyright 2014 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.atomgraph.client.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for data manager subclass.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see com.atomgraph.client.util.DataManager
 */
@Provider
public class DataManagerProvider extends PerRequestTypeInjectableProvider<Context, DataManager> implements ContextResolver<DataManager>
{
    private static final Logger log = LoggerFactory.getLogger(DataManagerProvider.class);
    
    private final DataManager dataManager;
    
    public DataManagerProvider(final DataManager dataManager)
    {
        super(DataManager.class);
        
        this.dataManager = dataManager;
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

    /**
     * Returns default data manager instance.
     * @return data manager instance
     */
    public DataManager getDataManager()
    {
        return dataManager;
    }

}