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

import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
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
public class ExternalModelProvider extends PerRequestTypeInjectableProvider<Context, Model> implements ContextResolver<Model>
{
    private static final Logger log = LoggerFactory.getLogger(ExternalModelProvider.class);

    @Context private UriInfo uriInfo;

    public ExternalModelProvider()
    {
        super(Model.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    // query param name could be passed as an argument for custom annotation like @Model("uri") instead
    public String getQueryParamName()
    {
        return "uri";
    }
    
    public URI getURI()
    {
        if (getUriInfo().getQueryParameters().getFirst(getQueryParamName()) != null)
            return URI.create(getUriInfo().getQueryParameters().getFirst(getQueryParamName()));
        
        return null;
    }
    
    @Override
    public Injectable<Model> getInjectable(final ComponentContext cc, final Context a)
    {        
	return new Injectable<Model>()
	{
	    @Override
	    public Model getValue()
	    {
                return getModel();
	    }

	};
    }

    @Override
    public Model getContext(Class<?> type)
    {
        return getModel();
    }

    public Model getModel()
    {
        if (getURI() != null) return getModel(getURI());

        return null;        
    }
    
    public Model getModel(URI uri)
    {
	if (uri == null) throw new IllegalArgumentException("External Model URI cannot be null");	

        if (log.isDebugEnabled()) log.debug("Loading Model from external URI: {}", uri);
        return DataManager.get().loadModel(uri.toString());
    }
    
}