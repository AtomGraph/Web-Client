/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.hp.hpl.jena.update.UpdateRequest;
import com.sun.jersey.api.core.ResourceConfig;
import javax.naming.ConfigurationException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.util.DataManager;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of SPARQL endpoints proxies.
 * Given a remote endpoint, it functions as a proxy and forwards SPARQL HTTP protocol requests to a remote SPARQL endpoint.
 * Otherwise, the endpoint serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol for RDF</a>
 */
@Path("/sparql")
public class SPARQLEndpointBase extends org.graphity.server.model.SPARQLEndpointBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointBase.class);

    private final UriInfo uriInfo;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * Uses <code>void:sparqlEndpoint</code> parameter value from web.xml as endpoint URI, if present.
     * Otherwise, uses <code>@Path</code> annotation value for this class (usually <code>/sparql</code> to
     * build local endpoint URI.
     * 
     * @param sitemap ontology of this webapp
     * @param dataManager RDF data manager for this endpoint
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     */
    public SPARQLEndpointBase(@Context OntModel sitemap, @Context DataManager dataManager,
            @Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig)
    {
	this(sitemap.createResource(uriInfo.getBaseUriBuilder().
                path(SPARQLEndpointBase.class).
                build().
                toString()),
            dataManager, uriInfo, request, resourceConfig);
    }
    
    /**
     * Protected constructor with explicit endpoint resource.
     * Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param endpoint RDF resource of this endpoint (must be URI resource, not a blank node)
     * @param dataManager RDF data manager for this endpoint
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param resourceConfig webapp configuration
     */
    protected SPARQLEndpointBase(Resource endpoint, DataManager dataManager, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
	super(endpoint, dataManager, request, resourceConfig);

	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	this.uriInfo = uriInfo;

        if (endpoint.isURIResource() && !dataManager.hasServiceContext(endpoint))
        {
            if (log.isDebugEnabled()) log.debug("Adding service Context for local SPARQL endpoint with URI: {}", endpoint.getURI());
            dataManager.addServiceContext(endpoint);
        }        
    }

    /**
     * Returns  SPARQL service resource for site resource.
     * 
     * @param property property pointing to service resource
     * @return service resource
     */
    public Resource getService(Property property)
    {
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        return getModel().createResource(getUriInfo().getBaseUri().toString()).
                getPropertyResourceValue(property);
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
        Resource service = getService(GP.service);
        if (service != null) return getOrigin(service);
        else return null;
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
     * Loads RDF model by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param query query object
     * @return loaded model
     */
    @Override
    public Model loadModel(Query query)
    {
        Resource remote = getOrigin();
	if (remote == null || remote.equals(this))
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from Model using Query: {}", query);
	    return getDataManager().loadModel(getModel(), query);
	}

        return super.loadModel(query);
    }

    /**
     * Loads RDF model by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param query query object
     * @return loaded model
     */
    @Override
    public ResultSetRewindable loadResultSetRewindable(Query query)
    {
        Resource remote = getOrigin();
	if (remote == null || remote.equals(this))
	{
	    if (log.isDebugEnabled()) log.debug("Loading ResultSet from Model using Query: {}", query);
	    return getDataManager().loadResultSet(getModel(), query);
	}
        
        return super.loadResultSetRewindable(query);
    }

    /**
     * Asks for boolean result by querying either local or remote SPARQL endpoint (depends on its URI).
     * 
     * @param query query object
     * @return boolean result
     */
    @Override
    public boolean ask(Query query)
    {
        Resource remote = getOrigin();
	if (remote == null || remote.equals(this))
	{
	    if (log.isDebugEnabled()) log.debug("Loading Model from Model using Query: {}", query);
	    return getDataManager().ask(getModel(), query);
	}

        return super.ask(query);
    }

    @Override
    public void update(UpdateRequest updateRequest)
    {
        Resource remote = getOrigin();
	if (remote == null || remote.equals(this))
	{
	    if (log.isDebugEnabled()) log.debug("Attempting to update local Model, discarding UpdateRequest: {}", updateRequest);
	}

        super.update(updateRequest);
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

}