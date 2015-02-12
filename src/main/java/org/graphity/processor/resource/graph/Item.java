/*
 * Copyright 2014 Martynas Jusevičius <martynas@graphity.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphity.processor.resource.graph;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.core.ResourceContext;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.processor.model.impl.ResourceBase;
import org.graphity.core.model.GraphStore;
import org.graphity.core.model.SPARQLEndpoint;
import org.graphity.core.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named graph resource.
 * Implements direct graph identification of the SPARQL Graph Store Protocol.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.core.model.GraphStore
 * @see <a href="http://www.w3.org/TR/sparql11-http-rdf-update/#direct-graph-identification">4.1 Direct Graph Identification</a>
 */
public class Item extends ResourceBase
{
    
    private static final Logger log = LoggerFactory.getLogger(Item.class);

    public Item(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig,
            @Context SPARQLEndpoint endpoint, @Context GraphStore graphStore,
            @Context OntClass matchedOntClass, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, request, servletConfig, endpoint, graphStore,
                matchedOntClass, httpHeaders, resourceContext);
	if (log.isDebugEnabled()) log.debug("Constructing {} as direct indication of GRAPH {}", getClass(), uriInfo.getAbsolutePath());
    }
    
    @Override
    public Response get()
    {
	if (log.isDebugEnabled()) log.debug("GET GRAPH {} from GraphStore {}", getRealURI(), getGraphStore());        
        return getResponse(getGraphStore().getModel(getRealURI().toString()));
    }

    @Override
    public Response post(Model model)
    {
	if (log.isDebugEnabled()) log.debug("POST GRAPH {} to GraphStore {}", getRealURI(), getGraphStore());
        return getGraphStore().post(model, Boolean.FALSE, getRealURI());
    }

    @Override
    public Response put(Model model)
    {
	Model existing = getGraphStore().getModel(getRealURI().toString());

	if (!existing.isEmpty()) // remove existing representation
	{
	    EntityTag entityTag = new EntityTag(Long.toHexString(ModelUtils.hashModel(model)));
	    ResponseBuilder rb = getRequest().evaluatePreconditions(entityTag);
	    if (rb != null)
	    {
		if (log.isDebugEnabled()) log.debug("PUT preconditions were not met for resource: {} with entity tag: {}", this, entityTag);
		return rb.build();
	    }
        }
        
        if (log.isDebugEnabled()) log.debug("PUT GRAPH {} to GraphStore {}", getRealURI(), getGraphStore());
        getGraphStore().put(model, Boolean.FALSE, getRealURI());
        
	if (existing.isEmpty()) return Response.created(getRealURI()).build();        
        else return Response.ok(model).build();
    }

    @Override
    public Response delete()
    {
	if (log.isDebugEnabled()) log.debug("DELETE GRAPH {} from GraphStore {}", getRealURI(), getGraphStore());
        return getGraphStore().delete(Boolean.FALSE, getRealURI());
    }
    
}
