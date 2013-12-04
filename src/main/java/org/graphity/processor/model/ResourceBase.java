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

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.uri.UriTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.update.InsertDataBuilder;
import org.graphity.processor.update.UpdateBuilder;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.LDA;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.VoID;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.server.model.LDPResource;
import org.graphity.server.model.QueriedResourceBase;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.model.SPARQLUpdateEndpoint;
import org.graphity.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Base class of generic read-write Graphity Processor resources.
 * Configured declaratively using sitemap ontology to provide Linked Data layer over a SPARQL endpoint.
 * Supports pagination on containers (implemented using SPARQL query solution modifiers).
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see PageResource
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/OntResource.html">OntResource</a>
 * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("{path: .*}")
public class ResourceBase extends QueriedResourceBase implements LDPResource, PageResource, OntResource
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final OntResource ontResource;
    private final Long limit, offset;
    private final String orderBy;
    private final Boolean desc;
    private final OntClass matchedOntClass;
    private final Resource dataset;
    private final SPARQLEndpoint endpoint;
    private final UriInfo uriInfo;
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final ResourceConfig resourceConfig;
    private final QueryBuilder queryBuilder;
    private final URI graphURI;
    private final CacheControl cacheControl;
    
    /**
     * JAX-RS-compatible resource constructor with injected initialization objects.
     * The URI of the resource being created is the current request URI (note: this is different from Server).
     * The sitemap ontology model and the SPARQL endpoint resource are injected via JAX-RS providers.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param resourceContext resource context
     * @param sitemap sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code> (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @see org.graphity.processor.provider.OntologyProvider
     * @see org.graphity.processor.provider.SPARQLEndpointProvider
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") @DefaultValue("false") Boolean desc,
	    @QueryParam("graph") URI graphURI)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		sitemap, endpoint,
		limit, offset, orderBy, desc, graphURI);
    }
    
    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param ontModel sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     */
    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntModel ontModel, SPARQLEndpoint endpoint,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		ontModel.createOntResource(uriInfo.getAbsolutePath().toString()), endpoint,
		limit, offset, orderBy, desc, graphURI);
	
	if (log.isDebugEnabled()) log.debug("Constructing Graphity processor ResourceBase");
    }

    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * If the request URI does not match any URI template in the sitemap ontology, 404 Not Found is returned.
     * 
     * If the matching ontology class is a subclass of LDP Page, this resource becomes a page resource and
     * HATEOS metadata is added (relations to the container and previous/next page resources).
     * 
     * If the matching ontology class has a value restriction on <code>void:inDataset</code> property, and
     * that dataset in turn has a SPARQL endpoint defined using <code>void:sparqlEndpoint</code>, that
     * endpoint resource overrides the default value supplied as constructor argument.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param resourceConfig webapp configuration
     * @param ontResource this resource as OWL resource
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @see <a href="http://en.wikipedia.org/wiki/HATEOAS">HATEOS</a>
     */
    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpoint endpoint,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI)
    {
	super(ontResource, endpoint, resourceConfig);

	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
	if (request == null) throw new IllegalArgumentException("Request cannot be null");
	if (httpHeaders == null) throw new IllegalArgumentException("HttpHeaders cannot be null");
	if (resourceConfig == null) throw new IllegalArgumentException("ResourceConfig cannot be null");
	if (desc == null) throw new IllegalArgumentException("DESC Boolean cannot be null");

	this.ontResource = ontResource;
	this.uriInfo = uriInfo;
	this.request = request;
	this.httpHeaders = httpHeaders;
	this.resourceConfig = resourceConfig;
	this.limit = limit;
	this.offset = offset;
	this.orderBy = orderBy;
	this.desc = desc;
	if (graphURI != null && graphURI.isAbsolute()) this.graphURI = graphURI;
	else this.graphURI = null;

	matchedOntClass = matchOntClass(getRealURI(), uriInfo.getBaseUri());
	if (matchedOntClass == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
	
        queryBuilder = setSelectModifiers(QueryBuilder.fromQuery(getQuery(matchedOntClass, ontResource), getModel()));
        if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with QueryBuilder: {}", queryBuilder);

	dataset = getDataset(matchedOntClass);
	if (dataset != null && dataset.hasProperty(VoID.sparqlEndpoint))
	    this.endpoint = new SPARQLEndpointBase(dataset.getPropertyResourceValue(VoID.sparqlEndpoint),
		    uriInfo, request, resourceConfig);
	else this.endpoint = endpoint;	    
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with Dataset: {} and SPARQL endpoint: {}", dataset, this.endpoint);	

        cacheControl = getCacheControl(matchedOntClass);
    }

    /**
     * Handles GET request and returns response with RDF description of this resource.
     * In case this resource is a container, a redirect to its first page is returned.
     * 
     * @return response with RDF description, or a redirect in case of container
     */
    @Override
    public Response get()
    {
	 // ldp:Container always redirects to first ldp:Page
	if (hasRDFType(LDP.Container) && getRealURI().equals(getUriInfo().getRequestUri()))
	{
	    if (log.isDebugEnabled()) log.debug("OntResource is ldp:Container, redirecting to the first ldp:Page");	    
	    return Response.seeOther(getPageUriBuilder().build()).build();
	}

	return super.get();
    }
    
    /**
     * Handles POST method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @return response
     */
    @Override
    public Response post(Model model)
    {
	return post(model, getEndpoint());
    }

    /**
     * Handles POST method, stores the submitted RDF model in the specified named graph on the default SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param graphURI target graph name
     * @return response
     */
    public Response post(Model model, URI graphURI)
    {
	return post(model, graphURI, getEndpoint());
    }

    /**
     * Handles POST method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response post(Model model, SPARQLUpdateEndpoint endpoint)
    {
	return post(model, getGraphURI(), endpoint);
    }
    
    /**
     * Handles POST method, stores the submitted RDF model in the specified named graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param graphURI target graph name
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response post(Model model, URI graphURI, SPARQLUpdateEndpoint endpoint)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (endpoint == null) throw new IllegalArgumentException("SPARQL update endpoint cannot be null");
	if (log.isDebugEnabled()) log.debug("POST GRAPH URI: {} SPARQLUpdateEndpoint: {}", graphURI, endpoint);
	if (log.isDebugEnabled()) log.debug("POSTed Model: {}", model);

	Resource created = getURIResource(model, FOAF.Document);
	if (created == null)
	{
	    if (log.isDebugEnabled()) log.debug("POSTed Model does not contain statements with URI as subject and type '{}'", FOAF.Document.getURI());
	    throw new WebApplicationException(Response.Status.BAD_REQUEST);
	}

	UpdateRequest insertDataRequest; 
	if (graphURI != null) insertDataRequest = InsertDataBuilder.fromData(graphURI, model).build();
	else insertDataRequest = InsertDataBuilder.fromData(model).build();
	if (log.isDebugEnabled()) log.debug("INSERT DATA request: {}", insertDataRequest);

	endpoint.update(insertDataRequest, null, null);
	
	URI createdURI = UriBuilder.fromUri(created.getURI()).build();
	if (log.isDebugEnabled()) log.debug("Redirecting to POSTed Resource URI: {}", createdURI);
	// http://stackoverflow.com/questions/3383725/post-redirect-get-prg-vs-meaningful-2xx-response-codes
	// http://www.blackpepper.co.uk/posts/201-created-or-post-redirect-get/
	//return Response.created(createdURI).entity(model).build();
	return Response.seeOther(createdURI).build();
    }

    public Resource getURIResource(Model model, Resource type)
    {
	ResIterator resIt = model.listSubjectsWithProperty(RDF.type, type);

	try
	{
	    while (resIt.hasNext())
	    {
		Resource created = resIt.next();

		if (created.isURIResource()) return created;
	    }
	}
	finally
	{
	    resIt.close();
	}
	
	return null;
    }
    
    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @return response
     */
    @Override
    public Response put(Model model)
    {
	return put(model, getEndpoint());
    }

    /**
     * Handles PUT method, stores the submitted RDF model in the specified named graph of default SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @param graphURI target graph name
     * @return response
     */
    public Response put(Model model, URI graphURI)
    {
	return put(model, graphURI, getEndpoint());
    }

    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response put(Model model, SPARQLUpdateEndpoint endpoint)
    {
	return put(model, getGraphURI(), endpoint);
    }
    
    /**
     * Handles PUT method, stores the submitted RDF model in the specified named graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @param graphURI target graph name
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response put(Model model, URI graphURI, SPARQLUpdateEndpoint endpoint)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (endpoint == null) throw new IllegalArgumentException("SPARQL update endpoint cannot be null");
	if (log.isDebugEnabled()) log.debug("PUT GRAPH URI: {} SPARQLUpdateEndpoint: {}", graphURI, endpoint);
	if (log.isDebugEnabled()) log.debug("PUT Model: {}", model);

	if (!model.containsResource(this))
	{
	    if (log.isDebugEnabled()) log.debug("PUT Model does not contain statements with request URI '{}' as subject", getURI());
	    throw new WebApplicationException(Response.Status.BAD_REQUEST);
	}
	
	Model description = describe();	
	UpdateRequest updateRequest = UpdateFactory.create();
	
	if (!description.isEmpty()) // remove existing representation
	{
	    EntityTag entityTag = new EntityTag(Long.toHexString(ModelUtils.hashModel(model)));
	    Response.ResponseBuilder rb = getRequest().evaluatePreconditions(entityTag);
	    if (rb != null)
	    {
		if (log.isDebugEnabled()) log.debug("PUT preconditions were not met for resource: {} with entity tag: {}", this, entityTag);
		return rb.build();
	    }
	    
	    UpdateRequest deleteRequest = getUpdateRequest(getMatchedOntClass(), this);
	    if (log.isDebugEnabled()) log.debug("DELETE UpdateRequest: {}", deleteRequest);
	    Iterator<com.hp.hpl.jena.update.Update> it = deleteRequest.getOperations().iterator();
	    while (it.hasNext()) updateRequest.add(it.next());
	}
	
	UpdateRequest insertDataRequest; 
	if (graphURI != null) insertDataRequest = InsertDataBuilder.fromData(graphURI, model).build();
	else insertDataRequest = InsertDataBuilder.fromData(model).build();
	if (log.isDebugEnabled()) log.debug("INSERT DATA request: {}", insertDataRequest);
	Iterator<com.hp.hpl.jena.update.Update> it = insertDataRequest.getOperations().iterator();
	while (it.hasNext()) updateRequest.add(it.next());
	
	if (log.isDebugEnabled()) log.debug("Combined DELETE/INSERT DATA request: {}", updateRequest);
	endpoint.update(updateRequest, null, null);
	
	if (description.isEmpty()) return Response.created(getRealURI()).build();
	else return getResponse(model);
    }

    /**
     * Handles DELETE method, deletes the RDF representation of this resource from the default SPARQL endpoint, and
     * returns response.
     * 
     * @return response
     */
    @Override
    public Response delete()
    {
	return delete(getEndpoint());
    }
    
    /**
     * Handles DELETE method, deletes the RDF representation of this resource from the specified SPARQL endpoint, and
     * returns response.
     * 
     * @param endpoint target SPARQL endpoint
     * @return response
     */    
    public Response delete(SPARQLUpdateEndpoint endpoint)
    {
	if (log.isDebugEnabled()) log.debug("DELETEing resource: {} matched OntClass: {}", this, getMatchedOntClass());
	UpdateRequest deleteRequest = getUpdateRequest(getMatchedOntClass(), this);
	if (log.isDebugEnabled()) log.debug("DELETE UpdateRequest: {}", deleteRequest);
	endpoint.update(deleteRequest, null, null);
	
	return Response.noContent().build();
    }

    /**
     * Returns RDF description of this resource.
     * 
     * @return RDF description
     * @see getQuery()
     */
    @Override
    public Model describe()
    {	
	Model description = super.describe();
	if (log.isDebugEnabled()) log.debug("Generating Response from description Model with {} triples", description.size());

	if (hasRDFType(LDP.Container))
	{
	    if (log.isDebugEnabled()) log.debug("Adding PageResource metadata: ldp:pageOf {}", this);
	    Resource page = description.createResource(getPageUriBuilder().build().toString()).
		addProperty(RDF.type, LDP.Page).
                addProperty(LDP.pageOf, this);

	    if (getOffset() >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:previous {}", getURI(), getPreviousUriBuilder().build().toString());
		page.addProperty(XHV.prev, description.createResource(getPreviousUriBuilder().build().toString()));
	    }

	    // no way to know if there's a next page without counting results (either total or in current page)
	    //int subjectCount = describe().listSubjects().toList().size();
	    //log.debug("describe().listSubjects().toList().size(): {}", subjectCount);
	    //if (subjectCount >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:next {}", getURI(), getNextUriBuilder().build().toString());
		page.addProperty(XHV.next, description.createResource(getNextUriBuilder().build().toString()));
	    }
	}

	return description;
    }

    /**
     * Returns dataset resource, specified in an ontology class restriction.
     * 
     * @param ontClass the ontology class with the restriction
     * @return dataset resource or null, if no dataset restriction was found
     */
    public final Resource getDataset(OntClass ontClass)
    {
	RDFNode hasValue = getRestrictionHasValue(ontClass, VoID.inDataset);
	if (hasValue != null && hasValue.isResource()) return hasValue.asResource();

	return null;
    }

    /**
     * Returns `Cache-Control` HTTP header value, specified in an ontology class restriction.
     * 
     * @param ontClass the ontology class with the restriction
     * @return CacheControl instance or null, if no dataset restriction was found
     */
    public final CacheControl getCacheControl(OntClass ontClass)
    {
	RDFNode hasValue = getRestrictionHasValue(ontClass, GP.cacheControl);
	if (hasValue != null && hasValue.isLiteral())
        {
            String controlString = hasValue.asLiteral().getString();
            return CacheControl.valueOf(controlString); // will fail on bad config
        }

	return null;
    }

    /**
     * Sets <code>SELECT</code> on a supplied query builder, if it contains such sub-query.
     * 
     * @param queryBuilder query builder
     * @return query builder with set modifiers
     * @see org.graphity.processor.query.QueryBuilder
     */
    public final QueryBuilder setSelectModifiers(QueryBuilder queryBuilder)
    {
        if (queryBuilder == null) throw new IllegalArgumentException("QueryBuilder cannot be null");
        
        SelectBuilder selectBuilder = queryBuilder.getSubSelectBuilder();
        if (selectBuilder != null) setSelectModifiers(selectBuilder);
        
        return queryBuilder;
    }

    /**
     * Sets <code>SELECT</code> on a supplied builder.
     * This method is used to build queries for page resources. It sets <code>LIMIT</code> and <code>OFFSET</code>
     * modifiers as well as <code>ORDER BY</code>/<code>DESC</code> clauses on <code>SELECT</code> sub-queries.
     * Currently only one <code>ORDER BY</code> condition is supported.
     * 
     * @param selectBuilder SELECT builder
     * @return SELECT builder with set modifiers
     * @see org.graphity.processor.query.SelectBuilder
     */
    public SelectBuilder setSelectModifiers(SelectBuilder selectBuilder)
    {	
        if (selectBuilder == null) throw new IllegalArgumentException("SelectBuilder cannot be null");
        selectBuilder.replaceLimit(getLimit()).replaceOffset(getOffset());

        if (getOrderBy() != null)
        {
            try
            {
                selectBuilder.replaceOrderBy(null). // any existing ORDER BY condition is removed first
                    orderBy(getOrderBy(), getDesc());
            }
            catch (IllegalStateException ex)
            {
                if (log.isWarnEnabled()) log.warn("Tried to use ORDER BY variable ?{} which is not present in the WHERE pattern", getOrderBy());
            }
        }
        
	return selectBuilder;
    }

    /**
     * Given an RDF resource, returns a SPARQL query that can be used to retrieve its description.
     * The query is built using a SPIN template call attached to the ontology class that this resource matches.
     * 
     * @param resource the resource to be described
     * @return query object
     */
    public Query getQuery(Resource resource)
    {
	return getQuery(getMatchedOntClass(), resource);
    }

    /**
     * Given an RDF resource and an ontology class that it belongs to, returns a SPARQL query that can be used
     * to retrieve its description.
     * The ontology class must have a SPIN template call attached (using <code>spin:constraint</code>).
     * 
     * @param ontClass ontology class of the resource
     * @param resource resource to be described
     * @return query object
     * @see org.topbraid.spin.model.TemplateCall
     */
    public final Query getQuery(OntClass ontClass, Resource resource)
    {
	return getQuery(getQueryCall(ontClass), resource);
    }
    
    /**
     * Given an ontology class, returns the SPIN template call attached to it.
     * The class must have a <code>spin:query</code> property with the template call resource as object.
     * 
     * @param ontClass ontology class
     * @return SPIN template call resource
     * @see org.topbraid.spin.model.TemplateCall
     */
    public TemplateCall getQueryCall(OntClass ontClass)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (!ontClass.hasProperty(SPIN.query))
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN query Template");	    

	RDFNode constraint = getModel().getResource(ontClass.getURI()).getProperty(SPIN.query).getObject();
	return SPINFactory.asTemplateCall(constraint);
    }
    
    /**
     * Given a SPIN template call and an RDF resource, returns a SPARQL query that can be used to retrieve
     * resource's description.
     * Following the convention of SPIN API, variable name <code>?this</code> has a special meaning and
     * is assigned to the value of the resource (which is usually this resource).
     * If this resource is a page resource, the SELECT sub-query modifiers (<code>LIMIT</code> and
     * <code>OFFSET</code>) will be set to implement pagination.
     * 
     * @param call SPIN template call resource
     * @param resource RDF resource
     * @return query object
     */
    public Query getQuery(TemplateCall call, Resource resource)
    {
	if (call == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	ParameterizedSparqlString queryString = new ParameterizedSparqlString(call.getQueryString(), qsm);
	return queryString.asQuery();
    }

    public final UpdateBuilder getUpdateBuilder(Update update)
    {
	UpdateBuilder ub = UpdateBuilder.fromUpdate(update);
	
	return ub;
    }
    
    public final UpdateRequest getUpdateRequest(OntClass ontClass, Resource resource)
    {
	return getUpdateRequest(getUpdateCall(ontClass), resource);
    }

    public TemplateCall getUpdateCall(OntClass ontClass)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (!ontClass.hasProperty(ResourceFactory.createProperty(SPIN.NS, "update")))
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN update Template");	    

	RDFNode update = getModel().getResource(ontClass.getURI()).
		getProperty(ResourceFactory.createProperty(SPIN.NS, "update")).getObject();
	return SPINFactory.asTemplateCall(update);
    }
    
    public UpdateRequest getUpdateRequest(TemplateCall call, Resource resource)
    {
	if (call == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	ParameterizedSparqlString queryString = new ParameterizedSparqlString(call.getQueryString(), qsm);
	return queryString.asUpdate();
    }

    /**
     * Given an absolute URI and a base URI, returns ontology class with a matching URI template, if any.
     * 
     * @param uri absolute URI being matched
     * @param base base URI
     * @return matching ontology class or null, if none
     */
    public final OntClass matchOntClass(URI uri, URI base)
    {
	if (uri == null) throw new IllegalArgumentException("URI being matched cannot be null");
	if (base == null) throw new IllegalArgumentException("Base URI cannot be null");
	if (!uri.isAbsolute()) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not absolute");
	if (base.relativize(uri).equals(uri)) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not relative to the base URI \"" + base + "\"");
	    
	StringBuilder path = new StringBuilder();
	// instead of path, include query string by relativizing request URI against base URI
	path.append("/").append(base.relativize(uri));
	return matchOntClass(path);
    }

    /**
     * Given a relative URI, returns ontology class with a matching URI template, if any.
     * URIs are matched against the URI templates specified in ontology class restrictions in the sitemap
     * ontology. This method uses Jersey implementation of the JAX-RS URI matching algorithm.
     * The URI template restrictions use <code>lda:uriTemplate</code> property (from Linked Data API) with
     * template string as the object literal. Note: the property might change in future processor versions.
     * 
     * @param path absolute path (relative URI)
     * @return matching ontology class or null, if none
     * @see <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec3.html#x3-340003.7">3.7 Matching Requests to Resource Methods (JAX-RS 1.1)</a>
     * @see <a href="https://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/uri/UriTemplate.html">Jersey UriTemplate</a>
     * @see <a href="https://code.google.com/p/linked-data-api/wiki/API_Vocabulary">Linked Data API Vocabulary</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public final OntClass matchOntClass(CharSequence path)
    {
	if (path == null) throw new IllegalArgumentException("Path being matched cannot be null");
	ExtendedIterator<Restriction> it = getOntModel().listRestrictions();

	try
	{
	    TreeMap<UriTemplate, OntClass> matchedClasses = new TreeMap<>(UriTemplate.COMPARATOR);

	    while (it.hasNext())
	    {
		Restriction restriction = it.next();	    
		if (restriction.canAs(HasValueRestriction.class))
		{
		    HasValueRestriction hvr = restriction.asHasValueRestriction();
		    if (hvr.getOnProperty().equals(LDA.uriTemplate))
		    {
			UriTemplate uriTemplate = new UriTemplate(hvr.getHasValue().toString());
			HashMap<String, String> map = new HashMap<>();

			if (uriTemplate.match(path, map))
			{
			    if (log.isDebugEnabled()) log.debug("Path {} matched UriTemplate {}", path, uriTemplate);

			    OntClass ontClass = hvr.listSubClasses(true).next(); //hvr.getSubClass();	    
			    if (log.isDebugEnabled()) log.debug("Path {} matched endpoint OntClass {}", path, ontClass);
			    matchedClasses.put(uriTemplate, ontClass);
			}
			else
			    if (log.isDebugEnabled()) log.debug("Path {} did not match UriTemplate {}", path, uriTemplate);
		    }
		}
	    }
	    
	    if (!matchedClasses.isEmpty())
	    {
		if (log.isDebugEnabled()) log.debug("Matched UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
		return matchedClasses.firstEntry().getValue();
	    }

	    if (log.isDebugEnabled()) log.debug("Path {} has no OntClass match in this OntModel", path);
	    return null;
	}
	finally
	{
	    it.close();
	}	
    }

    /**
     * Returns value of "limit" query string parameter, which indicates the number of resources per page.
     * This value is set as <code>LIMIT</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * 
     * @return limit value
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modResultLimit">15.5 LIMIT</a>
     */
    @Override
    public final Long getLimit()
    {
	return limit;
    }

    /**
     * Returns value of "offset" query string parameter, which indicates the number of resources the page
     * has skipped from the start of the container.
     * This value is set as <code>OFFSET</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * 
     * @return offset value
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOffset">15.4 OFFSET</a>
     */
    @Override
    public final Long getOffset()
    {
	return offset;
    }

    /**
     * Returns value of "order-by" query string parameter, which indicates the name of the variable after
     * which the container (and the page) is sorted.
     * This value is set as <code>ORDER BY</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * Note that ordering might be undefined, in which case the same page might not contain identical resources
     * during different requests.
     * 
     * @return name of ordering variable or null, if not specified
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    @Override
    public final String getOrderBy()
    {
	return orderBy;
    }

    /**
     * Returns value of "desc" query string parameter, which indicates the direction of resource ordering
     * in the container (and the page).
     * If this method returns true, <code>DESC</code> order modifier is set if this resource is a page
     * (therefore pagination is used).
     * 
     * @return true if the order is descending, false otherwise
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    @Override
    public final Boolean getDesc()
    {
	return desc;
    }

    public URI getGraphURI()
    {
	return graphURI;
    }

    /**
     * Given an ontology class and ontology property, returns value of <code>owl:hasValue</code> restriction,
     * if one is present.
     * The ontology class must be a subclass of the restriction, and the property must be used as
     * <code>owl:onProperty</code>.
     * 
     * @param ontClass ontology class
     * @param property ontology property
     * @return RDF node or null, if not present
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public RDFNode getRestrictionHasValue(OntClass ontClass, Property property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("OntProperty cannot be null");
	
	ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
	
	try
	{
	    while (it.hasNext())
	    {
		OntClass superClass = it.next();
		if (superClass.canAs(HasValueRestriction.class))
		{
		    HasValueRestriction restriction = superClass.asRestriction().asHasValueRestriction();
		    if (restriction.getOnProperty().equals(property))
			return restriction.getHasValue();
		}
	    }
	    
	    return null;
	}
	finally
	{
	    it.close();
	}
    }

    /**
     * Returns URI builder instantiated with pagination parameters for the current page.
     * 
     * @return URI builder
     */
    public UriBuilder getPageUriBuilder()
    {
	UriBuilder uriBuilder = getUriBuilder().
	    queryParam("limit", getLimit()).
	    queryParam("offset", getOffset());
	if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	if (getDesc()) uriBuilder.queryParam("desc", getDesc());
    
	return uriBuilder;
    }

    /**
     * Returns URI builder instantiated with pagination parameters for the previous page.
     * 
     * @return URI builder
     */
    public UriBuilder getPreviousUriBuilder()
    {
	UriBuilder uriBuilder = getUriBuilder().
	    queryParam("limit", getLimit()).
	    queryParam("offset", getOffset() - getLimit());
	if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	if (getDesc()) uriBuilder.queryParam("desc", getDesc());
	
	return uriBuilder;
    }

    /**
     * Returns URI builder instantiated with pagination parameters for the next page.
     * 
     * @return URI builder
     */
    public UriBuilder getNextUriBuilder()
    {
	UriBuilder uriBuilder = getUriBuilder().
	    queryParam("limit", getLimit()).
	    queryParam("offset", getOffset() + getLimit());
	if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	if (getDesc()) uriBuilder.queryParam("desc", getDesc());
	
	return uriBuilder;
    }

    /**
     * Returns URI builder, initialized with the URI of this resource.
     * 
     * @return URI builder
     */
    public final UriBuilder getUriBuilder()
    {
	return UriBuilder.fromUri(getURI());
    }
    
    /**
     * Returns URI of this resource. Uses Java's URI class instead of string as the {@link getURI()} does.
     * 
     * @return URI object
     */
    public final URI getRealURI()
    {
	return getUriBuilder().build();
    }

    /**
     * Returns this resource as ontology resource.
     * 
     * @return ontology resource
     */
    public OntResource getOntResource()
    {
	return ontResource;
    }

    /**
     * Returns ontology class that this resource matches.
     * If the request URI did not match any ontology class, <code>404 Not Found</code> was returned.
     * 
     * @return ontology class
     */
    public OntClass getMatchedOntClass()
    {
	return matchedOntClass;
    }

    /**
     * Returns the VoID dataset of this resource, if specified.
     * The dataset can be specified as a <code>void:inDataset</code> value restriction on an ontology class in
     * the sitemap ontology.
     * 
     * @return dataset resource or null, if not specified
     */
    public Resource getDataset()
    {
	return dataset;
    }

    /**
     * Returns the cache control of this resource, if specified.
     * The control value can be specified as a <code>gp:cacheControl</code> value restriction on an ontology class in
     * the sitemap ontology.
     * 
     * @return cache control object or null, if not specified
     */
    @Override
    public CacheControl getCacheControl()
    {
	return cacheControl;
    }

    /**
     * Returns the active SPARQL endpoint of this resource.
     * The default endpoint is supplied as constructor argument. However, it is overridden if the matching
     * ontology class has a <code>void:inDataset</code> value restriction and that dataset has a SPARQL
     * endpoint resource defined.
     * 
     * @return endpoint resource
     */
    @Override
    public SPARQLEndpoint getEndpoint()
    {
	return endpoint;
    }

    /**
     * Returns query used to retrieve RDF description of this resource
     * 
     * @return query object
     */
    @Override
    public Query getQuery()
    {
	return getQueryBuilder().build();
    }

    /**
     * Returns query builder, which is used to build SPARQL query to retrieve RDF description of this resource.
     * 
     * @return query builder
     */
    public QueryBuilder getQueryBuilder()
    {
	return queryBuilder;
    }

    /**
     * Returns URI information of current request.
     * 
     * @return URI information
     */
    public final UriInfo getUriInfo()
    {
	return uriInfo;
    }

    /**
     * Returns current request.
     * 
     * @return request object
     */
    public Request getRequest()
    {
	return request;
    }

    /**
     * Returns HTTP headers of the current request.
     * 
     * @return header object
     */
    public HttpHeaders getHttpHeaders()
    {
	return httpHeaders;
    }
    
    @Override
    public final OntModel getOntModel()
    {
	return getOntResource().getOntModel();
    }
    
    @Override
    public Profile getProfile()
    {
	return getOntResource().getProfile();
    }

    @Override
    public boolean isOntLanguageTerm()
    {
	return getOntResource().isOntLanguageTerm();
    }

    @Override
    public void setSameAs(Resource rsrc)
    {
	getOntResource().setSameAs(rsrc);
    }

    @Override
    public void addSameAs(Resource rsrc)
    {
	getOntResource().addSameAs(rsrc);
    }

    @Override
    public OntResource getSameAs()
    {
	return getOntResource().getSameAs();
    }

    @Override
    public ExtendedIterator<? extends Resource> listSameAs()
    {
	return getOntResource().listSameAs();
    }

    @Override
    public boolean isSameAs(Resource rsrc)
    {
	return getOntResource().isSameAs(rsrc);
    }

    @Override
    public void removeSameAs(Resource rsrc)
    {
	getOntResource().removeSameAs(rsrc);
    }

    @Override
    public void setDifferentFrom(Resource rsrc)
    {
	getOntResource().setDifferentFrom(rsrc);
    }

    @Override
    public void addDifferentFrom(Resource rsrc)
    {
	getOntResource().addDifferentFrom(rsrc);
    }

    @Override
    public OntResource getDifferentFrom()
    {
	return getOntResource().getDifferentFrom();
    }

    @Override
    public ExtendedIterator<? extends Resource> listDifferentFrom()
    {
	return getOntResource().listDifferentFrom();
    }

    @Override
    public boolean isDifferentFrom(Resource rsrc)
    {
	return getOntResource().isDifferentFrom(rsrc);
    }

    @Override
    public void removeDifferentFrom(Resource rsrc)
    {
	getOntResource().removeDifferentFrom(rsrc);
    }

    @Override
    public void setSeeAlso(Resource rsrc)
    {
	getOntResource().setSeeAlso(rsrc);
    }

    @Override
    public void addSeeAlso(Resource rsrc)
    {
	getOntResource().addSeeAlso(rsrc);
    }

    @Override
    public Resource getSeeAlso()
    {
	return getOntResource().getSeeAlso();
    }

    @Override
    public ExtendedIterator<RDFNode> listSeeAlso()
    {
	return getOntResource().listSeeAlso();
    }

    @Override
    public boolean hasSeeAlso(Resource rsrc)
    {
	return getOntResource().hasSeeAlso(rsrc);
    }

    @Override
    public void removeSeeAlso(Resource rsrc)
    {
	getOntResource().removeSeeAlso(rsrc);
    }

    @Override
    public void setIsDefinedBy(Resource rsrc)
    {
	getOntResource().setIsDefinedBy(rsrc);
    }

    @Override
    public void addIsDefinedBy(Resource rsrc)
    {
	getOntResource().addIsDefinedBy(rsrc);
    }

    @Override
    public Resource getIsDefinedBy()
    {
	return getOntResource().getIsDefinedBy();
    }

    @Override
    public ExtendedIterator<RDFNode> listIsDefinedBy()
    {
	return getOntResource().listIsDefinedBy();
    }

    @Override
    public boolean isDefinedBy(Resource rsrc)
    {
	return getOntResource().isDefinedBy(rsrc);
    }

    @Override
    public void removeDefinedBy(Resource rsrc)
    {
	getOntResource().removeDefinedBy(rsrc);
    }

    @Override
    public void setVersionInfo(String string)
    {
	getOntResource().setVersionInfo(string);
    }

    @Override
    public void addVersionInfo(String string)
    {
	getOntResource().addVersionInfo(string);
    }

    @Override
    public String getVersionInfo()
    {
	return getOntResource().getVersionInfo();
    }

    @Override
    public ExtendedIterator<String> listVersionInfo()
    {
	return getOntResource().listVersionInfo();
    }

    @Override
    public boolean hasVersionInfo(String string)
    {
	return getOntResource().hasVersionInfo(string);
    }

    @Override
    public void removeVersionInfo(String string)
    {
	getOntResource().removeVersionInfo(string);
    }

    @Override
    public void setLabel(String string, String string1)
    {
	getOntResource().setLabel(string, string1);
    }

    @Override
    public void addLabel(String string, String string1)
    {
	getOntResource().addLabel(string, string1);
    }

    @Override
    public void addLabel(Literal ltrl)
    {
	getOntResource().addLabel(ltrl);
    }

    @Override
    public String getLabel(String string)
    {
	return getOntResource().getLabel(string);
    }

    @Override
    public ExtendedIterator<RDFNode> listLabels(String string)
    {
	return getOntResource().listLabels(string);
    }

    @Override
    public boolean hasLabel(String string, String string1)
    {
	return getOntResource().hasLabel(string, string1);
    }

    @Override
    public boolean hasLabel(Literal ltrl)
    {
	return getOntResource().hasLabel(ltrl);
    }

    @Override
    public void removeLabel(String string, String string1)
    {
	getOntResource().removeLabel(string, string1);
    }

    @Override
    public void removeLabel(Literal ltrl)
    {
	getOntResource().removeLabel(ltrl);
    }

    @Override
    public void setComment(String string, String string1)
    {
	getOntResource().setComment(string, string1);
    }

    @Override
    public void addComment(String string, String string1)
    {
	getOntResource().addComment(string, string1);
    }

    @Override
    public void addComment(Literal ltrl)
    {
	getOntResource().addComment(ltrl);
    }

    @Override
    public String getComment(String string)
    {
	return getOntResource().getComment(string);
    }

    @Override
    public ExtendedIterator<RDFNode> listComments(String string)
    {
	return getOntResource().listComments(string);
    }

    @Override
    public boolean hasComment(String string, String string1)
    {
	return getOntResource().hasComment(string, string1);
    }

    @Override
    public boolean hasComment(Literal ltrl)
    {
	return getOntResource().hasComment(ltrl);
    }

    @Override
    public void removeComment(String string, String string1)
    {
	getOntResource().removeComment(string, string1);
    }

    @Override
    public void removeComment(Literal ltrl)
    {
	getOntResource().removeComment(ltrl);
    }

    @Override
    public void setRDFType(Resource rsrc)
    {
	getOntResource().setRDFType(rsrc);
    }

    @Override
    public void addRDFType(Resource rsrc)
    {
	getOntResource().addRDFType(rsrc);
    }

    @Override
    public Resource getRDFType()
    {
	return getOntResource().getRDFType();
    }

    @Override
    public Resource getRDFType(boolean bln)
    {
	return getOntResource().getRDFType(bln);
    }

    @Override
    public ExtendedIterator<Resource> listRDFTypes(boolean bln)
    {
	return getOntResource().listRDFTypes(bln);
    }

    @Override
    public boolean hasRDFType(Resource rsrc, boolean bln)
    {
	return getOntResource().hasRDFType(rsrc, bln);
    }

    @Override
    public boolean hasRDFType(Resource rsrc)
    {
	return getOntResource().hasRDFType(rsrc);
    }

    @Override
    public void removeRDFType(Resource rsrc)
    {
	getOntResource().removeRDFType(rsrc);
    }

    @Override
    public boolean hasRDFType(String string)
    {
	return getOntResource().hasRDFType(string);
    }

    @Override
    public int getCardinality(Property prprt)
    {
	return getOntResource().getCardinality(prprt);
    }

    @Override
    public void setPropertyValue(Property prprt, RDFNode rdfn)
    {
	getOntResource().setPropertyValue(prprt, rdfn);
    }

    @Override
    public RDFNode getPropertyValue(Property prprt)
    {
	return getOntResource().getPropertyValue(prprt);
    }

    @Override
    public NodeIterator listPropertyValues(Property prprt)
    {
	return getOntResource().listPropertyValues(prprt);
    }

    @Override
    public void removeProperty(Property prprt, RDFNode rdfn)
    {
	getOntResource().removeProperty(prprt, rdfn);
    }

    @Override
    public void remove()
    {
	getOntResource().remove();
    }

    @Override
    public OntProperty asProperty()
    {
	return getOntResource().asProperty();
    }

    @Override
    public AnnotationProperty asAnnotationProperty()
    {
	return getOntResource().asAnnotationProperty();
    }

    @Override
    public ObjectProperty asObjectProperty()
    {
	return getOntResource().asObjectProperty();
    }

    @Override
    public DatatypeProperty asDatatypeProperty()
    {
	return getOntResource().asDatatypeProperty();
    }

    @Override
    public Individual asIndividual()
    {
	return getOntResource().asIndividual();
    }

    @Override
    public OntClass asClass()
    {
	return getOntResource().asClass();
    }

    @Override
    public Ontology asOntology()
    {
	return getOntResource().asOntology();
    }

    @Override
    public DataRange asDataRange()
    {
	return getOntResource().asDataRange();
    }

    @Override
    public AllDifferent asAllDifferent()
    {
	return getOntResource().asAllDifferent();
    }

    @Override
    public boolean isProperty()
    {
	return getOntResource().isProperty();
    }

    @Override
    public boolean isAnnotationProperty()
    {
	return getOntResource().isAnnotationProperty();
    }

    @Override
    public boolean isObjectProperty()
    {
	return getOntResource().isObjectProperty();
    }

    @Override
    public boolean isDatatypeProperty()
    {
	return getOntResource().isDatatypeProperty();
    }

    @Override
    public boolean isIndividual()
    {
	return getOntResource().isIndividual();
    }

    @Override
    public boolean isClass()
    {
	return getOntResource().isClass();
    }

    @Override
    public boolean isOntology()
    {
	return getOntResource().isOntology();
    }

    @Override
    public boolean isDataRange()
    {
	return getOntResource().isDataRange();
    }

    @Override
    public boolean isAllDifferent()
    {
	return getOntResource().isAllDifferent();
    }

}