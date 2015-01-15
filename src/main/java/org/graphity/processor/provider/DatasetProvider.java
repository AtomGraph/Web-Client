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
import com.hp.hpl.jena.rdf.model.Property;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.naming.ConfigurationException;
import javax.servlet.ServletConfig;
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
    @Context ServletConfig servletConfig;

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
            String datasetLocation = getDatasetLocation(GP.datasetLocation);
            if (datasetLocation == null)
            {
                if (log.isErrorEnabled()) log.error("Application dataset (gp:datasetLocation) is not configured in web.xml");
                throw new ConfigurationException("Application dataset (gp:datasetLocation) is not configured in web.xml");
            }
            
            return getDataset(datasetLocation, getUriInfo().getBaseUri());
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
    }
    
    public String getDatasetLocation(Property property)
    {
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Object datasetLocation = getServletConfig().getInitParameter(property.getURI());
        if (datasetLocation != null) return datasetLocation.toString();
        
        return null;
    }
    
    public Dataset getDataset(String location, URI baseURI)
    {
        if (location == null) throw new IllegalArgumentException("Location String cannot be null");
        if (baseURI == null) throw new IllegalArgumentException("Base URI cannot be null");
	
        Dataset dataset = DatasetFactory.createMem();
        RDFDataMgr.read(dataset, location.toString(), baseURI.toString(), null); // Lang.TURTLE
        return dataset;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    @Override
    public Dataset getContext(Class<?> type)
    {
        return getDataset();
    }
    
}