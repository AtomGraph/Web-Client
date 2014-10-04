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
import com.hp.hpl.jena.query.DatasetFactory;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.apache.jena.riot.RDFDataMgr;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for dataset.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see com.hp.hpl.jena.query.Dataset 
 */
@Provider
public class DatasetProvider extends PerRequestTypeInjectableProvider<Context, Dataset> implements ContextResolver<Dataset>
{
    private static final Logger log = LoggerFactory.getLogger(DatasetProvider.class);

    @Context UriInfo uriInfo;
    @Context ServletContext servletContext;

    public DatasetProvider()
    {
        super(Dataset.class);
    }

    @Override
    public Injectable<Dataset> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<Dataset>()
	{
	    @Override
	    public Dataset getValue()
	    {
		return getDataset();
	    }
	};
    }
    
    /**
     * Returns configured dataset instance.
     * Uses <code>gp:datasetLocation</code> context parameter value from web.xml as dataset location.
     * 
     * @return dataset instance
     */
    public Dataset getDataset()
    {
        try
        {
            String datasetLocation = getDatasetLocation(getServletContext(), GP.datasetLocation.getURI());
            if (datasetLocation == null)
            {
                if (log.isErrorEnabled()) log.error("Application dataset (gp:datasetLocation) is not configured in web.");
                throw new ConfigurationException("Application dataset (gp:datasetLocation) is not configured in web.xml");
            }
            
            return getDataset(datasetLocation, getUriInfo());
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
    }
    
    public String getDatasetLocation(ServletContext servletContext, String property)
    {
        Object datasetLocation = servletContext.getInitParameter(property);
        if (datasetLocation != null) return datasetLocation.toString();
        
        return null;
    }
    
    public Dataset getDataset(String datasetLocation, UriInfo uriInfo) throws ConfigurationException
    {
        if (datasetLocation == null) throw new IllegalArgumentException("Location String cannot be null");
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	
        Dataset dataset = DatasetFactory.createMem();
        RDFDataMgr.read(dataset, datasetLocation.toString(), uriInfo.getBaseUri().toString(), null); // Lang.TURTLE
        return dataset;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    @Override
    public Dataset getContext(Class<?> type)
    {
        return getDataset();
    }
    
}