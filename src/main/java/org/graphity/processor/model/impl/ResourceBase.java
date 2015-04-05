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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.naming.ConfigurationException;
import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.graphity.processor.exception.NotFoundException;
import org.graphity.processor.provider.OntClassMatcher;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.update.InsertDataBuilder;
import org.graphity.processor.util.Link;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SIOC;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.core.model.GraphStore;
import org.graphity.core.model.impl.QueriedResourceBase;
import org.graphity.core.model.SPARQLEndpoint;
import org.graphity.core.util.ModelUtils;
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
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/OntResource.html">OntResource</a>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#solutionModifiers">15 Solution Sequences and Modifiers</a>
 */
@Path("/")
public class ResourceBase extends QueriedResourceBase implements org.graphity.processor.model.Resource
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
    private UpdateRequest updateRequest;
    private QuerySolutionMap querySolutionMap;
    private CacheControl cacheControl;
    private URI mode;

    /**
     * Public JAX-RS constructor. Suitable for subclassing.
     * If the request URI does not match any URI template in the sitemap ontology, 404 Not Found is returned.
     * 
     * If the matching ontology class is a subclass of <code>gp:Page</code>, this resource becomes a page resource and
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
        this.querySolutionMap = new QuerySolutionMap();
        this.graphStore = graphStore;
	this.httpHeaders = httpHeaders;
        this.resourceContext = resourceContext;

	querySolutionMap.add(SPIN.THIS_VAR_NAME, ontResource); // ?this
	querySolutionMap.add(GP.baseUri.getLocalName(), ResourceFactory.createResource(getUriInfo().getBaseUri().toString())); // ?baseUri

        if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
    }

    /**
     * Post-construct initialization. Subclasses need to call super.init() first, just like with super() constructor.
     */
    @Override
    public void init()
    {
	if (log.isDebugEnabled()) log.debug("OntResource {} gets type of OntClass: {}", this, getMatchedOntClass());
	addProperty(RDF.type, getMatchedOntClass());

        if (getUriInfo().getQueryParameters().containsKey(GP.mode.getLocalName()))
            this.mode = URI.create(getUriInfo().getQueryParameters().getFirst(GP.mode.getLocalName()));
        else mode = null;

        try
        {
            Query query = getQuery(getMatchedOntClass(), GP.query);
            if (query == null && getRequest().getMethod().equalsIgnoreCase("GET"))
            {
                if (log.isErrorEnabled()) log.error("Query not defined for template '{}' (gp:query missing)", getMatchedOntClass().getURI());
                throw new ConfigurationException("Query not defined for template '" + getMatchedOntClass().getURI() +"'");
            }
            queryBuilder = QueryBuilder.fromQuery(query, getModel());

            updateRequest = getUpdateRequest(getMatchedOntClass(), GP.update);
            if (updateRequest == null && (getRequest().getMethod().equalsIgnoreCase("PUT") ||
                    getRequest().getMethod().equalsIgnoreCase("DELETE")))
            {
                if (log.isErrorEnabled()) log.error("Update not defined for template '{}' (gp:update missing)", getMatchedOntClass().getURI());
                throw new ConfigurationException("Update not defined for template '" + getMatchedOntClass().getURI() +"'");
            }
            
            if (getMatchedOntClass().equals(GP.Container) || getMatchedOntClass().hasSuperClass(GP.Container))
            {
                if (queryBuilder.getSubSelectBuilder() == null)
                {
                    if (log.isErrorEnabled()) log.error("Container query for template '{}' does not contain a sub-SELECT", getMatchedOntClass().getURI());
                    throw new ConfigurationException("Sub-SELECT missing in the query of container template '" + getMatchedOntClass().getURI() +"'");
                }

                try
                {
                    if (getUriInfo().getQueryParameters().containsKey(GP.offset.getLocalName()))
                        offset = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.offset.getLocalName()));
                    else
                    {
                        Long defaultOffset = getLongValue(getMatchedOntClass(), GP.defaultOffset);
                        if (defaultOffset == null) defaultOffset = Long.valueOf(0); // OFFSET is 0 by default
                        this.offset = defaultOffset;
                    }
                    
                    if (log.isDebugEnabled()) log.debug("Setting OFFSET on container sub-SELECT: {}", offset);
                    queryBuilder.getSubSelectBuilder().replaceOffset(offset);

                    if (getUriInfo().getQueryParameters().containsKey(GP.limit.getLocalName()))
                        limit = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.limit.getLocalName()));
                    else
                    {
                        Long defaultLimit =  getLongValue(getMatchedOntClass(), GP.defaultLimit);
                        //if (defaultLimit == null) throw new IllegalArgumentException("Template class '" + getMatchedOntClass().getURI() + "' must have gp:defaultLimit annotation if it is used as container");
                        this.limit = defaultLimit;                        
                    }
                    
                    if (log.isDebugEnabled()) log.debug("Setting LIMIT on container sub-SELECT: {}", limit);
                    queryBuilder.getSubSelectBuilder().replaceLimit(limit);

                    if (getUriInfo().getQueryParameters().containsKey(GP.orderBy.getLocalName()))
                        this.orderBy = getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName());
                    else
                        this.orderBy = getStringValue(getMatchedOntClass(), GP.defaultOrderBy);
                    
                    if (this.orderBy != null)
                    {
                        if (getUriInfo().getQueryParameters().containsKey(GP.desc.getLocalName()))
                            desc = Boolean.parseBoolean(getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName()));
                        else
                            desc = getBooleanValue(getMatchedOntClass(), GP.defaultDesc);
                        if (desc == null) desc = false; // ORDERY BY is ASC() by default

                        if (log.isDebugEnabled()) log.debug("Setting ORDER BY on container sub-SELECT: ?{} DESC: {}", orderBy, desc);
                        queryBuilder.getSubSelectBuilder().replaceOrderBy(null). // any existing ORDER BY condition is removed first
                            orderBy(orderBy, desc);
                    }

                    if (getMode() != null && getMode().equals(URI.create(GP.ConstructMode.getURI())) &&
                        queryBuilder.getSubSelectBuilder() != null)
                    {
                        if (log.isDebugEnabled()) log.debug("Mode is {}, setting sub-SELECT LIMIT to zero", getMode());
                        queryBuilder.getSubSelectBuilder().replaceLimit(Long.valueOf(0));
                    }
                }
                catch (NumberFormatException ex)
                {
                    throw new WebApplicationException(ex);
                }
            }

            if (log.isDebugEnabled()) log.debug("OntResource {} gets explicit spin:query value {}", this, queryBuilder);
            addProperty(SPIN.query, getQueryBuilder());
        }
        catch (ConfigurationException ex)
        {
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
        
        cacheControl = getCacheControl(getMatchedOntClass(), GP.cacheControl);        
    }

    public Long getLongValue(OntClass ontClass, AnnotationProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getLong();
        
        return null;
    }

    public Boolean getBooleanValue(OntClass ontClass, AnnotationProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getBoolean();
        
        return null;
    }

    public String getStringValue(OntClass ontClass, AnnotationProperty property)
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
        // gp:Container always redirect to first gp:Page
	if ((getMatchedOntClass().equals(GP.Container) || getMatchedOntClass().hasSuperClass(GP.Container))
            && getRealURI().equals(getUriInfo().getRequestUri()) && getLimit() != null)
	{
	    if (log.isDebugEnabled()) log.debug("OntResource is gp:Container, redirecting to the first gp:Page");
	    return Response.seeOther(getStateUriBuilder(getOffset(), getLimit(), getOrderBy(), getDesc(), getMode()).build()).build();
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
	return post(model, null);
    }
    
    /**
     * Handles POST method, stores the submitted RDF model in the specified named graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model the RDF payload
     * @param graphURI target graph name
     * @return response
     */
    public Response post(Model model, URI graphURI)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (log.isDebugEnabled()) log.debug("POSTed Model: {} to GRAPH URI: {}", model, graphURI);

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

        insertDataRequest.setBaseURI(getUriInfo().getBaseUri().toString());
        if (log.isDebugEnabled()) log.debug("INSERT DATA request: {}", insertDataRequest);

	getSPARQLEndpoint().post(insertDataRequest, null, null);
	
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
                if (getMatchedOntClass().hasSuperClass(GP.Container) && res.hasProperty(RDF.type, GP.Container))
                    memberProperty = SIOC.HAS_PARENT;

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
	return put(model, null);
    }
    
    /**
     * Handles PUT method, stores the submitted RDF model in the specified named graph of the specified SPARQL endpoint, and returns response.
     * 
     * @param model RDF payload
     * @param graphURI target graph name
     * @return response
     */
    public Response put(Model model, URI graphURI)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");
	if (log.isDebugEnabled()) log.debug("PUT Model: {} GRAPH URI: {}", model, graphURI);

	if (!model.containsResource(this))
	{
	    if (log.isDebugEnabled()) log.debug("PUT Model does not contain statements with request URI '{}' as subject", getURI());
	    throw new WebApplicationException(Response.Status.BAD_REQUEST);
	}
	
	Model description = describe();	
	UpdateRequest deleteInsertRequest = UpdateFactory.create();
	
	if (!description.isEmpty()) // remove existing representation
	{
	    EntityTag entityTag = new EntityTag(Long.toHexString(ModelUtils.hashModel(model)));
	    Response.ResponseBuilder rb = getRequest().evaluatePreconditions(entityTag);
	    if (rb != null)
	    {
		if (log.isDebugEnabled()) log.debug("PUT preconditions were not met for resource: {} with entity tag: {}", this, entityTag);
		return rb.build();
	    }
	    
	    //UpdateRequest deleteRequest = getUpdateRequest(getMatchedOntClass(), GP.update, this);
	    if (log.isDebugEnabled()) log.debug("DELETE UpdateRequest: {}", getUpdateRequest());
	    Iterator<com.hp.hpl.jena.update.Update> it = getUpdateRequest().getOperations().iterator();
	    while (it.hasNext()) deleteInsertRequest.add(it.next());
	}
	
	UpdateRequest insertDataRequest; 
	if (graphURI != null) insertDataRequest = InsertDataBuilder.fromData(graphURI, model).build();
	else insertDataRequest = InsertDataBuilder.fromData(model).build();
	if (log.isDebugEnabled()) log.debug("INSERT DATA request: {}", insertDataRequest);
	Iterator<com.hp.hpl.jena.update.Update> it = insertDataRequest.getOperations().iterator();
	while (it.hasNext()) deleteInsertRequest.add(it.next());
	
	if (log.isDebugEnabled()) log.debug("Combined DELETE/INSERT DATA request: {}", deleteInsertRequest);
	getSPARQLEndpoint().post(deleteInsertRequest, null, null);
	
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
	if (log.isDebugEnabled()) log.debug("DELETEing resource: {} matched OntClass: {}", this, getMatchedOntClass());
	if (log.isDebugEnabled()) log.debug("DELETE UpdateRequest: {}", getUpdateRequest());
	getSPARQLEndpoint().post(getUpdateRequest(), null, null);
	
	return Response.noContent().build();
    }

    /**
     * Returns RDF description of this resource.
     * The description is the result of a query executed on the SPARQL endpoint of this resource.
     * By default, the query is <code>DESCRIBE</code> with URI of this resource.
     * 
     * @return RDF description
     * @see getQuery()
     */
    @Override
    public Model describe()
    {
	return getSPARQLEndpoint().loadModel(new ParameterizedSparqlString(getQuery().toString(),
                getQuerySolutionMap()).asQuery());
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
	if (getMatchedOntClass().equals(GP.Container) || getMatchedOntClass().hasSuperClass(GP.Container))
	{
            Map<Property, OntClass> childrenClasses = new HashMap<>();
            childrenClasses.putAll(new OntClassMatcher().matchOntClasses(getOntModel(), SIOC.HAS_PARENT, getMatchedOntClass()));
            childrenClasses.putAll(new OntClassMatcher().matchOntClasses(getOntModel(), SIOC.HAS_CONTAINER, getMatchedOntClass()));

            Iterator<OntClass> it = childrenClasses.values().iterator();
            while (it.hasNext())
            {
                OntClass forClass = it.next();
                String constructorURI = getStateUriBuilder(null, null, null, null, URI.create(GP.ConstructMode.getURI())).
                        queryParam(GP.forClass.getLocalName(), forClass.getURI()).build().toString();
                    Resource template = createState(model.createResource(constructorURI), null, null, null, null, GP.ConstructMode).
                        addProperty(RDF.type, FOAF.Document).
                        addProperty(RDF.type, GP.Constructor).
                        addProperty(GP.forClass, forClass).
                        addProperty(GP.constructorOf, this);
            }

            ResIterator resIt = model.listResourcesWithProperty(SIOC.HAS_PARENT, this);
            try
            {
                while (resIt.hasNext())
                {
                    Resource childContainer = resIt.next();
                    URI childURI = URI.create(childContainer.getURI());
                    OntClass childClass = new OntClassMatcher().matchOntClass(getOntModel(), childURI, getUriInfo().getBaseUri());
                    Map<Property, OntClass> grandChildrenClasses = new HashMap<>();
                    grandChildrenClasses.putAll(new OntClassMatcher().matchOntClasses(getOntModel(), SIOC.HAS_PARENT, childClass));
                    grandChildrenClasses.putAll(new OntClassMatcher().matchOntClasses(getOntModel(), SIOC.HAS_CONTAINER, childClass));
                    Iterator<OntClass> gccIt = grandChildrenClasses.values().iterator();
                    while (gccIt.hasNext())
                    {
                        OntClass forClass = gccIt.next();
                        String constructorURI = getStateUriBuilder(UriBuilder.fromUri(childURI), null, null, null, null, URI.create(GP.ConstructMode.getURI())).
                            queryParam(GP.forClass.getLocalName(), forClass.getURI()).build().toString();
                        Resource template = createState(model.createResource(constructorURI), null, null, null, null, GP.ConstructMode).
                            addProperty(RDF.type, FOAF.Document).
                            addProperty(RDF.type, GP.Constructor).
                            addProperty(GP.forClass, forClass).                                    
                            addProperty(GP.constructorOf, childContainer);                    
                    }
                }
            }
            finally
            {
                resIt.close();
            }
            
            if (getMode() != null && getMode().equals(URI.create(GP.ConstructMode.getURI())))
            {
                try
                {
                    if (!getUriInfo().getQueryParameters().containsKey(GP.forClass.getLocalName()))
                        throw new IllegalStateException("gp:ConstructMode is active, but gp:forClass value not supplied");

                    URI forClassURI = new URI(getUriInfo().getQueryParameters().getFirst(GP.forClass.getLocalName()));
                    OntClass forClass = getOntModel().createClass(forClassURI.toString());
                    if (forClass == null) throw new IllegalStateException("gp:ConstructMode is active, but gp:forClass value is not a known owl:Class");

                    Query templateQuery = getQuery(forClass, GP.template);
                    if (templateQuery == null)
                    {
                        if (log.isErrorEnabled()) log.error("gp:ConstructMode is active but template not defined for class '{}' (gp:template missing)", forClass.getURI());
                        throw new ConfigurationException("gp:ConstructMode template not defined for class '" + forClass.getURI() +"'");
                    }
                    
                    QueryExecution qex = QueryExecutionFactory.create(templateQuery, ModelFactory.createDefaultModel());
                    Model templateModel = qex.execConstruct();
                    model.add(templateModel);
                    if (log.isDebugEnabled()) log.debug("gp:template CONSTRUCT query '{}' created {} triples", templateQuery, templateModel.size());
                    qex.close();
                }
                catch (ConfigurationException ex)
                {
                    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
                }
                catch (URISyntaxException ex)
                {
                    if (log.isErrorEnabled()) log.error("gp:ConstructMode is active but gp:forClass value is not a URI: '{}'", getUriInfo().getQueryParameters().getFirst(GP.forClass.getLocalName()));
                    throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
                }
            }
            else
            {                    
                if (getLimit() != null)
                {
                    if (log.isDebugEnabled()) log.debug("Adding Page metadata: gp:pageOf {}", this);
                    String pageURI = getStateUriBuilder(getOffset(), getLimit(), getOrderBy(), getDesc(), null).build().toString();
                    Resource page = createState(model.createResource(pageURI), getOffset(), getLimit(), getOrderBy(), getDesc(), null).
                            addProperty(RDF.type, FOAF.Document).
                            addProperty(RDF.type, GP.Page).
                            addProperty(GP.pageOf, this);

                    if (getOffset() != null && getLimit() != null)
                    {
                        if (getOffset() >= getLimit())
                        {
                            String prevURI = getStateUriBuilder(getOffset() - getLimit(), getLimit(), getOrderBy(), getDesc(), getMode()).build().toString();
                            if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:previous {}", getURI(), prevURI);
                            page.addProperty(XHV.prev, model.createResource(prevURI));
                        }

                        // no way to know if there's a next page without counting results (either total or in current page)
                        //int subjectCount = describe().listSubjects().toList().size();
                        //log.debug("describe().listSubjects().toList().size(): {}", subjectCount);
                        //if (subjectCount >= getLimit())
                        {
                            String nextURI = getStateUriBuilder(getOffset() + getLimit(), getLimit(), getOrderBy(), getDesc(), getMode()).build().toString();
                            if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:next {}", getURI(), nextURI);
                            page.addProperty(XHV.next, model.createResource(nextURI));
                        }
                    }
                }
            }
        }
        
        return model;
    }
    
    /**
     * Creates a page resource for the current container. Includes HATEOS previous/next links.
     * 
     * @param state
     * @param offset
     * @param limit
     * @param orderBy
     * @param desc
     * @param mode
     * @return page resource
     */
    public Resource createState(Resource state, Long offset, Long limit, String orderBy, Boolean desc, Resource mode)
    {
        if (state == null) throw new IllegalArgumentException("Resource subject cannot be null");        

        if (offset != null) state.addLiteral(GP.offset, offset);
        if (limit != null) state.addLiteral(GP.limit, limit);
        if (orderBy != null) state.addLiteral(GP.orderBy, orderBy);
        if (desc != null) state.addLiteral(GP.desc, desc);
        if (mode != null) state.addProperty(GP.mode, mode);
        
        return state;
    }
    
    /**
     * Returns the layout mode query parameter value.
     * 
     * @return mode URI
     */
    public URI getMode()
    {
	return mode;
    }

    /**
     * Returns <code>Cache-Control</code> HTTP header value, specified on an ontology class with given property.
     * 
     * @param ontClass the ontology class with the restriction
     * @param property the property holding the literal value
     * @return CacheControl instance or null, if no dataset restriction was found
     */
    public CacheControl getCacheControl(OntClass ontClass, AnnotationProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return CacheControl.valueOf(ontClass.getPropertyValue(property).asLiteral().getString()); // will fail on bad config

	return null;
    }

    /**
     * Given an RDF resource and an ontology class that it belongs to, returns a SPARQL query that can be used
     * to retrieve its description.
     * The ontology class must have a SPIN template call attached (using <code>spin:query</code>).
     * 
     * @param ontClass ontology class of the resource
     * @param property property for the query object
     * @return query object
     * @see org.topbraid.spin.model.TemplateCall
     */
    public Query getQuery(OntClass ontClass, AnnotationProperty property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");
	//if (ontClass.getPropertyResourceValue(property) == null)
	//    throw new IllegalArgumentException("Resource OntClass must have a SPIN query or template call resource (" + property + ")");

	Resource queryOrTemplateCall = ontClass.getPropertyResourceValue(property);
        if (queryOrTemplateCall != null)
        {
            org.topbraid.spin.model.Query spinQuery = SPINFactory.asQuery(queryOrTemplateCall);
            if (spinQuery != null) return new ParameterizedSparqlString(spinQuery.toString()).asQuery();

            TemplateCall templateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
            if (templateCall != null) return new ParameterizedSparqlString(templateCall.getQueryString()).asQuery();
        }
        
        return null;
    }

    /**
     * Returns variable bindings for description query.
     * 
     * @return map with variable bindings
     */
    public QuerySolutionMap getQuerySolutionMap()
    {
        return querySolutionMap;
    }
    
    public UpdateRequest getUpdateRequest(OntClass ontClass, AnnotationProperty property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");

	Resource updateOrTemplateCall = ontClass.getPropertyResourceValue(property);
	if (updateOrTemplateCall != null)
        {
            Update spinUpdate = SPINFactory.asUpdate(updateOrTemplateCall);
            if (spinUpdate != null) return new ParameterizedSparqlString(spinUpdate.toString()).asUpdate();

            TemplateCall templateCall = SPINFactory.asTemplateCall(updateOrTemplateCall);
            if (templateCall != null) return new ParameterizedSparqlString(templateCall.getQueryString()).asUpdate();
        }
        
        return null;
    }

    public UpdateRequest getUpdateRequest(String updateString, Resource resource)
    {
	if (updateString == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	return new ParameterizedSparqlString(updateString, qsm).asUpdate();
    }

    /**
     * Adds matched sitemap class as affordance metadata in <pre>Link</pre> header.
     * 
     * @param model response model
     * @return response builder
     */
    @Override
    public ResponseBuilder getResponseBuilder(Model model)
    {
        Link classLink = new Link(URI.create(getMatchedOntClass().getURI()), RDF.type.getLocalName(), null);
        return super.getResponseBuilder(model).header("Link", classLink.toString());
    }

    public List<Locale> getLanguages(Property property)
    {
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        
        List<Locale> languages = new ArrayList<>();
        StmtIterator it = getMatchedOntClass().listProperties(property);
        
        try
        {
            while (it.hasNext())
            {
                Statement stmt = it.next();
                if (stmt.getObject().isLiteral()) languages.add(new Locale(stmt.getString()));
            }
        }
        finally
        {
            it.close();
        }
        
        return languages;
    }
    
    @Override
    public List<Locale> getLanguages()
    {
        return getLanguages(GP.lang);
    }
    
    /**
     * Returns value of <samp>limit</samp> query string parameter, which indicates the number of resources per page.
     * This value is set as <code>LIMIT</code> query modifier when this resource is a page (therefore
     * pagination is used).
     * 
     * @return limit value
     * @see <a href="http://www.w3.org/TR/sparql11-query/#modResultLimit">15.5 LIMIT</a>
     */
    //@Override
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
    //@Override
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
    //@Override
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
    //@Override
    public Boolean getDesc()
    {
	return desc;
    }
    
    /**
     * Returns URI builder instantiated with pagination parameters for the current page.
     * 
     * @param offset
     * @param limit
     * @param orderBy
     * @param desc
     * @param mode
     * @return URI builder
     */
    public UriBuilder getStateUriBuilder(Long offset, Long limit, String orderBy, Boolean desc, URI mode)
    {
	return getStateUriBuilder(UriBuilder.fromUri(getURI()), offset, limit, orderBy, desc, mode);
    }

    public UriBuilder getStateUriBuilder(UriBuilder uriBuilder, Long offset, Long limit, String orderBy, Boolean desc, URI mode)
    {        
        if (offset != null) uriBuilder.queryParam(GP.offset.getLocalName(), offset);
        if (limit != null) uriBuilder.queryParam(GP.limit.getLocalName(), limit);
	if (orderBy != null) uriBuilder.queryParam(GP.orderBy.getLocalName(), orderBy);
	if (desc != null) uriBuilder.queryParam(GP.desc.getLocalName(), desc);
        if (mode != null) uriBuilder.queryParam(GP.mode.getLocalName(), mode);
        
	return uriBuilder;
    }

    /**
     * Returns URI of this resource. Uses Java's URI class instead of string as the {@link #getURI()} does.
     * 
     * @return URI object
     */
    public URI getRealURI()
    {
	return URI.create(getURI()); // getUriBuilder().build();
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
        Query query = getQueryBuilder().build();            
        query.setBaseURI(getUriInfo().getBaseUri().toString());            
	return query;
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

    public UpdateRequest getUpdateRequest()
    {
        return updateRequest;
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