/**
 *  Copyright 2012 Graphity Team
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
 *  @package        org.graphity
 *  @author         Martynas Jusevičius <martynas@graphity.org>
 *  @link           http://graphity.org/
 */

package org.graphity;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.graphity.provider.ModelProvider;
import org.graphity.util.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Pumba
 */
abstract public class RDFResourceImpl extends ResourceImpl implements RDFResource
{
    public static final Map<javax.ws.rs.core.MediaType, Double> QUALIFIED_TYPES;    
    static
    {
	Map<javax.ws.rs.core.MediaType, Double> typeMap = new HashMap<javax.ws.rs.core.MediaType, Double>();
	
	typeMap.put(MediaType.APPLICATION_RDF_XML_TYPE, null);

	typeMap.put(MediaType.TEXT_TURTLE_TYPE, 0.9);
	
	typeMap.put(MediaType.TEXT_PLAIN_TYPE, 0.7);
	
	typeMap.put(MediaType.APPLICATION_XML_TYPE, 0.5);
	
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }    
    private static final Logger log = LoggerFactory.getLogger(RDFResourceImpl.class);
    
    private com.hp.hpl.jena.rdf.model.Resource resource = null;
    private ClientConfig config = new DefaultClientConfig();
    private Client client = null;

    public RDFResourceImpl()
    {
	config.getClasses().add(ModelProvider.class);
	client = Client.create(config); // add OAuth filter
    }

    @GET
    @Produces("text/html; charset=UTF-8")
    public Response getResponse()
    {
	return Response.ok(this).
	    //type(MediaType.TEXT_HTML).
	    build();

    }

    // 2 options here: load RDF/XML directly from getURI(), or via DESCRIBE from SPARQL endpoint
    // http://openjena.org/wiki/ARQ/Manipulating_SPARQL_using_ARQ
    @Override
    @GET
    @Produces("text/plain; charset=UTF-8")
    public Model getModel()
    {
	Model model = null; // ModelFactory.createDefaultModel();

	log.debug("getURI(): {}", getURI());
	log.debug("getServiceURI(): {}", getServiceURI());

	// query the available SPARQL endpoint (either local or remote)
	if (getServiceURI() != null)
	{
	    try
	    {
		QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(getServiceURI(), getQuery());
		request.setBasicAuthentication("M6aF7uEY9RBQLEVyxjUG", "X".toCharArray());
		log.trace("Request to SPARQL endpoint: {} with query: {}", getServiceURI(), getQuery());
		if (getQuery().isConstructType()) model = request.execConstruct();
		if (getQuery().isDescribeType()) model = request.execDescribe();
	    }
	    catch (Exception ex)
	    {
		log.trace("Could not load Model from SPARQL endpoint: {} with query: {}", getURI(), getQuery());
		//throw new WebApplicationException("Only CONSTRUCT and DESCRIBE SPARQL queries are supported", Response.Status.BAD_REQUEST);
	    }
	}
	else
	{
	    if (isRemote())
	    // load remote Linked Data
	    try
	    {
		log.trace("Loading Model from URI: {} with Accept header: {}", getURI(), getAcceptHeader());
		model = client.resource(getURI()).
			header("Accept", getAcceptHeader()).
			get(Model.class);
		log.debug("Number of Model stmts read: {}", model.size());
	    }
	    catch (Exception ex)
	    {
		log.trace("Could not load Model from URI: {}", getURI());
	    }
	   else
		model = getOntModel(); // we're on a local host! load local sitemap
	}

	// RDF/XML description must include some statements about this URI, otherwise it's 404 Not Found
	//if (!model.containsResource(model.createResource(getURI())))
	//    throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (model == null)
	    throw new WebApplicationException(Response.Status.NOT_FOUND);
	
	return model;
    }
    
    protected boolean isRemote()
    {
	// resolve somehow better?
	return !getURI().startsWith(getUriInfo().getBaseUri().toString());
    }
    
    public OntModel getOntModel()
    {
	OntModel ontModel = ModelFactory.createOntologyModel(); // .createDefaultModel().

	log.debug("@base: {}", getUriInfo().getBaseUri().toString());
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/graphity.ttl"), null, FileUtils.langTurtle);
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/sitemap.ttl"), getUriInfo().getBaseUri().toString(), FileUtils.langTurtle);
	
	return ontModel;
    }
    
    public Query getQuery()
    {	
	ResIterator it = getOntModel().listResourcesWithProperty(RDF.type, SP.Construct);
	
	if (it.hasNext())
	    return QueryBuilder.fromResource(it.nextResource()).
		//bind("subject", getURI()).
		build();
	else
	    return QueryFactory.create("DESCRIBE <" + getURI() + ">"); // DESCRIBE as default
    }

    protected Resource getResource()
    {
	if (resource == null)
	    resource = getModel().createResource(getURI());
	
	return resource;
    }
    
