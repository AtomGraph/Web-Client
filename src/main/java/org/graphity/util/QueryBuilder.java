/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Pumba
 */
public class QueryBuilder
{
    private Model model = ModelFactory.createDefaultModel();
    private org.topbraid.spin.model.Query spinQuery = null;

    protected static QueryBuilder newInstance()
    {
	return new QueryBuilder();
    }
    
    public static QueryBuilder fromQuery(Query query, String uri)
    {
	return newInstance().query(query, uri);
    }

    public static QueryBuilder fromQueryString(String queryString)
    {
	return newInstance().query(QueryFactory.create(queryString), null);
    }

    public static QueryBuilder fromModel(Model model)
    {
	return newInstance().query(model);
    }

    protected QueryBuilder query(Model model)
    {
	this.model = model;
	Resource queryRes = model.listResourcesWithProperty(RDF.type, SP.Construct).nextResource();
		
	spinQuery = SPINFactory.asQuery(queryRes);
	
	return this;
    }
    
    protected QueryBuilder query(Query query, String uri)
    {
	ARQ2SPIN arq2Spin = new ARQ2SPIN(model);
	spinQuery = arq2Spin.createQuery(query, uri);
	
	return this;
    }
    
    public QueryBuilder bind(String name, String value)
    {
	//if (value.isURIResource())
	{
	    Resource var = getVariableByName("uri");
	    if (var != null)
	    {
		var.removeAll(SP.varName);
		ResourceUtils.renameResource(var, value);
	    }
	}
	
	return this;
    }
    
    protected Resource getVariableByName(String name)
    {
	ResIterator it = model.listResourcesWithProperty(SP.varName, name);
	if (it.hasNext()) return it.nextResource();
	else return null;
    }

    public Query build()
    {
//System.out.println("SPIN query in Turtle:");
//model.write(System.out, FileUtils.langTurtle);
System.out.println(spinQuery.toString());

	return ARQFactory.get().createQuery(spinQuery);
    }

}
