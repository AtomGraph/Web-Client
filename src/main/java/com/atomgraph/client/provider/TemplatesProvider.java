/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for XSLT builder.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see com.atomgraph.client.util.XSLTBuilder
 */
@Provider
public class TemplatesProvider extends PerRequestTypeInjectableProvider<Context, Templates> implements ContextResolver<Templates>
{

    private static final Logger log = LoggerFactory.getLogger(TemplatesProvider.class);

    private final SAXTransformerFactory transformerFactory;
    private final Source stylesheet;
    private final Boolean cacheStylesheet;
    private Templates templatesCache;

    /**
     * Creates provider from stylesheet
     * @param transformerFactory SAX transformer factory
     * @param uriResolver URI resolver
     * @param stylesheet stylesheet source
     * @param cacheStylesheet true if the processor should cache stylesheet
     */
    public TemplatesProvider(final SAXTransformerFactory transformerFactory, final URIResolver uriResolver,
            final Source stylesheet, final boolean cacheStylesheet)
    {
	super(Templates.class);
        this.transformerFactory = transformerFactory;
        this.stylesheet = stylesheet;
        this.cacheStylesheet = cacheStylesheet;
        
        this.transformerFactory.setURIResolver(uriResolver);
    }
        
    public boolean isCacheStylesheet()
    {
        return cacheStylesheet;
    }

    @Override
    public Injectable<Templates> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<Templates>()
	{
	    @Override
	    public Templates getValue()
	    {
                return getTemplates();
	    }
	};
    }

    @Override
    public Templates getContext(Class<?> type)
    {
        return getTemplates();
    }

    public Source getStylesheet()
    {
        return stylesheet;
    }

    protected Templates getTemplatesCache()
    {
        return templatesCache;
    }
    
    public Templates getTemplates()
    {
        try
        {            
            if (isCacheStylesheet() && getTemplatesCache() != null) return getTemplatesCache();

            templatesCache = getTemplates(getStylesheet());
            
            return getTemplatesCache();
        }
        catch (TransformerConfigurationException ex)
        {
	    if (log.isErrorEnabled()) log.error("XSLT transformer not configured property", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    public Templates getTemplates(Source source) throws TransformerConfigurationException
    {
        return getTransformerFactory().newTemplates(source);
    }

    public SAXTransformerFactory getTransformerFactory()
    {
        return transformerFactory;
    }
    
}