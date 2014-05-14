/*
 * Copyright (C) 2013 Martynas
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.List;
import javax.naming.ConfigurationException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.graphity.server.util.DataManager;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of Graph Store proxies.
 * Given a remote store, it functions as a proxy and forwards SPARQL Graph Store protocol requests to a remote graph store.
 * Otherwise, the store serves the sitemap ontology of the application.
 * 
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a>
 */
@Path("/service") // not standard
public class GraphStoreBase extends org.graphity.server.model.GraphStoreBase
{
    private static final Logger log = LoggerFactory.getLogger(GraphStoreBase.class);

    private final UriInfo uriInfo;
    
    public GraphStoreBase(@Context OntModel sitemap, @Context DataManager dataManager,
            @Context UriInfo uriInfo, @Context Request request, @Context ResourceConfig resourceConfig)
    {
        this(sitemap.createResource(uriInfo.getBaseUriBuilder().
                path(GraphStoreBase.class).
                build().
                toString()),
            dataManager, uriInfo, request, resourceConfig);
    }

    public GraphStoreBase(Resource graphStore, DataManager dataManager, UriInfo uriInfo, Request request, ResourceConfig resourceConfig)
    {
        super(graphStore, dataManager, request, resourceConfig);
        
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	this.uriInfo = uriInfo;
        
        if (graphStore.isURIResource() && !getDataManager().hasServiceContext(graphStore))
        {
            if (log.isDebugEnabled()) log.debug("Adding service Context for local Graph Store with URI: {}", graphStore.getURI());
            dataManager.addServiceContext(graphStore);
        }
    }

    /**
     * Builds a list of acceptable response variants
     * 
     * @return supported variants
     */
    @Override
    public List<Variant> getVariants()
    {
        // workaround for Saxon-CE - it currently seems to run only in HTML mode (not XHTML)
        // https://saxonica.plan.io/issues/1447
        /*
	if (getMode() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Saxon-CE workaround", getMode());
	    List<Variant> list = super.getVariants();
            list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null));
            return list;
	}
        */
        
        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null)); // TO-DO: move this out to Client!
        return list;
    }
    
    /**
     * Returns  SPARQL service resource for site resource.
     * 
     * @param property property pointing to service resource
     * @return service resource
     */
    public Resource getService(Property property)
    {
        return getModel().createResource(getUriInfo().getBaseUri().toString()).
                getPropertyResourceValue(property);
    }

     /**
     * Returns configured Graph Store resource.
     * 
     * @return graph store resource
     */
    @Override
    public Resource getOrigin()
    {
        Resource service = getService(GP.service);
        if (service != null) return getOrigin(service);
        else return null;
    }

     /**
     * Returns configured Graph Store resource for a given service.
     * 
     * @param service SPARQL service
     * @return graph store resource
     */
    public Resource getOrigin(Resource service)
    {
        if (service == null) throw new IllegalArgumentException("Service resource cannot be null");

        try
        {
            Resource graphStore = getGraphStore(service);
            if (graphStore == null) throw new ConfigurationException("Configured SPARQL service (gp:service in sitemap ontology) does not have a Graph Store (gp:graphStore)");
            
            putAuthContext(service, graphStore);
            
            return graphStore;
        }
        catch (ConfigurationException ex)
        {
            if (log.isErrorEnabled()) log.warn("Graph Store configuration error", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);            
        }
    }
       
    /**
     * Returns Graph Store resource for the supplied SPARQL service.
     * Uses <code>gp:graphStore</code> parameter value from current SPARQL service resource.
     * 
     * @param service SPARQL service resource
     * @return service resource
     */
    public Resource getGraphStore(Resource service)
    {
        if (service == null) throw new IllegalArgumentException("SPARQL service resource cannot be null");
        return service.getPropertyResourceValue(GP.graphStore);
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
    
}