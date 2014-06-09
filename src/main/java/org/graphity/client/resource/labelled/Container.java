/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.client.resource.labelled;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.model.ResourceBase;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/{path}/labelled")
public class Container extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    private final String searchString;
    
    public Container(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ServletContext servletContext,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") Long limit,
	    @QueryParam("offset") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
            @QueryParam("label") String searchString)
    {
	super(uriInfo, request, httpHeaders, servletContext,
		sitemap, endpoint,
		limit, offset, orderBy, desc, graphURI, mode);
	this.searchString = searchString;
	
	if (!(searchString == null || searchString.isEmpty()) && hasRDFType(LDP.Container))
	{
            SelectBuilder selectBuilder = getQueryBuilder().getSubSelectBuilder();
	    if (selectBuilder != null)
	    {
                selectBuilder.filter(RDFS.label.getLocalName(), getQueryBuilder().quoteRegexMeta(searchString)); // escape special regex() characters!
		if (log.isDebugEnabled()) log.debug("Search query: {} QueryBuilder: {}", searchString, getQueryBuilder());
	    }
        }	
    }

    public String getSearchString()
    {
	return searchString;
    }

    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getSearchString() != null) return super.getPageUriBuilder().queryParam("label", getSearchString());
	
	return super.getPageUriBuilder();
    }

    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getSearchString() != null) return super.getPreviousUriBuilder().queryParam("label", getSearchString());
	
	return super.getPreviousUriBuilder();
    }

    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getSearchString() != null) return super.getNextUriBuilder().queryParam("label", getSearchString());
	
	return super.getNextUriBuilder();
    }

}