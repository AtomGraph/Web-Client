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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.update.UpdateRequest;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.update.UpdateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class UpdateBuilderProvider extends PerRequestTypeInjectableProvider<Context, UpdateBuilder> implements ContextResolver<UpdateBuilder>
{
    private static final Logger log = LoggerFactory.getLogger(UpdateBuilderProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;
    
    public UpdateBuilderProvider()
    {
        super(UpdateBuilder.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public UpdateRequest getUpdateRequest()
    {
	return getProviders().getContextResolver(UpdateRequest.class, null).getContext(UpdateRequest.class);
    }

    public OntModel getOntModel()
    {
	return getProviders().getContextResolver(OntModel.class, null).getContext(OntModel.class);
    }
    
    @Override
    public Injectable<UpdateBuilder> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<UpdateBuilder>()
	{
	    @Override
	    public UpdateBuilder getValue()
	    {
                return getUpdateBuilder();
	    }
	};
    }

    @Override
    public UpdateBuilder getContext(Class<?> type)
    {
        return getUpdateBuilder();
    }
    
    public UpdateBuilder getUpdateBuilder()
    {
        UpdateRequest updateRequest = null;
        // the Update fetched to the builder must be the first one in the UpdateRequest!
        if (updateRequest != null)
            return UpdateBuilder.fromUpdate(getUpdateRequest().getOperations().get(0), getOntModel());
        else return null;
    }
}
