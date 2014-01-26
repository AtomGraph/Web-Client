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
package org.graphity.client.model;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.jersey.api.core.ResourceConfig;
import java.net.URI;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.graphity.client.vocabulary.GC;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.update.UpdateBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Base class of generic read-write Graphity Client resources.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("{path: .*}")
public class ResourceBase extends org.graphity.processor.model.ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final URI mode;

    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * The URI of the resource being created is the current request URI (note: this is different from Server).
     * The sitemap ontology model and the SPARQL endpoint resource are injected via JAX-RS providers.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param sitemap sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param matchedOntClass the template class this resource matched
     * @param queryBuilder query builder that retrieves description of this resource
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @param mode <samp>mode</samp> query string param
     * @see org.graphity.processor.provider.OntologyProvider
     * @see org.graphity.processor.provider.SPARQLEndpointProvider
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint, @Context OntClass matchedOntClass,
            @Context QueryBuilder queryBuilder, @Context UpdateBuilder updateBuilder, @Context CacheControl cacheControl,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI,
	    @QueryParam("mode") URI mode)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		sitemap.createOntResource(uriInfo.getAbsolutePath().toString()), endpoint,
                matchedOntClass, queryBuilder, updateBuilder, cacheControl,
		limit, offset, orderBy, desc, graphURI, mode);	
    }

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param ontResource this resource as OWL resource
     * @param endpoint SPARQL endpoint of this resource
     * @param matchedOntClass the template class this resource matched
     * @param queryBuilder query builder that retrieves description of this resource
     * @param updateBuilder
     * @param cacheControl
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @param mode <samp>mode</samp> query string param
     */
    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders,ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint,
            OntClass matchedOntClass, QueryBuilder queryBuilder, UpdateBuilder updateBuilder, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI, URI mode)
    {
	super(uriInfo, request, httpHeaders, resourceConfig,
		ontResource, endpoint,
                matchedOntClass, queryBuilder, updateBuilder, cacheControl,
		limit, offset, orderBy, desc, graphURI);
	this.mode = mode;
    }

    @Override
    public Model describe()
    {	
        if (getMode() != null && hasRDFType(LDP.Container) &&
            (getMode().equals(URI.create(GC.CreateMode.getURI())) || getMode().equals(URI.create(GC.EditMode.getURI()))))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning default DESCRIBE Model", getMode());
	    return DataManager.get().loadModel(getModel(), getQuery(getURI()));
	}

	Model description = super.describe();

	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addRDFType(getMatchedOntClass());
	
	// set metadata properties after description query is executed
	getQueryBuilder().build(); // sets sp:text value
	if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, getQueryBuilder());
	setPropertyValue(SPIN.query, getQueryBuilder());

	return description;
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
	if (getMode() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Saxon-CE workaround", getMode());
	    List<Variant> list = super.getVariants();
            list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null));
            return list;
	}

        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.APPLICATION_XHTML_XML_TYPE, null, null));
        return list;
    }
    
    public URI getMode()
    {
	return mode;
    }

    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getMode() != null) return super.getPageUriBuilder().queryParam("mode", getMode());
	
	return super.getPageUriBuilder();
    }

    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getMode() != null) return super.getPreviousUriBuilder().queryParam("mode", getMode());
	
	return super.getPreviousUriBuilder();
    }

    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getMode() != null) return super.getNextUriBuilder().queryParam("mode", getMode());
	
	return super.getNextUriBuilder();
    }
    
}