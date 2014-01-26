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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.util.OntModelUtils;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class CacheControlProvider extends PerRequestTypeInjectableProvider<Context, CacheControl> implements ContextResolver<CacheControl>
{
    private static final Logger log = LoggerFactory.getLogger(CacheControlProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;

    public CacheControlProvider()
    {
        super(CacheControl.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public OntClass getOntClass()
    {
	return getProviders().getContextResolver(OntClass.class, null).getContext(OntClass.class);
    }
    
    @Override
    public Injectable<CacheControl> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<CacheControl>()
	{
	    @Override
	    public CacheControl getValue()
	    {
                return getCacheControl();
	    }
	};
    }

    @Override
    public CacheControl getContext(Class<?> type)
    {
        return getCacheControl();
    }

    public CacheControl getCacheControl()
    {
        return getCacheControl(getOntClass());
    }
    
    /**
     * Returns `Cache-Control` HTTP header value, specified in an ontology class restriction.
     * 
     * @param ontClass the ontology class with the restriction
     * @return CacheControl instance or null, if no dataset restriction was found
     */
    public final CacheControl getCacheControl(OntClass ontClass)
    {
	RDFNode hasValue = OntModelUtils.getRestrictionHasValue(ontClass, GP.cacheControl);
	if (hasValue != null && hasValue.isLiteral())
        {
            String controlString = hasValue.asLiteral().getString();
            return CacheControl.valueOf(controlString); // will fail on bad config
        }

	return null;
    }
}
