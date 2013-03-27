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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.uri.UriTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.TreeMap;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.update.InsertDataBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.VoID;
import org.graphity.processor.vocabulary.XHV;
import org.graphity.server.model.QueriedResourceBase;
import org.graphity.server.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Base class of generic read-write Graphity Platform resources
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see PageResource
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/OntResource.html">OntResource</a>
 * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
@Path("{path: .*}")
public class ResourceBase extends QueriedResourceBase implements PageResource, OntResource
{
    private static final Logger log = LoggerFactory.getLogger(ResourceBase.class);

    private final OntResource ontResource;
    private final Long limit, offset;
    private final String orderBy;
    private final Boolean desc;
    private final OntClass matchedOntClass;
    private final Resource dataset;
    private final SPARQLEndpointBase endpoint;
    private final UriInfo uriInfo;
    private final Request request;
    private final HttpHeaders httpHeaders;
    private final ResourceConfig resourceConfig;

    /**
     * Configuration property for ontology file location (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_LOCATION = "org.graphity.platform.ontology.location";
    
    /**
     * Configuration property for ontology path relative to the base URI (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_PATH = "org.graphity.platform.ontology.path";

    /**
     * Configuration property for absolute ontology URI (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_URI = "org.graphity.platform.ontology.uri";

    /**
     * Configuration property for ontology SPARQL endpoint (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_ENDPOINT = "org.graphity.platform.ontology.endpoint";

    /**
     * Configuration property for ontology named graph URI (set in web.xml)
     * 
     */
    public static final String PROPERTY_ONTOLOGY_GRAPH = "org.graphity.platform.ontology.graph";

    /**
     * Reads ontology from configured file and resolves against base URI of the request
     * @param uriInfo JAX-RS URI info
     * @param config configuration from web.xml
     * @return ontology Model
     * @see <a href="http://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/core/ResourceConfig.html">ResourceConfig</a>
     */
    public static OntModel getOntology(UriInfo uriInfo, ResourceConfig config)
    {
	if (log.isDebugEnabled()) log.debug("web.xml properties: {}", config.getProperties());
	Object ontologyPath = config.getProperty(PROPERTY_ONTOLOGY_PATH);
	if (ontologyPath == null) throw new IllegalArgumentException("Property '" + PROPERTY_ONTOLOGY_PATH + "' needs to be set in ResourceConfig (web.xml)");
	
	String localUri = uriInfo.getBaseUriBuilder().path(ontologyPath.toString()).build().toString();

	if (config.getProperty(PROPERTY_ONTOLOGY_ENDPOINT) != null)
	{
	    Object ontologyEndpoint = config.getProperty(PROPERTY_ONTOLOGY_ENDPOINT);
	    Object graphUri = config.getProperty(PROPERTY_ONTOLOGY_GRAPH);
	    Query query;
	    if (graphUri != null)
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from named graph {} in SPARQL endpoint {}", graphUri.toString(), ontologyEndpoint);
		query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphUri.toString() +  "> { ?s ?p ?o } }");
	    }
	    else
	    {
		if (log.isDebugEnabled()) log.debug("Reading ontology from default graph in SPARQL endpoint {}", ontologyEndpoint);
		query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");		
	    }
    
