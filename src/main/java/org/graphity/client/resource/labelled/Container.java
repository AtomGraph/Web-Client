/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.client.resource.labelled;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.core.ResourceContext;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.model.impl.ResourceBase;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.vocabulary.GP;
import org.graphity.server.model.GraphStore;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Container extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    private final String searchString;
    
    public Container(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig,
            @Context SPARQLEndpoint endpoint, @Context GraphStore graphStore,
            @Context OntClass ontClass, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, request, servletConfig,
                endpoint, graphStore,
                ontClass, httpHeaders, resourceContext);
	this.searchString = getUriInfo().getQueryParameters().getFirst(RDFS.label.getLocalName());
    }

    @Override
    public void init()
    {
        super.init();

	if (!(getSearchString() == null || getSearchString().isEmpty()) && getMatchedOntClass().hasSuperClass(GP.Container))
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
    public UriBuilder getPageUriBuilder(Long offset, Long limit, String orderBy, Boolean desc)
    {
	if (getSearchString() != null) return super.getPageUriBuilder(offset, limit, orderBy, desc).
                queryParam(RDFS.label.getLocalName(), getSearchString());
	
	return super.getPageUriBuilder(offset, limit, orderBy, desc);
    }

}