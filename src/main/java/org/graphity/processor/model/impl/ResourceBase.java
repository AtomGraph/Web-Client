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
package org.graphity.processor.model.impl;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.uri.UriTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import org.graphity.processor.model.ContainerResource;
import org.graphity.processor.model.MatchedIndividual;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.update.InsertDataBuilder;
import org.graphity.processor.update.UpdateBuilder;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.server.model.Origin;
import org.graphity.server.model.Proxy;
import org.graphity.server.model.impl.QueriedResourceBase;
import org.graphity.server.model.SPARQLEndpoint;
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
 * @see ContainerResource
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/OntResource.html">OntResource</a>
 * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class ResourceBase extends QueriedResourceBase implements OntResource, ContainerResource, MatchedIndividual
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final OntResource ontResource;
    private final UriInfo uriInfo;
    private final ResourceContext resourceContext;
    private final HttpHeaders httpHeaders;
    private final URI graphURI;
    private String orderBy;
    private Boolean desc;
    private Long limit, offset;
    private QueryBuilder queryBuilder;
    private OntClass matchedOntClass;
    private CacheControl cacheControl;
    
    /**
     * Protected constructor. Not suitable for JAX-RS but can be used when subclassing.
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param httpHeaders HTTP headers of the current request
     * @param servletContext webapp context
     * @param metaEndpoint sitemap ontology
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntModel ontModel,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext,
            @QueryParam("limit") Long limit,
	    @QueryParam("offset") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc,
	    @QueryParam("graph") URI graphURI)
    {
	this(ontModel.createOntResource(uriInfo.getAbsolutePath().toString()), endpoint,
                request, servletContext, uriInfo, httpHeaders, resourceContext,
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
     * @param servletContext webapp context
     * @param resource this resource as OWL resource
     * @param endpoint SPARQL endpoint of this resource
     * @param limit pagination <code>LIMIT</code (<samp>limit</samp> query string param)
     * @param offset pagination <code>OFFSET</code> (<samp>offset</samp> query string param)
     * @param orderBy pagination <code>ORDER BY</code> variable name (<samp>order-by</samp> query string param)
     * @param desc pagination <code>DESC</code> value (<samp>desc</samp> query string param)
     * @param graphURI target <code>GRAPH</code> name (<samp>graph</samp> query string param)
     * @see <a href="http://en.wikipedia.org/wiki/HATEOAS">HATEOS</a>
     */
    protected ResourceBase(OntResource resource, SPARQLEndpoint endpoint,
            Request request, ServletContext servletContext,
            UriInfo uriInfo, HttpHeaders httpHeaders, ResourceContext resourceContext,
	    Long limit, Long offset, String orderBy, Boolean desc, URI graphURI)
    {
	super(resource.getURI(), endpoint);

	if (resource == null) throw new IllegalArgumentException("OntResource cannot be null");
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        if (httpHeaders == null) throw new IllegalArgumentException("HttpHeaders cannot be null");
	if (resourceContext == null) throw new IllegalArgumentException("ResourceContext cannot be null");

        this.ontResource = resource;
	this.uriInfo = uriInfo;
	this.httpHeaders = httpHeaders;
        this.resourceContext = resourceContext;
	if (graphURI != null && graphURI.isAbsolute()) this.graphURI = graphURI;
	else this.graphURI = null;
        this.offset = offset;
        this.limit = limit;
        this.orderBy = orderBy;
        this.desc = desc;
    }

    @PostConstruct
    public void init()
    {
        if (log.isDebugEnabled()) log.debug("@PostConstruct initializtion");

	matchedOntClass = matchOntClass(getOntModel(), getRealURI(), uriInfo.getBaseUri());
	if (matchedOntClass == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
        QuerySolutionMap qsm = getQuerySolutionMap(this);
        
        if (hasRDFType(LDP.Container)) //if (matchedOntClass.hasSuperClass(LDP.Container))
        {
            if (offset == null)
            {
                Long defaultOffset = getLongValue(matchedOntClass, GP.offset);
                if (defaultOffset == null) defaultOffset = Long.valueOf(0); // OFFSET is always 0 by default
                this.offset = defaultOffset;
            }
            
            if (limit == null)
            {
                Long defaultLimit = getLongValue(matchedOntClass, GP.limit);
                if (defaultLimit == null) throw new IllegalArgumentException("Template class '" + matchedOntClass.getURI() + "' must have gp:limit if it is used as container");
                this.limit = defaultLimit;
            }
            
            if (orderBy == null) this.orderBy = getStringValue(matchedOntClass, GP.orderBy);
            
            if (desc == null)
            {
                Boolean defaultDesc = getBooleanValue(matchedOntClass, GP.desc);
                if (defaultDesc == null) defaultDesc = false; // ORDERY BY is always ASC() by default
                this.desc = defaultDesc;
            }

            queryBuilder = setSelectModifiers(QueryBuilder.fromQuery(getQuery(matchedOntClass, SPIN.query, qsm), getModel()),
                    this.offset, this.limit, this.orderBy, this.desc);
        }
        else
        {
            this.offset = this.limit = null;
            this.orderBy = null;
            this.desc = null;
            
            queryBuilder = QueryBuilder.fromQuery(getQuery(matchedOntClass, SPIN.query, qsm), getModel());
        }
        if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with QueryBuilder: {}", queryBuilder);
        
        cacheControl = getCacheControl(matchedOntClass);
    }
    
    public Long getLongValue(OntClass ontClass, DatatypeProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getLong();
        
        return null;
    }

    public Boolean getBooleanValue(OntClass ontClass, DatatypeProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getBoolean();
        
        return null;
    }

    public String getStringValue(OntClass ontClass, DatatypeProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getString();
        
        return null;
    }

    @Path("{path: .+}")
    public Object getSubResource()
    {
        return this;
    }

    @Path("sparql")
    public Object getSPARQLResource()
    {
        // avoid eternal loop if endpoint proxy is configured to point to local SPARQL endpoint
        SPARQLEndpoint endpoint = getSPARQLEndpoint();
        if (endpoint instanceof Proxy)
        {
            Origin origin = ((Proxy)endpoint).getOrigin();
            if (origin.getURI().equals(getURI()))
            {
                if (log.isDebugEnabled()) log.debug("SPARQLEndpoint with URI {} is a proxy of itself, returning SPARQLEndpointBase", getURI());
                return getResourceContext().getResource(SPARQLEndpointBase.class);
            }
        }
        
        return endpoint;
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
	return post(model, getSPARQLEndpoint());
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
	return post(model, graphURI, getSPARQLEndpoint());
    }

    /**
     * Handles POST method, stores the submitted RDF model in the default graph of default SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response post(Model model, SPARQLEndpoint endpoint)
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
    public Response post(Model model, URI graphURI, SPARQLEndpoint endpoint)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (endpoint == null) throw new IllegalArgumentException("SPARQL update endpoint cannot be null");
	if (log.isDebugEnabled()) log.debug("POST GRAPH URI: {} SPARQLEndpoint: {}", graphURI, endpoint);
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

	endpoint.post(insertDataRequest, null, null);
	
	URI createdURI = UriBuilder.fromUri(created.getURI()).build();
	if (log.isDebugEnabled()) log.debug("Redirecting to POSTed Resource URI: {}", createdURI);
	// http://stackoverflow.com/questions/3383725/post-redirect-get-prg-vs-meaningful-2xx-response-codes
	// http://www.blackpepper.co.uk/posts/201-created-or-post-redirect-get/
	//return Response.created(createdURI).entity(model).build();
	return Response.seeOther(createdURI).build();
    }

    public Resource getURIResource(Model model, Resource type)
    {
	ResIterator it = model.listSubjectsWithProperty(RDF.type, type);

	try
	{
	    while (it.hasNext())
	    {
		Resource resource = it.next();

		if (resource.isURIResource()) return resource;
	    }
	}
	finally
	{
	    it.close();
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
	return put(model, getSPARQLEndpoint());
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
	return put(model, graphURI, getSPARQLEndpoint());
    }

    /**
     * Handles PUT method, stores the submitted RDF model in the default graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response put(Model model, SPARQLEndpoint endpoint)
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
    public Response put(Model model, URI graphURI, SPARQLEndpoint endpoint)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (endpoint == null) throw new IllegalArgumentException("SPARQL update endpoint cannot be null");
	if (log.isDebugEnabled()) log.debug("PUT GRAPH URI: {} SPARQLEndpoint: {}", graphURI, endpoint);
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
	endpoint.post(updateRequest, null, null);
	
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
	return delete(getSPARQLEndpoint());
    }
    
    /**
     * Handles DELETE method, deletes the RDF representation of this resource from the specified SPARQL endpoint, and
     * returns response.
     * 
     * @param endpoint target SPARQL endpoint
     * @return response
     */    
    public Response delete(SPARQLEndpoint endpoint)
    {
	if (log.isDebugEnabled()) log.debug("DELETEing resource: {} matched OntClass: {}", this, getMatchedOntClass());
	UpdateRequest deleteRequest = getUpdateRequest(getMatchedOntClass(), this);
	if (log.isDebugEnabled()) log.debug("DELETE UpdateRequest: {}", deleteRequest);
	endpoint.post(deleteRequest, null, null);
	
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

	if (hasRDFType(LDP.Container)) // && !description.isEmpty()
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
     * Returns <samp>Cache-Control</samp> HTTP header value, specified on an ontology class.
     * 
     * @param ontClass the ontology class with the restriction
     * @return CacheControl instance or null, if no dataset restriction was found
     */
    public CacheControl getCacheControl(OntClass ontClass)
    {
       return getCacheControl(ontClass, GP.cacheControl);
    }

    /**
     * Returns `Cache-Control` HTTP header value, specified on an ontology class with given property.
     * 
     * @param ontClass the ontology class with the restriction
     * @param property the property holding the literal value
     * @return CacheControl instance or null, if no dataset restriction was found
     */
    public CacheControl getCacheControl(OntClass ontClass, DatatypeProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return CacheControl.valueOf(ontClass.getPropertyValue(property).asLiteral().getString()); // will fail on bad config

	return null;
    }

    /**
     * Sets <code>SELECT</code> on a supplied query builder, if it contains such sub-query
     * 
     * @param queryBuilder query builder
     * @param offset <code>OFFSET</code> value
     * @param limit <code>LIMIT</code> value
     * @param orderBy <code>ORDER BY</code> variable name
     * @param desc ordering direction (true for <code>DESC()</code>
     * @return query builder with set modifiers
     * @see org.graphity.processor.query.QueryBuilder
     */
    public QueryBuilder setSelectModifiers(QueryBuilder queryBuilder, Long offset, Long limit, String orderBy, Boolean desc)
    {
        if (queryBuilder == null) throw new IllegalArgumentException("QueryBuilder cannot be null");
        
        SelectBuilder selectBuilder = queryBuilder.getSubSelectBuilder();
        if (selectBuilder != null) setSelectModifiers(selectBuilder, offset, limit, orderBy, desc);
        
        return queryBuilder;
    }

    /**
     * Sets <code>SELECT</code> on a supplied builder.
     * This method is used to build queries for page resources. It sets <code>LIMIT</code> and <code>OFFSET</code>
     * modifiers as well as <code>ORDER BY</code>/<code>DESC</code> clauses on <code>SELECT</code> sub-queries.
     * Currently only one <code>ORDER BY</code> condition is supported.
     * 
     * @param selectBuilder <code>SELECT</code> builder
     * @param offset <code>OFFSET</code> value
     * @param limit <code>LIMIT</code> value
     * @param orderBy <code>ORDER BY</code> variable name
     * @param desc ordering direction (true for <code>DESC()</code>
     * @return SELECT builder with set modifiers
     * @see org.graphity.processor.query.SelectBuilder
     */
    public SelectBuilder setSelectModifiers(SelectBuilder selectBuilder, Long offset, Long limit, String orderBy, Boolean desc)
    {	
        if (selectBuilder == null) throw new IllegalArgumentException("SelectBuilder cannot be null");
        if (offset == null) throw new IllegalArgumentException("Offset cannot be null");
        if (limit == null) throw new IllegalArgumentException("Limit cannot be null");
        
        selectBuilder.replaceOffset(offset).replaceLimit(limit);

        if (orderBy != null)
        {
            try
            {
                selectBuilder.replaceOrderBy(null). // any existing ORDER BY condition is removed first
                    orderBy(orderBy, desc);
            }
            catch (IllegalArgumentException ex)
            {
                if (log.isWarnEnabled()) log.warn("Tried to use ORDER BY variable ?{} which is not present in the WHERE pattern", getOrderBy());
            }
        }
        
	return selectBuilder;
    }

    /**
     * Given an RDF resource and an ontology class that it belongs to, returns a SPARQL query that can be used
     * to retrieve its description.
     * The ontology class must have a SPIN template call attached (using <code>spin:query</code>).
     * 
     * @param ontClass ontology class of the resource
     * @param property property for the query object
     * @param resource resource to be described
     * @return query object
     * @see org.topbraid.spin.model.TemplateCall
     */
    public Query getQuery(OntClass ontClass, Property property, QuerySolutionMap qsm)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (ontClass.getPropertyResourceValue(property) == null)
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN query or template call resource (spin:query)");

	Resource queryOrTemplateCall = ontClass.getPropertyResourceValue(property);
	
        org.topbraid.spin.model.Query query = SPINFactory.asQuery(queryOrTemplateCall);
        if (query != null) return getQuery(query.toString(), qsm);
                
        TemplateCall templateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
        if (templateCall != null) return getQuery(templateCall.getQueryString(), qsm);
        
        return null;
    }
    
    public QuerySolutionMap getQuerySolutionMap(Resource resource)
    {
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
        return qsm;
    }
    
    /**
     * Given a SPIN template call and an RDF resource, returns a SPARQL query that can be used to retrieve
     * resource's description.
     * Following the convention of SPIN API, variable name <code>?this</code> has a special meaning and
     * is assigned to the value of the resource (which is usually this resource).
     * If this resource is a page resource, the SELECT sub-query modifiers (<code>LIMIT</code> and
     * <code>OFFSET</code>) will be set to implement pagination.
     * 
     * @param queryString SPARQL query string
     * @param resource RDF resource
     * @return query object
     */
    public Query getQuery(String queryString, QuerySolutionMap qsm)
    {
	if (queryString == null) throw new IllegalArgumentException("Query string cannot be null");
	if (qsm == null) throw new IllegalArgumentException("QuerySolutionMap cannot be null");
	
	return new ParameterizedSparqlString(queryString, qsm).asQuery();
    }

    public UpdateBuilder getUpdateBuilder(Update update)
    {
	UpdateBuilder ub = UpdateBuilder.fromUpdate(update);
	
	return ub;
    }
    
    public UpdateRequest getUpdateRequest(OntClass ontClass, Resource resource)
    {
	if (ontClass.getPropertyResourceValue(ResourceFactory.createProperty(SPIN.NS, "update")) == null)
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN update or template call resource (spin:update)");

	Resource updateOrTemplateCall = ontClass.getPropertyResourceValue(ResourceFactory.createProperty(SPIN.NS, "update"));
	
        Update update = SPINFactory.asUpdate(updateOrTemplateCall);
        if (update != null) return getUpdateRequest(update.toString(), resource);

        TemplateCall templateCall = SPINFactory.asTemplateCall(updateOrTemplateCall);
        if (templateCall != null) return getUpdateRequest(templateCall.getQueryString(), resource);
        
        return null;
    }

    public UpdateRequest getUpdateRequest(String queryString, Resource resource)
    {
	if (queryString == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	return new ParameterizedSparqlString(queryString, qsm).asUpdate();
    }

    /**
     * Given an absolute URI and a base URI, returns ontology class with a matching URI template, if any.
     * 
     * @param uri absolute URI being matched
     * @param base base URI
     * @return matching ontology class or null, if none
     */
    public OntClass matchOntClass(OntModel ontModel, URI uri, URI base)
    {
	if (uri == null) throw new IllegalArgumentException("URI being matched cannot be null");
	if (base == null) throw new IllegalArgumentException("Base URI cannot be null");
	if (!uri.isAbsolute()) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not absolute");
	if (base.relativize(uri).equals(uri)) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not relative to the base URI \"" + base + "\"");
	    
	StringBuilder path = new StringBuilder();
	// instead of path, include query string by relativizing request URI against base URI
	path.append("/").append(base.relativize(uri));
	return matchOntClass(ontModel, path);
    }

    /**
     * Given a relative URI, returns ontology class with a matching URI template, if any.
     * By default, <code>lda:uriTemplate</code> property (from Linked Data API) is used for the <code>owl:HasValue</code>
     * restrictions, with URI template string as the object literal.
     * 
     * @param path absolute path (relative URI)
     * @return matching ontology class or null, if none
     * @see <a href="https://code.google.com/p/linked-data-api/wiki/API_Vocabulary">Linked Data API Vocabulary</a>
     */
    public OntClass matchOntClass(OntModel ontModel, CharSequence path)
    {
        return matchOntClass(ontModel, path, GP.uriTemplate);
    }

    /**
     * Given a relative URI and URI template property, returns ontology class with a matching URI template, if any.
     * URIs are matched against the URI templates specified in ontology class <code>owl:hasValue</code> restrictions
     * on the given property in the sitemap ontology.
     * This method uses Jersey implementation of the JAX-RS URI matching algorithm.
     * 
     * @param path absolute path (relative URI)
     * @param property restriction property holding the URI template value
     * @return matching ontology class or null, if none
     * @see <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec3.html#x3-340003.7">3.7 Matching Requests to Resource Methods (JAX-RS 1.1)</a>
     * @see <a href="https://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/uri/UriTemplate.html">Jersey UriTemplate</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public OntClass matchOntClass(OntModel ontModel, CharSequence path, Property property)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
        
        TreeMap<UriTemplate, OntClass> matchedClasses = new TreeMap<>(UriTemplate.COMPARATOR);

        // the main sitemap has precedence
        matchedClasses.putAll(matchOntClasses(ontModel, path, property, true));
        if (!matchedClasses.isEmpty())
        {
            if (log.isDebugEnabled()) log.debug("Matched UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
            return matchedClasses.firstEntry().getValue();
        }

        // gp:Templates from imported ontologies have lower precedence
        matchedClasses.putAll(matchOntClasses(ontModel, path, property, false));
        if (!matchedClasses.isEmpty())
        {
            if (log.isDebugEnabled()) log.debug("Matched imported UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
            return matchedClasses.firstEntry().getValue();
        }
        
        if (log.isDebugEnabled()) log.debug("Path {} has no OntClass match in this OntModel", path);
        return null;
    }

    public Map<UriTemplate, OntClass> matchOntClasses(OntModel ontModel, CharSequence path, Property property, boolean inBaseModel)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
        if (path == null) throw new IllegalArgumentException("Path being matched cannot be null");
 	if (property == null) throw new IllegalArgumentException("URI template property cannot be null");

        Map<UriTemplate, OntClass> matchedClasses = new HashMap<>();
        StmtIterator it = ontModel.listStatements(null, property, (RDFNode)null);

        try
	{
	    while (it.hasNext())
	    {
                Statement stmt = it.next();
                if (((ontModel.isInBaseModel(stmt) && inBaseModel) || (!ontModel.isInBaseModel(stmt) && !inBaseModel)) &&
                        stmt.getSubject().canAs(OntClass.class))
                {
                    OntClass ontClass = stmt.getSubject().as(OntClass.class);
                    if (ontClass.hasSuperClass(FOAF.Document) && 
                            ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
                    {
                        UriTemplate uriTemplate = new UriTemplate(ontClass.getPropertyValue(property).asLiteral().getString());
                        HashMap<String, String> map = new HashMap<>();

                        if (uriTemplate.match(path, map))
                        {
                            if (log.isDebugEnabled()) log.debug("Path {} matched UriTemplate {}", path, uriTemplate);
                            if (log.isDebugEnabled()) log.debug("Path {} matched OntClass {}", path, ontClass);
                            matchedClasses.put(uriTemplate, ontClass);
                        }
                        else
                            if (log.isDebugEnabled()) log.debug("Path {} did not match UriTemplate {}", path, uriTemplate);
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return matchedClasses;
    }

    /**
     * Returns value of <samp>limit</samp> query string parameter, which indicates the number of resources per page.
     * This value is set as <code>LIMIT</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * 
     * @return limit value
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modResultLimit">15.5 LIMIT</a>
     */
    @Override
    public Long getLimit()
    {
	return limit;
    }

    /**
     * Returns value of <samp>offset</samp> query string parameter, which indicates the number of resources the page
     * has skipped from the start of the container.
     * This value is set as <code>OFFSET</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * 
     * @return offset value
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOffset">15.4 OFFSET</a>
     */
    @Override
    public Long getOffset()
    {
	return offset;
    }

    /**
     * Returns value of <samp>order-by</samp> query string parameter, which indicates the name of the variable after
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
    public String getOrderBy()
    {
	return orderBy;
    }

    /**
     * Returns value of <samp>desc</samp> query string parameter, which indicates the direction of resource ordering
     * in the container (and the page).
     * If this method returns true, <code>DESC</code> order modifier is set if this resource is a page
     * (therefore pagination is used).
     * 
     * @return true if the order is descending, false otherwise
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modOrderBy">15.1 ORDER BY</a>
     */
    @Override
    public Boolean getDesc()
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
	    queryParam("offset", getOffset()).
	    queryParam("limit", getLimit());
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
	    queryParam("offset", getOffset() - getLimit()).
	    queryParam("limit", getLimit());
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
	    queryParam("offset", getOffset() + getLimit()).
	    queryParam("limit", getLimit());
        if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	if (getDesc()) uriBuilder.queryParam("desc", getDesc());
	
	return uriBuilder;
    }

    /**
     * Returns URI builder, initialized with the URI of this resource.
     * 
     * @return URI builder
     */
    public UriBuilder getUriBuilder()
    {
	return UriBuilder.fromUri(getURI());
    }
    
    /**
     * Returns URI of this resource. Uses Java's URI class instead of string as the {@link getURI()} does.
     * 
     * @return URI object
     */
    public URI getRealURI()
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
    @Override
    public OntClass getMatchedOntClass()
    {
	return matchedOntClass;
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
    public UriInfo getUriInfo()
    {
	return uriInfo;
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

    public ResourceContext getResourceContext()
    {
        return resourceContext;
    }

    @Override
    public Model getModel()
    {
        return getOntResource().getModel();
    }
    
    @Override
    public OntModel getOntModel()
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

    @Override
    public AnonId getId()
    {
	return getOntResource().getId();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource inModel(Model model)
    {
	return getOntResource().inModel(model);
    }

    @Override
    public boolean hasURI(String string)
    {
	return getOntResource().hasURI(string);
    }

    @Override
    public String getNameSpace()
    {
	return getOntResource().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return getOntResource().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property prprt)
    {
	return getOntResource().getRequiredProperty(prprt);
    }

    @Override
    public Statement getProperty(Property prprt)
    {
	return getOntResource().getProperty(prprt);
    }

    @Override
    public StmtIterator listProperties(Property prprt)
    {
	return getOntResource().listProperties(prprt);
    }

    @Override
    public StmtIterator listProperties()
    {
	return getOntResource().listProperties();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, boolean bln)
    {
	return getOntResource().addLiteral(prprt, bln);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, long l)
    {
	return getOntResource().addLiteral(prprt, l);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, char c)
    {
	return getOntResource().addLiteral(prprt, c);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, double d)
    {
	return getOntResource().addLiteral(prprt, d);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, float f)
    {
	return getOntResource().addLiteral(prprt, f);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, Object o)
    {
	return getOntResource().addLiteral(prprt, o);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addLiteral(Property prprt, Literal ltrl)
    {
	return getOntResource().addLiteral(prprt, ltrl);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addProperty(Property prprt, String string)
    {
	return getOntResource().addLiteral(prprt, string);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addProperty(Property prprt, String string, String string1)
    {
	return getOntResource().addProperty(prprt, string, string1);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addProperty(Property prprt, String string, RDFDatatype rdfd)
    {
	return getOntResource().addProperty(prprt, prprt);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource addProperty(Property prprt, RDFNode rdfn)
    {
	return getOntResource().addProperty(prprt, rdfn);
    }

    @Override
    public boolean hasProperty(Property prprt)
    {
	return getOntResource().hasProperty(prprt);
    }

    @Override
    public boolean hasLiteral(Property prprt, boolean bln)
    {
	return getOntResource().hasLiteral(prprt, bln);
    }

    @Override
    public boolean hasLiteral(Property prprt, long l)
    {
	return getOntResource().hasLiteral(prprt, l);
    }

    @Override
    public boolean hasLiteral(Property prprt, char c)
    {
	return getOntResource().hasLiteral(prprt, c);
    }

    @Override
    public boolean hasLiteral(Property prprt, double d)
    {
	return getOntResource().hasLiteral(prprt, d);
    }

    @Override
    public boolean hasLiteral(Property prprt, float f)
    {
	return getOntResource().hasLiteral(prprt, f);
    }

    @Override
    public boolean hasLiteral(Property prprt, Object o)
    {
	return getOntResource().hasLiteral(prprt, o);
    }

    @Override
    public boolean hasProperty(Property prprt, String string)
    {
	return getOntResource().hasProperty(prprt, string);
    }

    @Override
    public boolean hasProperty(Property prprt, String string, String string1)
    {
	return getOntResource().hasProperty(prprt, string, string1);
    }

    @Override
    public boolean hasProperty(Property prprt, RDFNode rdfn)
    {
	return getOntResource().hasProperty(prprt, rdfn);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource removeProperties()
    {
	return getOntResource().removeProperties();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource removeAll(Property prprt)
    {
	return getOntResource().removeAll(prprt);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource begin()
    {
	return getOntResource().begin();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource abort()
    {
	return getOntResource().abort();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource commit()
    {
	return getOntResource().commit();
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource getPropertyResourceValue(Property prprt)
    {
	return getOntResource().getPropertyResourceValue(prprt);
    }

    @Override
    public boolean isAnon()
    {
	return getOntResource().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return getOntResource().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return getOntResource().isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return getOntResource().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> type)
    {
	return getOntResource().as(type);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> type)
    {
	return getOntResource().canAs(type);
    }

    @Override
    public Object visitWith(RDFVisitor rdfv)
    {
	return getOntResource().visitWith(rdfv);
    }

    @Override
    public com.hp.hpl.jena.rdf.model.Resource asResource()
    {
	return getOntResource().asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return getOntResource().asLiteral();
    }

    @Override
    public Node asNode()
    {
	return getOntResource().asNode();
    }

    @Override
    public String toString()
    {
	return getOntResource().toString();
    }
 
}