    protected Individual getIndividual()
    {
	log.debug("getUriInfo().getAbsolutePath().toString(): {}", getUriInfo().getAbsolutePath().toString());
	return getOntModel().getIndividual(getUriInfo().getAbsolutePath().toString());
	//return getOntModel().getIndividual(getUriInfo().getBaseUri().toString());
    }

    @Override
    public String getURI()
    {
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	    return getUriInfo().getQueryParameters().getFirst("uri");
	
	return super.getURI();
	
	/*
	return getUriInfo().getAbsolutePathBuilder().
		host("local.heltnormalt.dk").
		port(-1).
		replacePath("striben").
		queryParam("view", "rdf").
		build().
		toString();
	*/
    }
    
    @Override
    public final String getServiceURI()
    {
	// browsing remote resource, SPARQL endpoint is either supplied or null (or discovered from voiD?)
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	{
	    String serviceUri = getUriInfo().getQueryParameters().getFirst("service-uri");
	    if (serviceUri == null || serviceUri.isEmpty()) return null;
	    return serviceUri;
	}
	log.trace("getIndividual(): {}", getIndividual());
	// browsing local resource, SPARQL endpoint is retrieved from the sitemap
	if (getIndividual() == null) return null;
	
	return getIndividual().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#inDataset")).
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#sparqlEndpoint")).
		getURI();
    }

    public String getAcceptHeader()
    {
	String header = null;

	//for (Map.Entry<String, Double> type : getQualifiedTypes().entrySet())
	Iterator <Entry<javax.ws.rs.core.MediaType, Double>> it = QUALIFIED_TYPES.entrySet().iterator();
	while (it.hasNext())
	{
	    Entry<javax.ws.rs.core.MediaType, Double> type = it.next();
	    if (header == null) header = "";
	    
	    header += type.getKey();
	    if (type.getValue() != null) header += ";q=" + type.getValue();
	    
	    if (it.hasNext()) header += ",";
	}
	
	return header;
    }

    @Override
    public Date getLastModified()
    {
	//ResIterator it = getModel().listResourcesWithProperty(DCTerms.modified);
	return null;
    }
    
    @Override
    public AnonId getId()
    {
	return getResource().getId();
    }

    @Override
    public Resource inModel(Model m)
    {
	return getResource().inModel(m);
    }

    @Override
    public boolean hasURI(String uri)
    {
	return getResource().hasURI(uri);
    }

    @Override
    public String getNameSpace()
    {
	return getResource().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return getResource().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property p)
    {
	return getResource().getRequiredProperty(p);
    }

    @Override
    public Statement getProperty(Property p)
    {
	return getResource().getProperty(p);
    }

    @Override
    public StmtIterator listProperties(Property p)
    {
	return getResource().listProperties(p);
    }

    @Override
    public StmtIterator listProperties()
    {
	return getResource().listProperties();
    }

    @Override
    public Resource addLiteral(Property p, boolean o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, long o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, char o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property value, double d)
    {
	return getResource().addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property value, float d)
    {
	return getResource().addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property p, Object o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, Literal o)
    {
	return getResource().addLiteral(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o)
    {
	return getResource().addProperty(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o, String l)
    {
	return getResource().addProperty(p, o, l);
    }

    @Override
    public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype)
    {
	return getResource().addProperty(p, lexicalForm, datatype);
    }

    @Override
    public Resource addProperty(Property p, RDFNode o)
    {
	return getResource().addProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p)
    {
	return getResource().hasProperty(p);
    }

    @Override
    public boolean hasLiteral(Property p, boolean o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, long o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, char o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, double o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, float o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, Object o)
    {
	return getResource().hasLiteral(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o)
    {
	return getResource().hasProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o, String l)
    {
	return getResource().hasProperty(p, o, l);
    }

    @Override
    public boolean hasProperty(Property p, RDFNode o)
    {
	return getResource().hasProperty(p, o);
    }

    @Override
    public Resource removeProperties()
    {
	return getResource().removeProperties();
    }

    @Override
    public Resource removeAll(Property p)
    {
	return getResource().removeAll(p);
    }

    @Override
    public Resource begin()
    {
	return getResource().begin();
    }

    @Override
    public Resource abort()
    {
	return getResource().abort();
    }

    @Override
    public Resource commit()
    {
	return getResource().commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property p)
    {
	return getResource().getPropertyResourceValue(p);
    }

    @Override
    public boolean isAnon()
    {
	return getResource().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return getResource().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return getResource().isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return getResource().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> view)
    {
	return getResource().as(view);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> view)
    {
	return getResource().canAs(view);
    }

    @Override
    public Object visitWith(RDFVisitor rv)
    {
	return getResource().visitWith(rv);
    }

    @Override
    public Resource asResource()
    {
	return getResource().asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return getResource().asLiteral();
    }

    @Override
    public Node asNode()
    {
	return getResource().asNode();
    }
}
