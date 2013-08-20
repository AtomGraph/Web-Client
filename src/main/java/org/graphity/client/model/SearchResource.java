/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.client.model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.regex.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/search")
public class SearchResource extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

    public SearchResource(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig, @Context SecurityContext securityContext, @Context HttpContext httpContext,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("query") String searchString,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") @DefaultValue("label") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		sitemap, endpoint,
		limit, offset, orderBy, desc);
	
	//if (searchString == null)
	//    throw new WebApplicationException(Response.Status.BAD_REQUEST);
	
	if (hasRDFType(LDP.Container))
	{
	    getQueryBuilder().getSubSelectBuilder().
		filter(RDFS.label.getLocalName(), Pattern.compile(searchString));
	    if (log.isDebugEnabled()) log.debug("Search query: {} QueryBuilder: {}", searchString, getQueryBuilder());

	    if (hasProperty(XHV.prev))
		getProperty(XHV.prev).changeObject(getModel().createResource(getPreviousUriBuilder().
		    queryParam("query", searchString).build().toString()));
	    if (hasProperty(XHV.next))
		getProperty(XHV.next).changeObject(getModel().createResource(getNextUriBuilder().
		    queryParam("query", searchString).build().toString()));
	}
	
    }
    
}