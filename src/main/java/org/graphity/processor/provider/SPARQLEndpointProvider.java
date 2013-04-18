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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.graphity.processor.model.SPARQLEndpointBase;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.vocabulary.VoID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class SPARQLEndpointProvider extends PerRequestTypeInjectableProvider<Context, SPARQLEndpoint>
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointProvider.class);

    //@Context Providers providers;
    @Context ResourceConfig resourceConfig;
    @Context UriInfo uriInfo;
    @Context Request request;
    
    public SPARQLEndpointProvider()
    {
	super(SPARQLEndpoint.class);
    }

    public Request getRequest()
    {
	return request;
    }

    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    @Override
    public Injectable<SPARQLEndpoint> getInjectable(ComponentContext cc, Context context)
    {
	//if (log.isDebugEnabled()) log.debug("OntologyProvider UriInfo: {} ResourceConfig.getProperties(): {}", uriInfo, resourceConfig.getProperties());
	
	return new Injectable<SPARQLEndpoint>()
	{
	    @Override
	    public SPARQLEndpoint getValue()
	    {
		OntModel sitemap = OntologyProvider.getOntology(getUriInfo(), getResourceConfig());
		
		return new SPARQLEndpointBase(getResourceConfig().getProperty(VoID.sparqlEndpoint.getURI()) == null ?
			sitemap.createResource(getUriInfo().getBaseUriBuilder().
			    path(SPARQLEndpointBase.class).
			    build().toString()) :
			ResourceFactory.createResource(getResourceConfig().getProperty(VoID.sparqlEndpoint.getURI()).toString()),
		    getUriInfo(), getRequest(), getResourceConfig());
			
		//return new SPARQLEndpointBase(getUriInfo()), getRequest(), getResourceConfig(),
		//	OntologyProvider.getOntology(getUriInfo(), getResourceConfig()));
	    }
	};
    }
    
}
