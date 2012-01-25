/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.graphity.util.QueryBuilder;

/**
 *
 * @author Pumba
 */
abstract public class RDFResourceImpl extends ResourceImpl implements RDFResource
{
    //@QueryParam("service-uri") String serviceUri = null;
    //String serviceUri = null;
    
    //public static final String SERVICE_URI = "http://dolph.heltnormalt.dk:82/local/query";
    //public static final String SERVICE_URI = "http://dbpedia.org/sparql";
    //public static final String SERVICE_URI = "http://de.dydra.com/heltnormalt/testing/sparql";

    //public static final String QUERY_STRING = "CONSTRUCT{ ?uri ?forwardProp ?object . ?subject ?backwardProp ?uri } WHERE { { SELECT * WHERE { GRAPH ?graph { ?uri ?forwardProp ?object } } LIMIT 10 } UNION { SELECT * WHERE { GRAPH ?graph { ?subject ?backwardProp ?uri } } LIMIT 10 } }";
    
    private Model model = null;
    private com.hp.hpl.jena.rdf.model.Resource resource = null;

    // 2 options here: load RDF/XML directly from getURI(), or via DESCRIBE from SPARQL endpoint
    // http://openjena.org/wiki/ARQ/Manipulating_SPARQL_using_ARQ
    @Override
    @GET
    @Produces("text/plain")
    public Model getModel()
    {
System.out.println("getURI(): " + getURI());
System.out.println("getServiceURI(): " + getServiceURI());

	//if (model == null)
	if (getServiceURI() != null)
	{
	    //Query query = QueryFactory.create("DESCRIBE <" + getURI() + ">");
	    //QueryExecution qex = QueryExecutionFactory.sparqlService(getServiceURI(), query);
	    //model = qex.execDescribe();

	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(getServiceURI(), getQuery());
	    request.setBasicAuthentication("M6aF7uEY9RBQLEVyxjUG", "X".toCharArray());
	    model = request.execConstruct();
	}
	else
	{
	    model = FileManager.get().loadModel(getURI());
	    //JenaReader reader = new JenaReader();
	    //reader.read(model, getURI());
	}
	    
	
	return model;
    }

    public OntModel getOntModel()
    {
	OntModel ontModel = ModelFactory.createOntologyModel(); // .createDefaultModel().
	
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/ontology.n3"), null, FileUtils.langTurtle);
	ontModel.read(getServletContext().getResourceAsStream("/WEB-INF/structure.n3"), null, FileUtils.langTurtle);
	
	return ontModel;
    }
    
    public Query getQuery()
    {	
	return QueryBuilder.fromResource(getIndividual().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://graphity.org/ontology/query"))).
	    bind("uri", getURI()).
	    build();
    }

    protected Resource getResource()
    {
	if (resource == null)
	    resource = getModel().createResource(getURI());
	
	return resource;
    }
    
    protected Individual getIndividual()
    {
	return getOntModel().getIndividual(getUriInfo().getAbsolutePath().toString());
    }
    
    @Override
    public final String getServiceURI()
    {
	if (getUriInfo().getQueryParameters().getFirst("service-uri") != null)
	    return getUriInfo().getQueryParameters().getFirst("service-uri");
	
	return getIndividual
		().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#inDataset")).
		getPropertyResourceValue(getOntModel().
		    getProperty("http://rdfs.org/ns/void#sparqlEndpoint")).
		getURI();
    }

    @Override
    public boolean exists()
    {
	return getModel().containsResource(this);
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
