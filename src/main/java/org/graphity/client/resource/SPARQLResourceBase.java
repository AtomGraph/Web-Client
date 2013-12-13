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
package org.graphity.client.resource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.client.model.ResourceBase;
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/sparql")
public class SPARQLResourceBase extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLResourceBase.class);

    private final Query userQuery;
    private final URI endpointURI;
    private final MediaType mediaType;

    public SPARQLResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
	    @QueryParam("query") Query userQuery,
	    @QueryParam("endpoint-uri") URI endpointURI,
	    @QueryParam("accept") MediaType mediaType)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		sitemap.createOntResource(uriInfo.getAbsolutePath().toString()), endpoint,
		limit, offset, orderBy, desc, graphURI, mode,
		userQuery, endpointURI, mediaType);
    }

    protected SPARQLResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI, URI mode,
	    Query userQuery, URI endpointURI, MediaType mediaType)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint,
		limit, offset, orderBy, desc, graphURI, mode);
	
	this.userQuery = userQuery;
	this.endpointURI = endpointURI;
	this.mediaType = mediaType;
	if (log.isDebugEnabled()) log.debug("Constructing SPARQLResourceBase with endpoint URI: {} and MediaType: {}", userQuery, mediaType);
    }

    @Override
    public Response get()
    {
	MediaType mostAcceptable = getHttpHeaders().getAcceptableMediaTypes().get(0);

	// don't create query resource if HTML is requested
	if (getUserQuery() != null && (mostAcceptable.isCompatible(org.graphity.server.MediaType.APPLICATION_RDF_XML_TYPE) ||
	    mostAcceptable.isCompatible(org.graphity.server.MediaType.TEXT_TURTLE_TYPE) ||
	    mostAcceptable.isCompatible(org.graphity.server.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE)))
	{
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is RDF or SPARQL Results, returning SPARQL Response", mostAcceptable);
            return getEndpoint().query(getUserQuery(), null, null);
	}
	else
	    if (log.isDebugEnabled()) log.debug("Requested MediaType: {} is not RDF, returning default Response", mostAcceptable);

	return super.get(); // if HTML is requested, return DESCRIBE ?this results instead of user query
    }

    /*
    @POST
    @Consumes(org.graphity.server.MediaType.APPLICATION_SPARQL_UPDATE)
    public Response updateDirectly(UpdateRequest update, @QueryParam("using-graph-uri") URI defaultGraphUri,
	@QueryParam("using-named-graph-uri") URI graphUri)
    {
        return getEndpoint().updateDirectly(update, null, null);
    }
    */
    
    public Query getUserQuery()
    {
	return userQuery;
    }

    public URI getEndpointURI()
    {
	return endpointURI;
    }

    @Override
    public SPARQLEndpoint getEndpoint()
    {
	if (getEndpointURI() != null)
	{
            if (log.isDebugEnabled()) log.debug("Querying user-provided endpoint: {}", getEndpointURI());
            return SPARQLEndpointFactory.createEndpoint(getOntModel().createResource(getEndpointURI().toString()),
                    getUriInfo(), getRequest(), getResourceConfig());            
        }
        
        return super.getEndpoint();
    }
    
    public MediaType getMediaType()
    {
	return mediaType;
    }

}
