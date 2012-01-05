/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 *
 * @author Pumba
 */
abstract public class RDFResourceImpl extends ResourceImpl implements RDFResource
{
    public static final String SERVICE_URI = "http://dolph.heltnormalt.dk:82/local/query";
    
    private com.hp.hpl.jena.rdf.model.Model model = null;
    private com.hp.hpl.jena.rdf.model.Resource resource = null;

    public RDFResourceImpl()
    {
	Query query = QueryFactory.create();
	QueryExecution qex = QueryExecutionFactory.sparqlService(getServiceURI(), query);
	model = qex.execDescribe();

	resource = model.createResource(getURI());
    }

    public final String getServiceURI()
    {
	return SERVICE_URI;
    }

    @Override
    public boolean exists()
    {
	return getModel().containsResource(this);
    }
    
    @Override
    public Model getModel()
    {
	return resource.getModel();
    }
    
    @Override
    public AnonId getId()
    {
	return resource.getId();
    }

    @Override
    public Resource inModel(Model m)
    {
	return resource.inModel(m);
    }

    @Override
    public boolean hasURI(String uri)
    {
	return resource.hasURI(uri);
    }

    @Override
    public String getNameSpace()
    {
	return resource.getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return resource.getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property p)
    {
	return resource.getRequiredProperty(p);
    }

    @Override
    public Statement getProperty(Property p)
    {
	return resource.getProperty(p);
    }

    @Override
    public StmtIterator listProperties(Property p)
    {
	return resource.listProperties(p);
    }

    @Override
    public StmtIterator listProperties()
    {
	return resource.listProperties();
    }

    @Override
    public Resource addLiteral(Property p, boolean o)
    {
	return resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, long o)
    {
	return resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, char o)
    {
	return resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property value, double d)
    {
	return resource.addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property value, float d)
    {
	return resource.addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property p, Object o)
    {
	return resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, Literal o)
    {
	return resource.addLiteral(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o)
    {
	return resource.addProperty(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o, String l)
    {
	return resource.addProperty(p, o, l);
    }

    @Override
    public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype)
    {
	return resource.addProperty(p, lexicalForm, datatype);
    }

    @Override
    public Resource addProperty(Property p, RDFNode o)
    {
	return resource.addProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p)
    {
	return resource.hasProperty(p);
    }

    @Override
    public boolean hasLiteral(Property p, boolean o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, long o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, char o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, double o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, float o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, Object o)
    {
	return resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o)
    {
	return resource.hasProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o, String l)
    {
	return resource.hasProperty(p, o, l);
    }

    @Override
    public boolean hasProperty(Property p, RDFNode o)
    {
	return resource.hasProperty(p, o);
    }

    @Override
    public Resource removeProperties()
    {
	return resource.removeProperties();
    }

    @Override
    public Resource removeAll(Property p)
    {
	return resource.removeAll(p);
    }

    @Override
    public Resource begin()
    {
	return resource.begin();
    }

    @Override
    public Resource abort()
    {
	return resource.abort();
    }

    @Override
    public Resource commit()
    {
	return resource.commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property p)
    {
	return resource.getPropertyResourceValue(p);
    }

    @Override
    public boolean isAnon()
    {
	return resource.isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return resource.isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return resource.isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return resource.isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> view)
    {
	return resource.as(view);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> view)
    {
	return resource.canAs(view);
    }

    @Override
    public Object visitWith(RDFVisitor rv)
    {
	return resource.visitWith(rv);
    }

    @Override
    public Resource asResource()
    {
	return resource.asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return resource.asLiteral();
    }

    @Override
    public Node asNode()
    {
	return resource.asNode();
    }

}
