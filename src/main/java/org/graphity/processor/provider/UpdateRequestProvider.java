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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

/**
 *
 * @author Martynas
 */
public class UpdateRequestProvider extends PerRequestTypeInjectableProvider<Context, UpdateRequest> implements ContextResolver<UpdateRequest>
{
    private static final Logger log = LoggerFactory.getLogger(UpdateRequestProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;    

    public UpdateRequestProvider()
    {
        super(UpdateRequest.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public OntModel getOntModel()
    {
	return getProviders().getContextResolver(OntModel.class, null).getContext(OntModel.class);
    }

    public OntClass getOntClass()
    {
	return getProviders().getContextResolver(OntClass.class, null).getContext(OntClass.class);
    }
    
    @Override
    public Injectable<UpdateRequest> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<UpdateRequest>()
	{
	    @Override
	    public UpdateRequest getValue()
	    {
                return getUpdateRequest();
	    }
	};
    }

    @Override
    public UpdateRequest getContext(Class<?> type)
    {
        return getUpdateRequest();
    }

    public UpdateRequest getUpdateRequest()
    {
        return getUpdateRequest(getOntClass(), getUriInfo().getAbsolutePath());
    }
    
    public final UpdateRequest getUpdateRequest(OntClass ontClass, URI uri)
    {
	if (ontClass.hasProperty(ResourceFactory.createProperty(SPIN.NS, "update")))
        {
            TemplateCall call = SPINFactory.asTemplateCall(ontClass.getProperty(ResourceFactory.createProperty(SPIN.NS, "update")).getObject());
            return getUpdateRequest(call, getOntModel().createResource(uri.toString()));
        }

        return null;
    }
    
    public UpdateRequest getUpdateRequest(TemplateCall call, Resource resource)
    {
	if (call == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	ParameterizedSparqlString queryString = new ParameterizedSparqlString(call.getQueryString(), qsm);
	return queryString.asUpdate();
    }
}
