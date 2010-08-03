/*
 * QueryXMLResult.java
 *
 * Created on Tre�iadienis, 2007, Vasario 14, 23.23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package view;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Static helper class, used to select a model using a SPARQL select string.
 * @author Pumba
 */
public class QueryResult
{
    public static final String DEFAULT_PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";

    /** Queries model using SPARQL select string
     @param model Model to be queried
     @param queryString SPARQL select string (formatted beforehand)
     */
    public static ResultSetRewindable select(Model model, String queryString) throws IOException, QueryException
    {
	//String resultString = null;
        ResultSetRewindable resultSet = null;
        System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	//model.enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
	try
	{
	    resultSet = ResultSetFactory.makeRewindable(qe.execSelect());
	    //resultString = ResultSetFormatter.asXMLString(resultSet);
	}
	finally
	{
	    qe.close();
	}
        return resultSet;
    }

    /*
    public static ResultSet select(Dataset dataset, String queryString) throws IOException, QueryException
    {
	//String resultString = null;
        ResultSet resultSet = null;
        System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.create(query, dataset);
	try
	{
	    resultSet = qe.execSelect();
	    //resultString = ResultSetFormatter.asXMLString(resultSet);
	}
	finally
	{
	    qe.close();
	}
        return resultSet;
    }
    */

    public static ResultSetRewindable select(Dataset dataset, String queryString) throws IOException, QueryException
    {
	//String resultString = null;
        ResultSetRewindable resultSet = null;
        System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.create(query, dataset);
	try
	{
	    resultSet = ResultSetFactory.makeRewindable(qe.execSelect());
	    //resultString = ResultSetFormatter.asXMLString(resultSet);
	}
	finally
	{
	    qe.close();
	}
        return resultSet;
    }

    public static String query(ResultSet results)
    {
       // System.out.println("Query: " + queryString);
        return ResultSetFormatter.asXMLString(results);
    }
    
    public static ResultSetRewindable selectRemote(String endpointUri, String queryString, long resultLimit) throws IOException, QueryException
    {
	//String resultString = null;
        ResultSetRewindable resultSet = null;
        System.out.println("Endpoint: " + endpointUri);
        System.out.println("Query: " + queryString);

	//queryString = DEFAULT_PREFIXES + queryString;
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	query.setLimit(resultLimit);
	
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointUri, query);
	try
	{
	    resultSet = ResultSetFactory.makeRewindable(qe.execSelect());
            //resultSet = qe.execSelect();
	    //resultString = ResultSetFormatter.asXMLString(resultSet);
            //ResultSetFormatter.consume(resultSet);
	}
	finally
	{
	    qe.close();
	}
        return resultSet;
    }
    
    public static String describe(Model model, String queryString)
    {
        //System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	//model.enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        Model results = qe.execDescribe();        

	String rdfXml = null;
	StringWriter sw = new StringWriter();
	RDFWriter writer = results.getWriter("RDF/XML-ABBREV");
	// writer.setProperty("xmlbase", base);
	writer.write(results, sw, null);
	rdfXml = sw.getBuffer().toString();
	//model.leaveCriticalSection();
        return rdfXml;
    }
}
