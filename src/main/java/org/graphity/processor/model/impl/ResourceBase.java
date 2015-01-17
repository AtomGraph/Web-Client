/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.model.impl;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.util.Loader;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.core.ResourceContext;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.graphity.processor.exception.NotFoundException;
import org.graphity.processor.model.ContainerResource;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.update.InsertDataBuilder;
import org.graphity.processor.update.UpdateBuilder;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.SIOC;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.server.model.GraphStore;
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
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class ResourceBase extends QueriedResourceBase implements org.graphity.processor.model.Resource, ContainerResource
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final GraphStore graphStore;
    private final OntClass matchedOntClass;
    private final OntResource ontResource;
    private final ResourceContext resourceContext;
    private final HttpHeaders httpHeaders;
    private String orderBy;
    private Boolean desc;
    private Long limit, offset;
    private QueryBuilder queryBuilder;
    private CacheControl cacheControl;
    
    /**
     * Public JAX-RS constructor. Suitable for subclassing.
     * If the request URI does not match any URI template in the sitemap ontology, 404 Not Found is returned.
     * 
     * If the matching ontology class is a subclass of LDP Page, this resource becomes a page resource and
     * HATEOS metadata is added (relations to the container and previous/next page resources).
     * 
     * @param uriInfo URI information of the current request
     * @param request current request
     * @param servletConfig webapp context
     * @param endpoint SPARQL endpoint of this resource
     * @param graphStore Graph Store of this resource
     * @param ontClass matched ontology class
     * @param httpHeaders HTTP headers of the current request
     * @param resourceContext resource context
     */
    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context ServletConfig servletConfig,
            @Context SPARQLEndpoint endpoint, @Context GraphStore graphStore,
            @Context OntClass ontClass, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, request, servletConfig, endpoint);

        if (ontClass == null)
        {
            if (log.isDebugEnabled()) log.debug("Resource {} has not matched any template OntClass, returning 404 Not Found", getURI());
            throw new NotFoundException("Resource has not matched any template");
        }
        
	if (graphStore == null) throw new IllegalArgumentException("GraphStore cannot be null");
        if (httpHeaders == null) throw new IllegalArgumentException("HttpHeaders cannot be null");
	if (resourceContext == null) throw new IllegalArgumentException("ResourceContext cannot be null");

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.add(ontClass.getModel()); // we don't want to make permanent changes to base ontology which is cached
        this.ontResource = model.createOntResource(getURI());
        this.matchedOntClass = ontClass;
	this.graphStore = graphStore;
	this.httpHeaders = httpHeaders;
        this.resourceContext = resourceContext;

        if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
    }

    /**
     * Post-construct initialization. Subclasses need to call super.init() first, just like with super() constructor.
     */
    @PostConstruct
    public void init()
    {
	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addProperty(RDF.type, getMatchedOntClass()); // getOntModel().add(description); ?
        
        Query query = getQuery(getMatchedOntClass(), SPIN.query, getQuerySolutionMap(this));
        if (query == null)
        {
            if (log.isErrorEnabled()) log.error("Query not defined for template '{}' (spin:query missing)", getMatchedOntClass().getURI());
            throw new WebApplicationException(new ConfigurationException("Query not defined for template '" + getMatchedOntClass().getURI() +"'"));
        }

        if (getMatchedOntClass().hasSuperClass(LDP.Container))
        {
            if (!getUriInfo().getQueryParameters().containsKey(GP.offset.getLocalName()))
            {
                Long defaultOffset = getLongValue(getMatchedOntClass(), GP.offset);
                if (defaultOffset == null) defaultOffset = Long.valueOf(0); // OFFSET is 0 by default
                this.offset = defaultOffset;
            }
            else this.offset = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.offset.getLocalName()));
                
            if (!getUriInfo().getQueryParameters().containsKey(GP.limit.getLocalName()))
            {
                Long defaultLimit = getLongValue(getMatchedOntClass(), GP.limit);
                if (defaultLimit == null) throw new IllegalArgumentException("Template class '" + getMatchedOntClass().getURI() + "' must have gp:limit if it is used as container");
                this.limit = defaultLimit;
            }
            else this.limit = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.limit.getLocalName()));
                
            if (!getUriInfo().getQueryParameters().containsKey(GP.orderBy.getLocalName())) this.orderBy = getStringValue(getMatchedOntClass(), GP.orderBy);
            else this.orderBy = getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName());
            
            if (!getUriInfo().getQueryParameters().containsKey(GP.desc.getLocalName()))
            {
                Boolean defaultDesc = getBooleanValue(getMatchedOntClass(), GP.desc);
                if (defaultDesc == null) defaultDesc = false; // ORDERY BY is ASC() by default
                this.desc = defaultDesc;
            }
            else this.desc = Boolean.parseBoolean(getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName()));
            
            queryBuilder = setSelectModifiers(QueryBuilder.fromQuery(query, getModel()),
                    this.offset, this.limit, this.orderBy, this.desc);
        }
        else
        {
            this.offset = this.limit = null;
            this.orderBy = null;
            this.desc = null;
            
            queryBuilder = QueryBuilder.fromQuery(query, getModel());
        }
        
	if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, queryBuilder);
	addProperty(SPIN.query, getQueryBuilder());
        
        cacheControl = getCacheControl(getMatchedOntClass());        
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

    /**
     * Returns sub-resource instance.
     * By default matches any path.
     * 
     * @return resource object
     */
    @Path("{path: .+}")
    public Object getSubResource()
    {
        if (getMatchedOntClass().equals(GP.SPARQLEndpoint)) return getSPARQLEndpoint();
        if (getMatchedOntClass().getPropertyResourceValue(GP.loadClass) != null)
        {
            try
            {
                Resource javaClass = getMatchedOntClass().getPropertyResourceValue(GP.loadClass);
                if (!javaClass.isURIResource())
                {
                    if (log.isErrorEnabled()) log.debug("gp:loadClass value of class '{}' is not a URI resource", getMatchedOntClass().getURI());
                    throw new ConfigurationException("gp:loadClass value of class '" + getMatchedOntClass().getURI() + "' is not a URI resource");
                }

                Class clazz = Loader.loadClass(javaClass.getURI());
                if (clazz == null)
                {
                    if (log.isErrorEnabled()) log.debug("Java class with URI '{}' could not be loaded", javaClass.getURI());
                    throw new ConfigurationException("Java class with URI '" + javaClass.getURI() + "' not found");
                }

                if (!Resource.class.isAssignableFrom(clazz))
                    if (log.isWarnEnabled()) log.warn("Java class with URI: {} is not a subclass of Graphity Resource", javaClass.getURI());
                
                if (log.isDebugEnabled()) log.debug("Loading Java class with URI: {}", javaClass.getURI());
                return getResourceContext().getResource(clazz);
            }
            catch (ConfigurationException ex)
            {
                throw new WebApplicationException(ex);
            }
        }

        return this;
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
	if (getMatchedOntClass().hasSuperClass(LDP.Container) && getRealURI().equals(getUriInfo().getRequestUri()))
	{
	    if (log.isDebugEnabled()) log.debug("OntResource is ldp:Container, redirecting to the first ldp:Page");	    
	    return Response.seeOther(getPageUriBuilder().build()).build();
	}

	Model description = describe();
	if (description.isEmpty())
	{
	    if (log.isDebugEnabled()) log.debug("Description Model is empty; returning 404 Not Found");
	    throw new NotFoundException("Description Model is empty");
	}

        description = addMetadata(description);
        if (log.isDebugEnabled()) log.debug("Returning @GET Response with {} statements in Model", description.size());
	return getResponse(description);
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
	return post(model, null, graphURI, getSPARQLEndpoint());
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
	return post(model, null, null, endpoint);
    }
    
    /**
     * Handles POST method, stores the submitted RDF model in the specified named graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param baseURI base URI for the RDF payload
     * @param graphURI target graph name
     * @param endpoint target SPARQL endpoint
     * @return response
     */
    public Response post(Model model, URI baseURI, URI graphURI, SPARQLEndpoint endpoint)
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
        model = addProvenance(model);
        
	UpdateRequest insertDataRequest;
	if (graphURI != null) insertDataRequest = InsertDataBuilder.fromData(graphURI, model).build();
	else insertDataRequest = InsertDataBuilder.fromData(model).build();

        if (baseURI != null) insertDataRequest.setBaseURI(baseURI.toString());
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
     * Adds provenance properties such to submitted RDF model.
     * 
     * @param model RDF model to be processed
     * @return model with provenance
     */
    public Model addProvenance(Model model)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");

	ResIterator resIt = model.listSubjectsWithProperty(RDF.type, FOAF.Document);	
	try
	{
            while (resIt.hasNext())
	    {
                Resource res = resIt.next();

                ObjectProperty memberProperty = SIOC.HAS_CONTAINER;
                // use actual this resource types instead?
                if (getMatchedOntClass().hasSuperClass(SIOC.CONTAINER) && res.hasProperty(RDF.type, SIOC.CONTAINER))
                    memberProperty = SIOC.HAS_PARENT;
                if (getMatchedOntClass().hasSuperClass(SIOC.SPACE))
                    memberProperty = SIOC.HAS_SPACE;

                if (!res.hasProperty(memberProperty))
                    res.addProperty(memberProperty, this);
                
                if (!res.hasProperty(DCTerms.created))
                    res.addLiteral(DCTerms.created, model.createTypedLiteral(GregorianCalendar.getInstance()));
                else
                {
                    res.removeAll(DCTerms.modified).
                        addLiteral(DCTerms.modified, model.createTypedLiteral(GregorianCalendar.getInstance()));
                }
	    }
	}
	finally
	{
	    resIt.close();
	}
        
        return model;
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
	return put(model, null, endpoint);
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
     * Adds run-time metadata to RDF description.
     * In case a container is requested, page resource with HATEOS previous/next links is added to the model.
     * 
     * @param model target RDF model
     * @return description model with metadata
     */
    public Model addMetadata(Model model)
    {
	if (getMatchedOntClass().hasSuperClass(LDP.Container))
	{
	    if (log.isDebugEnabled()) log.debug("Adding PageResource metadata: ldp:pageOf {}", this);
            createPageResource(model);
        }

        return model;
    }
    
    /**
     * Creates a page resource for the current container. Includes HATEOS previous/next links.
     * 
     * @param model target RDF model
     * @return page resource
     * @see <a href="http://www.w3.org/1999/xhtml/vocab">XHTML Vocabulary</a>
     */
    public Resource createPageResource(Model model)
    {
        if (log.isDebugEnabled()) log.debug("Adding PageResource metadata: ldp:pageOf {}", this);
        Resource page = model.createResource(getPageUriBuilder().build().toString()).
            addProperty(RDF.type, LDP.Page).
            addProperty(LDP.pageOf, this);

        if (getOffset() >= getLimit())
        {
            if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:previous {}", getURI(), getPreviousUriBuilder().build().toString());
            page.addProperty(XHV.prev, model.createResource(getPreviousUriBuilder().build().toString()));
        }

        // no way to know if there's a next page without counting results (either total or in current page)
        //int subjectCount = describe().listSubjects().toList().size();
        //log.debug("describe().listSubjects().toList().size(): {}", subjectCount);
        //if (subjectCount >= getLimit())
        {
            if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:next {}", getURI(), getNextUriBuilder().build().toString());
            page.addProperty(XHV.next, model.createResource(getNextUriBuilder().build().toString()));
        }
        
        return page;
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
     * Sets <code>SELECT</code> solution modifiers on a supplied builder.
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
     * @param qsm query variable bindings
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
        if (query != null) return getQuery(query.toString(), getRealURI(), qsm);
                
        TemplateCall templateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
        if (templateCall != null) return getQuery(templateCall.getQueryString(), getRealURI(), qsm);
        
        return null;
    }

    /**
     * Returns variable bindings for description query.
     * 
     * @param resource this resource
     * @return map with variable bindings
     */
    public QuerySolutionMap getQuerySolutionMap(Resource resource)
    {
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
        return qsm;
    }
    
    /**
     * Given a SPARQL query string and variable bindings, returns a SPARQL query.
     * 
     * Following the convention of SPIN API, variable name <code>?this</code> has a special meaning and
     * is assigned to the value of the resource (which is usually this resource).
     * <code>OFFSET</code>) will be set to implement pagination.
     * 
     * @param queryString SPARQL query string
     * @param baseURI <code>BASE</code> URI for the query
     * @param qsm query variable bindings
     * @return query object
     */
    public Query getQuery(String queryString, URI baseURI, QuerySolutionMap qsm)
    {
	if (queryString == null) throw new IllegalArgumentException("Query string cannot be null");
	if (baseURI == null) throw new IllegalArgumentException("Base URI cannot be null");
        if (qsm == null) throw new IllegalArgumentException("QuerySolutionMap cannot be null");
	
	Query query = new ParameterizedSparqlString(queryString, qsm).asQuery();
        query.setBaseURI(baseURI.toString());
        return query;
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
     * Adds matched sitemap class as affordance metadata in Link header.
     * 
     * @param model response model
     * @return response builder
     */
    @Override
    public ResponseBuilder getResponseBuilder(Model model)
    {
        return super.getResponseBuilder(model).header("Link", "<" + getMatchedOntClass().getURI() + ">; rel='type'");
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
     * Returns value of <samp>orderBy</samp> query string parameter, which indicates the name of the variable after
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

    /**
     * Returns URI builder instantiated with pagination parameters for the current page.
     * 
     * @return URI builder
     */
    public UriBuilder getPageUriBuilder()
    {
	UriBuilder uriBuilder = getUriBuilder().
	    queryParam(GP.offset.getLocalName(), getOffset()).
	    queryParam(GP.limit.getLocalName(), getLimit());
	if (getOrderBy() != null) uriBuilder.queryParam(GP.orderBy.getLocalName(), getOrderBy());
	if (getDesc()) uriBuilder.queryParam(GP.desc.getLocalName(), getDesc());
    
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
	    queryParam(GP.offset.getLocalName(), getOffset() - getLimit()).
	    queryParam(GP.limit.getLocalName(), getLimit());
        if (getOrderBy() != null) uriBuilder.queryParam(GP.orderBy.getLocalName(), getOrderBy());
	if (getDesc()) uriBuilder.queryParam(GP.desc.getLocalName(), getDesc());
	
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
	    queryParam(GP.offset.getLocalName(), getOffset() + getLimit()).
	    queryParam(GP.limit.getLocalName(), getLimit());
        if (getOrderBy() != null) uriBuilder.queryParam(GP.orderBy.getLocalName(), getOrderBy());
	if (getDesc()) uriBuilder.queryParam(GP.desc.getLocalName(), getDesc());
	
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
     * Returns URI of this resource. Uses Java's URI class instead of string as the {@link #getURI()} does.
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
     * Returns Graph Store of this resource.
     * 
     * @return graph store object
     */
    public GraphStore getGraphStore()
    {
        return graphStore;
    }

    /**
     * Returns query builder, which is used to build SPARQL query to retrieve RDF description of this resource.
     * 
     * @return query builder
     */
    @Override
    public QueryBuilder getQueryBuilder()
    {
	return queryBuilder;
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