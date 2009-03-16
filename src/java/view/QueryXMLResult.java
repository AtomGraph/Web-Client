/*
 * QueryXMLResult.java
 *
 * Created on Tre�iadienis, 2007, Vasario 14, 23.23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package view;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Static helper class, used to query a model using a SPARQL query string.
 * @author Pumba
 */
public class QueryXMLResult
{
    
    /** Queries model using SPARQL query string
     @param model Model to be queried
     @param queryString SPARQL query string (formatted beforehand)
     */
    public static String query(Model model, String queryString) throws IOException
    {
        System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	//model.enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
	qe.close();
	//model.leaveCriticalSection();
        //ResultSetFormatter.outputAsXML(response.getOutputStream(), results);
        return ResultSetFormatter.asXMLString(results);
    }

    public static String query(ResultSet results)
    {
       // System.out.println("Query: " + queryString);
        return ResultSetFormatter.asXMLString(results);
    }
    public static String queryRemote(String endpointUri, String queryString) throws IOException
    {
        System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpointUri, query);
        ResultSet results = qe.execSelect();
	qe.close();
	//model.leaveCriticalSection();
        //ResultSetFormatter.outputAsXML(response.getOutputStream(), results);
        return ResultSetFormatter.asXMLString(results);
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
