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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.http.Service;
import javax.naming.ConfigurationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import org.graphity.processor.model.Application;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SD;
import org.graphity.server.model.SPARQLEndpointOrigin;
import org.graphity.server.model.SPARQLEndpointOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class SPARQLEndpointOriginProvider extends org.graphity.server.provider.SPARQLEndpointOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointOriginProvider.class);

    public Application getApplication()
    {
	ContextResolver<Application> cr = getProviders().getContextResolver(Application.class, null);
	return cr.getContext(Application.class);
    }

    /**
     * Returns configured SPARQL endpoint resource.
     * This endpoint is a proxy for the remote endpoint.
     * 
     * @return endpoint resource
     */
    @Override
    public SPARQLEndpointOrigin getSPARQLEndpointOrigin()
    {
        try
        {
            Resource service = getApplication().getPropertyResourceValue(GP.service);
            if (service == null) throw new ConfigurationException("Remote RDF service (gp:service) is not configured in the sitemap ontology");
            return getOrigin(service);
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
        }
    }

    /**
     * Returns configured SPARQL endpoint resource for a given service.
     * 
     * @param service SPARQL service
     * @return endpoint resource
     */
    public SPARQLEndpointOrigin getOrigin(Resource service)
    {
        if (service == null) throw new IllegalArgumentException("Service resource cannot be null");

        try
        {
            Resource remote = service.getPropertyResourceValue(SD.endpoint);
            if (remote == null) throw new ConfigurationException("Configured SPARQL endpoint (sd:endpoint in the sitemap ontology) does not have an endpoint (sd:endpoint)");
            //if (remote.equals(this)) throw new ConfigurationException("Configured SPARQL endpoint (sd:endpoint in the sitemap ontology) is not remote. This will lead to a request loop");

            putAuthContext(service, remote);

            return new SPARQLEndpointOriginBase(remote.getURI());
        }
        catch (ConfigurationException ex)
        {
            if (log.isErrorEnabled()) log.warn("SPARQL endpoint configuration error", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);            
        }
    }
    
    /**
     * Configures HTTP Basic authentication for SPARQL endpoint context
     * 
     * @param service service resource
     * @param endpoint endpoint resource
     */
    public void putAuthContext(Resource service, Resource endpoint)
    {
        if (service == null) throw new IllegalArgumentException("SPARQL service resource cannot be null");
        if (endpoint == null) throw new IllegalArgumentException("SPARQL endpoint resource cannot be null");
        if (!endpoint.isURIResource()) throw new IllegalArgumentException("SPARQL endpoint must be URI resource");

        Property userProp = ResourceFactory.createProperty(Service.queryAuthUser.getSymbol());            
        String username = null;
        if (service.getProperty(userProp) != null && service.getProperty(userProp).getObject().isLiteral())
            username = service.getProperty(userProp).getLiteral().getString();
        Property pwdProp = ResourceFactory.createProperty(Service.queryAuthPwd.getSymbol());
        String password = null;
        if (service.getProperty(pwdProp) != null && service.getProperty(pwdProp).getObject().isLiteral())
            password = service.getProperty(pwdProp).getLiteral().getString();

        if (username != null & password != null)
            getDataManager().putAuthContext(endpoint.getURI(), username, password);
    }
    
}
