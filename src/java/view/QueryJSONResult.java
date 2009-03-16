/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
/**
 *
 * @author Rokelis
 */
public class QueryJSONResult {
    public static String query(Model model, String queryString) throws IOException
    {
        //System.out.println("Query: " + queryString);
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
	//model.enterCriticalSection(Lock.READ);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();        
	//model.leaveCriticalSection();
        //ResultSetFormatter.outputAsXML(response.getOutputStream(), results);
        OutputStream jsonout = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(jsonout, results);
        
        return jsonout.toString();
    }

}