	    OntDocumentManager.getInstance().addModel(localUri,
		    DataManager.get().loadModel(ontologyEndpoint.toString(), query),
		    true);	    
	}
	else
	{
	    if (config.getProperty(PROPERTY_ONTOLOGY_URI) != null)
	    {
		Object externalUri = config.getProperty(PROPERTY_ONTOLOGY_URI);
		if (log.isDebugEnabled()) log.debug("Reading ontology from remote file with URI: {}", externalUri);
		OntDocumentManager.getInstance().addModel(localUri,
			DataManager.get().loadModel(externalUri.toString()),
			true);
	    }
	    else
	    {
		Object ontologyLocation = config.getProperty(PROPERTY_ONTOLOGY_LOCATION);
		if (ontologyLocation == null) throw new IllegalStateException("Ontology for this Graphity LDP Application is not configured properly. Check ResourceConfig and/or web.xml");
		if (log.isDebugEnabled()) log.debug("Mapping ontology to a local file: {}", ontologyLocation.toString());
		OntDocumentManager.getInstance().addAltEntry(localUri, ontologyLocation.toString());
	    }
	}
	OntModel ontModel = OntDocumentManager.getInstance().
		getOntology(localUri, OntModelSpec.OWL_MEM_RDFS_INF);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }
    
    public static OntModel getOntology(String ontologyUri, String ontologyLocation)
    {
	//if (!OntDocumentManager.getInstance().getFileManager().hasCachedModel(baseUri)) // not cached
	{	    
	    if (log.isDebugEnabled())
	    {
		log.debug("Ontology not cached, reading from file: {}", ontologyLocation);
		log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
		log.debug("Adding name/altName mapping: {} altName: {} ", ontologyUri, ontologyLocation);
	    }
	    OntDocumentManager.getInstance().addAltEntry(ontologyUri, ontologyLocation);

	    LocationMapper mapper = OntDocumentManager.getInstance().getFileManager().getLocationMapper();
	    if (log.isDebugEnabled()) log.debug("Adding prefix/altName mapping: {} altName: {} ", ontologyUri, ontologyLocation);
	    mapper.addAltPrefix(ontologyUri, ontologyLocation);
	}
	//else
	    //if (log.isDebugEnabled()) log.debug("Ontology already cached, returning cached instance");

	OntModel ontModel = OntDocumentManager.getInstance().getOntology(ontologyUri, OntModelSpec.OWL_MEM_RDFS_INF);
	if (log.isDebugEnabled()) log.debug("Ontology size: {}", ontModel.size());
	return ontModel;
    }

    public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders,
	    @Context ResourceConfig resourceConfig, @Context ResourceContext resourceContext,
	    @QueryParam("limit") @DefaultValue("20") Long limit,
	    @QueryParam("offset") @DefaultValue("0") Long offset,
	    @QueryParam("order-by") String orderBy,
	    @QueryParam("desc") Boolean desc)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		getOntology(uriInfo, resourceConfig),
		new SPARQLEndpointBase(ResourceFactory.createResource(uriInfo.getBaseUriBuilder().path(SPARQLEndpointBase.class).build().toString()),
		    uriInfo, request, resourceConfig),
		(resourceConfig.getProperty(PROPERTY_CACHE_CONTROL) == null) ? null : CacheControl.valueOf(resourceConfig.getProperty(PROPERTY_CACHE_CONTROL).toString()),
		limit, offset, orderBy, desc);
    }
    
    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntModel ontModel, SPARQLEndpointBase endpoint,
	    CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc)
    {
	this(uriInfo, request, httpHeaders, resourceConfig,
		ontModel.createOntResource(uriInfo.getRequestUri().toString()),
		endpoint, cacheControl,
		limit, offset, orderBy, desc);
	
	if (log.isDebugEnabled()) log.debug("Constructing Graphity Server ResourceBase");
    }

    protected ResourceBase(UriInfo uriInfo, Request request, HttpHeaders httpHeaders, ResourceConfig resourceConfig,
	    OntResource ontResource, SPARQLEndpointBase endpoint, CacheControl cacheControl,
	    Long limit, Long offset, String orderBy, Boolean desc)
    {
	super(ontResource, endpoint, cacheControl);

	if (request == null) throw new IllegalArgumentException("Request cannot be null");
	if (resourceConfig == null) throw new IllegalArgumentException("ResourceConfig cannot be null");

	this.ontResource = ontResource;
	this.uriInfo = uriInfo;
	this.request = request;
	this.httpHeaders = httpHeaders;
	this.resourceConfig = resourceConfig;
	this.limit = limit;
	this.offset = offset;
	this.orderBy = orderBy;
	this.desc = desc;
	
	matchedOntClass = matchOntClass(getRealURI(), uriInfo.getBaseUri());
	if (matchedOntClass == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
	
	if (matchedOntClass.hasSuperClass(LDP.Page)) //if (hasRDFType(LDP.Page))
	{
	    OntResource container = getOntModel().createOntResource(uriInfo.getAbsolutePath().toString());
	    if (log.isDebugEnabled()) log.debug("Adding PageResource metadata: {} ldp:pageOf {}", getResource(), container);
	    addProperty(LDP.pageOf, container); // setPropertyValue(LDP.pageOf, container);

	    if (log.isDebugEnabled())
	    {
		log.debug("OFFSET: {} LIMIT: {}", getOffset(), getLimit());
		log.debug("ORDER BY: {} DESC: {}", getOrderBy(), getDesc());
	    }

	    if (getOffset() >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:previous {}", getURI(), getPrevious().getURI());
		addProperty(XHV.prev, getPrevious());
	    }

	    // no way to know if there's a next page without counting results (either total or in current page)
	    //int subjectCount = describe().listSubjects().toList().size();
	    //log.debug("describe().listSubjects().toList().size(): {}", subjectCount);
	    //if (subjectCount >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:next {}", getURI(), getNext().getURI());
		addProperty(XHV.next, getNext());
	    }
	}
	
	dataset = getDataset(matchedOntClass);
	if (dataset != null && dataset.hasProperty(VoID.sparqlEndpoint))
	    this.endpoint = new SPARQLEndpointBase(dataset.getPropertyResourceValue(VoID.sparqlEndpoint),
		    uriInfo, request, resourceConfig);
	else this.endpoint = endpoint;	    
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with Dataset: {} and SPARQL endpoint: {}", dataset, endpoint);
    }

    @Override
    public Response getResponse()
    {
	 // ldp:Container always redirects to first ldp:Page
	if (hasRDFType(LDP.Container))
	{
	    if (log.isDebugEnabled()) log.debug("OntResource is ldp:Container, redirecting to the first ldp:Page");
	    //if (log.isDebugEnabled()) log.debug("Encoded order-by URI: {}", UriComponent.encode(getOrderBy(), UriComponent.Type.QUERY));

	    UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().
		queryParam("limit", getLimit()).
		queryParam("offset", getOffset());
	    //if (getOrderBy() != null) uriBuilder.queryParam("order-by", UriComponent.encode(getOrderBy(), UriComponent.Type.QUERY));
	    //if (getDesc() != null) uriBuilder.queryParam("desc", getDesc());
	    
	    return Response.seeOther(uriBuilder.buildFromEncoded()).build();
	}

	return super.getResponse();
    }

    @Override
    public Model describe()
    {
	return describe(true);
    }
    
    public Model describe(boolean addContainerPage)
    {	
	Model description = super.describe();
	if (log.isDebugEnabled()) log.debug("Generation Response from description Model with {} triples", description.size());

	if (addContainerPage && hasRDFType(LDP.Page))
	{
	    if (log.isDebugEnabled()) log.debug("Adding description of the ldp:Page");
	    description.add(getEndpoint().loadModel(getOntModel(), super.getQuery()));
	    
	    if (log.isDebugEnabled()) log.debug("Adding description of the ldp:Container");
	    OntResource container = getPropertyResourceValue(LDP.pageOf).as(OntResource.class);
	    ResourceBase ldc = new ResourceBase(getUriInfo(), getRequest(), getHttpHeaders(), getResourceConfig(),
		    container, getEndpoint(), getCacheControl(),
		    getLimit(), getOffset(), getOrderBy(), getDesc());
	    description.add(getEndpoint().loadModel(ldc.getOntModel(), ldc.getQuery()));
	}

	return description;
    }

    public final Resource getDataset(OntClass ontClass)
    {
	RDFNode hasValue = getRestrictionHasValue(ontClass, VoID.inDataset);
	if (hasValue != null && hasValue.isResource()) return hasValue.asResource();

	return null;
    }

    public QueryBuilder getQueryBuilder(org.topbraid.spin.model.Query query)
    {
	QueryBuilder qb = QueryBuilder.fromQuery(query);
	if (qb.getSubSelectBuilder() == null) throw new IllegalArgumentException("The SPIN query for ldp:Page class does not have a SELECT subquery");
	
	SelectBuilder selectBuilder = qb.getSubSelectBuilder().
	    limit(getLimit()).offset(getOffset());
	/*
	if (orderBy != null)
	{
	    Resource modelVar = getOntology().createResource().addLiteral(SP.varName, "model");
	    Property orderProperty = ResourceFactory.createProperty(getOrderBy();
	    Resource orderVar = getOntology().createResource().addLiteral(SP.varName, orderProperty.getLocalName());

	    selectBuilder.orderBy(orderVar, getDesc()).optional(modelVar, orderProperty, orderVar);
	}
	*/

	return qb;
    }

    public Query getQuery(URI thisUri)
    {
	return getQuery(getMatchedOntClass(), thisUri);
    }

    public Query getQuery(OntClass ontClass, URI thisUri)
    {
	return getQuery(getTemplateCall(ontClass), thisUri);
    }
    
    public TemplateCall getTemplateCall(OntClass ontClass)
    {
	if (!ontClass.hasProperty(SPIN.constraint))
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN constraint Template");	    

	RDFNode constraint = getModel().getResource(ontClass.getURI()).getProperty(SPIN.constraint).getObject();
	return SPINFactory.asTemplateCall(constraint);
    }
    
    public Query getQuery(TemplateCall call, URI thisUri)
    {
	if (call == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	String queryString = call.getQueryString();
	queryString = queryString.replace("?this", "<" + thisUri.toString() + ">"); // binds ?this to URI of current resource
	Query arqQuery = QueryFactory.create(queryString);
	
	if (hasRDFType(LDP.Page))
	{
	    if (log.isDebugEnabled()) log.debug("OntResource is an ldp:Page, making QueryBuilder from Query: {}", arqQuery);
	    return getQueryBuilder(ARQ2SPIN.parseQuery(arqQuery.toString(), getModel())).build();
	}
		
	return arqQuery;
    }

    public final OntClass matchOntClass(URI uri, URI base)
    {
	StringBuilder path = new StringBuilder();
	// instead of path, include query string by relativizing request URI against base URI
	path.append("/").append(base.relativize(uri));
	return matchOntClass(path);
    }
    
    public final OntClass matchOntClass(CharSequence path)
    {
	Property utProp = getOntModel().createProperty("http://purl.org/linked-data/api/vocab#uriTemplate");
	ExtendedIterator<Restriction> it = getOntModel().listRestrictions();
	TreeMap<UriTemplate, OntClass> matchedClasses = new TreeMap<UriTemplate,OntClass>(UriTemplate.COMPARATOR);
		
	while (it.hasNext())
	{
	    Restriction restriction = it.next();	    
	    if (restriction.canAs(HasValueRestriction.class)) // throw new IllegalArgumentException("Resource matching this URI template is not a HasValueRestriction");
	    {
		HasValueRestriction hvr = restriction.asHasValueRestriction();
		if (hvr.getOnProperty().equals(utProp))
		{
		    UriTemplate uriTemplate = new UriTemplate(hvr.getHasValue().toString());
		    HashMap<String, String> map = new HashMap<String, String>();

		    if (uriTemplate.match(path, map))
		    {
			if (log.isDebugEnabled()) log.debug("Path {} matched UriTemplate {}", path, uriTemplate);

			OntClass ontClass = hvr.listSubClasses(true).next(); //hvr.getSubClass();	    
			if (log.isDebugEnabled()) log.debug("Path {} matched endpoint OntClass {}", path, ontClass);
			//return ontClass;
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
	    return matchedClasses.firstEntry().getValue(); //matchedClasses.lastEntry().getValue();
	}
	
	if (log.isDebugEnabled()) log.debug("Path {} has no OntClass match in this OntModel", path);
	return null;
    }

    @Override
    public final Long getLimit()
    {
	return limit;
    }

    @Override
    public final Long getOffset()
    {
	return offset;
    }

    @Override
    public final String getOrderBy()
    {
	return orderBy;
    }

    @Override
    public final Boolean getDesc()
    {
	return desc;
    }

    public RDFNode getRestrictionHasValue(OntClass ontClass, OntProperty property)
    {
	ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
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

    @Override
    public final Resource getPrevious()
    {
	UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().
	    queryParam("limit", getLimit()).
	    queryParam("offset", getOffset() - getLimit());
	//if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	//if (getDesc() != null) uriBuilder.queryParam("desc", getDesc());

	return getOntModel().createResource(uriBuilder.build().toString());
    }

    @Override
    public final Resource getNext()
    {
	UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().
	    queryParam("limit", getLimit()).
	    queryParam("offset", getOffset() + getLimit());
	//if (getOrderBy() != null) uriBuilder.queryParam("order-by", getOrderBy());
	//if (getDesc() != null) uriBuilder.queryParam("desc", getDesc());

	return getOntModel().createResource(uriBuilder.build().toString());
    }

    public final UriBuilder getUriBuilder()
    {
	return UriBuilder.fromUri(getURI());
    }
    
    public final URI getRealURI()
    {
	return getUriBuilder().build();
    }

    public OntResource getOntResource()
    {
	return ontResource;
    }

    public OntClass getMatchedOntClass()
    {
	return matchedOntClass;
    }

    public Resource getDataset()
    {
	return dataset;
    }

    @Override
    public SPARQLEndpointBase getEndpoint()
    {
	return endpoint;
    }

    @Override
    public Query getQuery()
    {
	return getQuery(getRealURI());
    }

    public QueryBuilder getQueryBuilder()
    {
	return QueryBuilder.fromQuery(getQuery(), getModel());
	//return queryBuilder;
    }

    //@Override
    public Response post(Model model)
    {
	if (log.isDebugEnabled()) log.debug("Returning @POST Response of the POSTed Model");
	
	InsertDataBuilder insertBuilder = InsertDataBuilder.fromData(model);
	if (log.isDebugEnabled()) log.debug("INSERT DATA generated from the POSTed Model: {}", insertBuilder);
	
	//getQueryBuilder()
	
	return getResponseBuilder(model).build();
    }

    public final UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Request getRequest()
    {
	return request;
    }

    public HttpHeaders getHttpHeaders()
    {
	return httpHeaders;
    }

    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
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