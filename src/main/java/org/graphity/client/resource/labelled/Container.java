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
package org.graphity.client.resource.labelled;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.model.impl.ResourceBase;
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
    
    public Container(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntModel ontModel,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
            @QueryParam("limit") Long limit,
	    @QueryParam("offset") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode,
            @QueryParam("label") String searchString)
    {
	super(uriInfo, endpoint, ontModel,
                request, servletContext, httpHeaders, resourceContext,
		limit, offset, orderBy, desc, graphURI, mode);
	this.searchString = searchString;	
    }

    @Override
    public void init()
    {
        super.init();
        
	if (!(getSearchString() == null || getSearchString().isEmpty()) && getMatchedOntClass().hasSuperClass(LDP.Container))
	{
            SelectBuilder selectBuilder = getQueryBuilder().getSubSelectBuilder();
	    if (selectBuilder != null)
	    {
                selectBuilder.filter(RDFS.label.getLocalName(), getQueryBuilder().quoteRegexMeta(getSearchString())); // escape special regex() characters!
		if (log.isDebugEnabled()) log.debug("Search query: {} QueryBuilder: {}", getSearchString(), getQueryBuilder());
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