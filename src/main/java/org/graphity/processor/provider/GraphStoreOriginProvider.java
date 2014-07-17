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
import javax.ws.rs.ext.ContextResolver;
import org.graphity.processor.model.Application;
import org.graphity.processor.vocabulary.GP;
import org.graphity.server.model.GraphStoreOrigin;
import org.graphity.server.model.impl.GraphStoreOriginBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class GraphStoreOriginProvider extends org.graphity.server.provider.GraphStoreOriginProvider
{

    private static final Logger log = LoggerFactory.getLogger(GraphStoreOriginProvider.class);

    public Application getApplication()
    {
	ContextResolver<Application> cr = getProviders().getContextResolver(Application.class, null);
	return cr.getContext(Application.class);
    }

    @Override
    public GraphStoreOrigin getGraphStoreOrigin()
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
     * Returns configured Graph Store resource for a given service.
     * 
     * @param service SPARQL service
     * @return graph store resource
     * @throws javax.naming.ConfigurationException
     */
    public GraphStoreOrigin getOrigin(Resource service) throws ConfigurationException
    {
        if (service == null) throw new IllegalArgumentException("Service resource cannot be null");

        Resource remote = service.getPropertyResourceValue(GP.graphStore);
        if (remote == null) throw new ConfigurationException("Configured SPARQL service (gp:service in sitemap ontology) does not have a Graph Store (gp:graphStore)");
        //if (remote.equals(this)) throw new ConfigurationException("Configured SPARQL service (gp:service in sitemap ontology) is not remote. This will lead to a request loop");

        putAuthContext(service, remote);

        return new GraphStoreOriginBase(remote.getURI());
    }

    /**
     * Configures HTTP Basic authentication for Graph Store Protocol context
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
