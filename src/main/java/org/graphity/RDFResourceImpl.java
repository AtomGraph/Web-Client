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

package org.graphity;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
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
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.graphity.util.manager.DataManager;
import org.graphity.util.QueryBuilder;
import org.graphity.util.SPARULAdapter;
import org.graphity.vocabulary.Graphity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class RDFResourceImpl extends ResourceImpl implements RDFResource
{
    private static final Logger log = LoggerFactory.getLogger(RDFResourceImpl.class);
    
    private com.hp.hpl.jena.rdf.model.Resource resource = null;
    private Model model = null; // ModelFactory.createDefaultModel()
    //private OntModel ontModel = null;
    
    @PostConstruct
    public void init()
    {
	String ontologyUri = getUriInfo().getBaseUriBuilder().path("ontology").build().toString();
	OntDocumentManager.getInstance().addAltEntry(ontologyUri, "ontology.ttl");
	// reading OntModel is necessary to give the right base URI:
	if (OntDocumentManager.getInstance().getModel(ontologyUri) == null)
	{
	    log.debug("Adding OntModel with URI: {} to OntDocumentManager", ontologyUri);
	    OntDocumentManager.getInstance().addModel(ontologyUri, OntDocumentManager.getInstance().getFileManager().loadModel(ontologyUri, getUriInfo().getBaseUri().toString(), FileUtils.langTurtle));
	}
    }

    @GET
    @Produces("text/html; charset=UTF-8")
    public Response getResponse()
    {
	if (getUriInfo().getQueryParameters().getFirst("accept") != null)
	{
	    if (getUriInfo().getQueryParameters().getFirst("accept").equals(MediaType.APPLICATION_RDF_XML))
		return Response.ok(getModel(), MediaType.APPLICATION_RDF_XML_TYPE).
		    build();
	    if (getUriInfo().getQueryParameters().getFirst("accept").equals(MediaType.TEXT_TURTLE))
		return Response.ok(getModel(), MediaType.TEXT_TURTLE_TYPE).
		    build();
	}
	    
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
	if (model == null)
	{
	    if (getFirstParameter("service-uri") == null && getFirstParameter("uri") != null) //  && isRemote()
	    {
		// load remote Linked Data
		try
		{
		    String uri = getFirstParameter("uri");
		    log.debug("Loading Model from URI: {}", uri);

		    model = DataManager.get().loadModel(uri);
		    log.debug("Number of Model stmts read: {}", model.size());		    
		}
		catch (Exception ex)
		{
		    log.trace("Could not load Model from URI: {}", getFirstParameter("uri"));
		    throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
		}
	    }
	    else
	    {
		log.debug("Querying SPARQL endpoint");
		try
		{
		    if (getQuery().isConstructType()) model = getQueryExecution().execConstruct();
		    if (getQuery().isDescribeType()) model = getQueryExecution().execDescribe();
		    //model = getOntModel(); // we're on a local host! load local sitemap
		    log.debug("Number of Model stmts read: {}", model.size());
		}
		catch (Exception ex)
		{
		    log.trace("Could not execute Query: {}", getQuery());
		    throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
		}
	    }

	    log.debug("Caching model to SPARQL endpoint");
	    SPARULAdapter adapter = new SPARULAdapter(getSPARQLResource().getURI());
	    String graphUri = getUriInfo().getBaseUriBuilder().
		path("graphs/{graphId}").
		build(UUID.randomUUID().toString()).toString();
	    adapter.add(graphUri, model);
	    adapter.add(createGraphMetaModel(ResourceFactory.createResource(graphUri)));

	    // RDF/XML description must include some statements about this URI, otherwise it's 404 Not Found
	    //if (!model.containsResource(model.createResource(getURI())))
	    //    throw new WebApplicationException(Response.Status.NOT_FOUND);
	    //if (model == null)
	    //	throw new WebApplicationException(Response.Status.NOT_FOUND);
	}
	
	return model;
    }
    
    public Model createGraphMetaModel(Resource graph)
    {
	Model metaModel = ModelFactory.createDefaultModel();
	
	metaModel.add(metaModel.createLiteralStatement(graph, DCTerms.created, new GregorianCalendar()));
	//metaModel.add(metaModel.createStatement(graph, DCTerms.creator, user));
	
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://www.w3.org/2004/03/trix/rdfg-1/Graph")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://purl.org/net/opmv/ns#Artifact")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://purl.org/net/provenance/ns#DataItem")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://www.w3.org/ns/sparql-service-description#NamedGraph")));
	
	metaModel.add(metaModel.createStatement(graph, metaModel.createProperty("http://purl.org/net/opmv/ns#wasGeneratedBy"), metaModel.createResource("creation")));
	metaModel.add(metaModel.createStatement(metaModel.createResource("creation"), RDF.type, metaModel.createResource("http://purl.org/net/provenance/ns#HTTPBasedDataAccess")));
	//metaModel.add(metaModel.createStatement(metaModel.createResource("creation"), metaModel.createProperty("http://purl.org/net/opmv/ns#wasPerformedBy"), user));
	metaModel.add(metaModel.createStatement(metaModel.createResource("creation"), metaModel.createProperty("http://purl.org/net/provenance/types#exchangedHTTPMessage"), metaModel.createResource("request")));
	metaModel.add(metaModel.createStatement(metaModel.createResource("request"), RDF.type, metaModel.createResource("http://www.w3.org/2011/http#Request")));

	//metaModel.add(metaModel.createLiteralStatement(metaModel.createResource("request"), metaModel.createProperty("http://www.w3.org/2011/http#methodName"), "GET"));
	//metaModel.add(metaModel.createLiteralStatement(metaModel.createResource("request"), metaModel.createProperty("http://www.w3.org/2011/http#absoluteURI"), "GET"));

	log.debug("No of stmts in the metamodel: {} for GRAPH: {}", metaModel.size(), graph.getURI());
	
	return metaModel;
    }
    
    public OntModel getOntModel()
    {
	String ontologyUri = getUriInfo().getBaseUriBuilder().path("ontology").build().toString();
	//log.debug("getOntModel().size(): {}", OntDocumentManager.getInstance().getOntology(ontologyUri, OntModelSpec.OWL_MEM_RDFS_INF).size());
	return OntDocumentManager.getInstance().getOntology(ontologyUri, OntModelSpec.OWL_MEM_RDFS_INF);
	//return ModelFactory.createOntologyModel();
    }
    
    public Resource getService()
    {
	ResIterator it = getOntModel().listResourcesWithProperty(
		getOntModel().getProperty("http://www.w3.org/ns/sparql-service-description#endpoint"),
		getSPARQLResource());
	if (it.hasNext()) return it.nextResource();
	else return null;
    }
    
    public String getServiceApiKey()
    {
	Statement stmt = getService().getProperty(Graphity.apiKey);
	if (stmt != null)
	{
	    log.debug("API key: {} found for SPARQL service: {}", stmt.getLiteral().getLexicalForm(), getService());
	    return stmt.getLiteral().getLexicalForm();
	}
	else
	    log.debug("API key not found for SPARQL service: {}", getService());
	
	return null;
    }
    
    public QueryExecution getQueryExecution()
    {
	if (getFirstParameter("service-uri") != null || getSPARQLResource() != null)
	{
	    String serviceUri = (getFirstParameter("service-uri") != null) ?
		    getFirstParameter("service-uri") : getSPARQLResource().getURI();
	    
	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(serviceUri, getQuery());
	    request.setBasicAuthentication(getServiceApiKey(), "X".toCharArray());
	    log.trace("Request to SPARQL endpoint: {} with query: {}", serviceUri, getQuery());	    
	    return request;
	}
	else
	{
	    log.trace("Querying Model: {} with query: {}", getOntModel(), getQuery());
	    return QueryExecutionFactory.create(getQuery(), getOntModel());
	    //return QueryExecutionFactory.create(getDefaultQuery(), getOntModel());
	}
    }
    
    public Query getQuery()
    {
	if (getFirstParameter("service-uri") != null)
	{
	    if (getFirstParameter("uri") == null) // ?uri=&service-uri=http://dbpedia.org/sparql
		return getExplicitQuery();
	    else // ?uri=http://dbpedia.org/resource/Vilnius&service-uri=http://dbpedia.org/sparql
		return getDescribeQuery(getFirstParameter("uri"));
	}
	else
	{
	    if (getSPARQLResource() != null)
		return getExplicitQuery();
	    else
		return getDescribeQuery(getURI());  // query OntModel
	}

	//if (query == null) query = getDefaultQuery();
	//if (query == null) query = QueryFactory.create("DESCRIBE <" + getURI() + ">");
	//return null;
    }

    public Query getExplicitQuery()
    {
	if (getIndividual() == null) return null;
	Resource queryRes = getIndividual().getPropertyResourceValue(Graphity.query);
	
	log.trace("Explicit query resource {} for URI {}", queryRes, getURI());
	if (queryRes == null) return null;
	
	return QueryBuilder.fromResource(queryRes).
	    bind("uri", getURI()).
	    build();
    }

    public Query getDescribeQuery(String uri)
    {
log.trace("Default query {} for URI {}", "DESCRIBE <" + uri + ">", uri);
return QueryFactory.create("DESCRIBE <" + uri + ">");

	/*
	Resource queryRes = getBaseIndividual().getPropertyResourceValue(Graphity.defaultQuery);
	log.trace("Default query resource {} for URI {}", queryRes, getURI());
	if (queryRes == null) return null;
		
	return QueryBuilder.fromResource(queryRes).
	    bind("uri", uri).
	    build();
	 */
    }
	
    protected Resource getResource()
    {
	if (resource == null)
	    resource = getModel().createResource(getURI());
	
	return resource;
    }
    
    protected Individual getIndividual()
    {
	log.debug("getUriInfo().getAbsolutePath().toString(): {}", getURI());
	return getOntModel().getIndividual(getURI());
	//return getOntModel().getIndividual(getUriInfo().getBaseUri().toString());
    }

    protected Individual getBaseIndividual()
    {
	log.debug("getUriInfo().getBaseUri().toString(): {}", getUriInfo().getBaseUri().toString());
	return getOntModel().getIndividual(getUriInfo().getBaseUri().toString());
    }

    /*
    @Override
    public String getURI()
    {
	if (getFirstParameter("uri") != null)
	    return getFirstParameter("uri");
	
	return super.getURI();
    }
    */
    
    private String getFirstParameter(String name)
    {
	if (getUriInfo().getQueryParameters().getFirst(name) != null &&
		!getUriInfo().getQueryParameters().getFirst(name).isEmpty())
	    return getUriInfo().getQueryParameters().getFirst(name);
	return null;
    }
    
    @Override
    public com.hp.hpl.jena.rdf.model.Resource getSPARQLResource()
    {
	log.trace("getBaseIndividual(): {}", getBaseIndividual());
	if (getBaseIndividual() == null) return null;

	return getBaseIndividual().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://purl.org/linked-data/api/vocab#sparqlEndpoint"));

	/*
	return getBaseIndividual().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#inDataset")).
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#sparqlEndpoint")).
		getURI();
	 */
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
