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

package org.graphity.processor.model;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.http.Service;
import javax.naming.ConfigurationException;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SD;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class SPARQLEndpointProxyBase extends org.graphity.server.model.SPARQLEndpointProxyBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final UriInfo uriInfo;
    private final javax.ws.rs.core.Application application;

    public SPARQLEndpointProxyBase(UriInfo uriInfo, Request request, ServletContext servletContext, DataManager dataManager,
            javax.ws.rs.core.Application application)
    {
        super(uriInfo, request, servletContext, dataManager);
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	if (application == null) throw new IllegalArgumentException("Application cannot be null");

        this.uriInfo = uriInfo;
        this.application = application;
        
        /*
        if (endpoint.isURIResource() && !dataManager.hasServiceContext(endpoint))
        {
            if (log.isDebugEnabled()) log.debug("Adding service Context for local SPARQL endpoint with URI: {}", endpoint.getURI());
            dataManager.addServiceContext(endpoint);
        }
        */
    }

    /**
     * Returns SPARQL service resource for site resource.
     * 
     * @param property property pointing to service resource
     * @return service resource
     * @throws javax.naming.ConfigurationException
     */
    
    public Resource getService(Property property) throws ConfigurationException
    {
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        
        return getApplication().getResource(getUriInfo()).getPropertyResourceValue(property);
    }

    /**
     * Returns configured SPARQL endpoint resource for a given service.
     * 
     * @param service SPARQL service
     * @return endpoint resource
     */
    public Resource getOrigin(Resource service)
    {
        if (service == null) throw new IllegalArgumentException("Service resource cannot be null");

        try
        {
            Resource endpoint = service.getPropertyResourceValue(SD.endpoint);
            if (endpoint == null) throw new ConfigurationException("Configured SPARQL endpoint (sd:endpoint in the sitemap ontology) does not have an endpoint (sd:endpoint)");

            putAuthContext(service, endpoint);

            return endpoint;
        }
        catch (ConfigurationException ex)
        {
            if (log.isErrorEnabled()) log.warn("SPARQL endpoint configuration error", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);            
        }
    }

    /**
     * Returns configured SPARQL endpoint resource.
     * This endpoint is a proxy for the remote endpoint.
     * 
     * @return endpoint resource
     */
    @Override
    public Resource getOrigin()
    {
        try
        {
            Resource service = getService(GP.service);
            if (service != null) return getOrigin(service);
            
            return null;
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex);
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

    /**
     * Returns URI information of the current request.
     * 
     * @return URI information
     */
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Application getApplication()
    {
        return (Application)application;
    }

}