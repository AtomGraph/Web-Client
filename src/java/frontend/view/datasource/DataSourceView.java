/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.datasource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import frontend.controller.resource.datasource.DataSourceResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class DataSourceView extends FrontEndView
{

        public DataSourceView(DataSourceResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getServlet().getServletConfig().getServletContext().getRealPath("/xslt/sparql2google-wire.xsl")));

	String queryString = null;
	if (request.getParameter("sparql-result") != null) queryString = request.getParameter("query-string");
	else queryString = QueryStringBuilder.build(getServlet().getServletConfig().getServletContext().getRealPath("/sparql/companiesByRevenue.rq"));
	
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        ResultSet resultSet = qe.execSelect();
	//qe.close();

	String results = QueryXMLResult.query(resultSet);
	
	setDocument(results);

	//getTransformer().setParameter("query-string", queryString);

	getResolver().setArgument("results", results);

	super.display(request, response);
	
	response.setStatus(HttpServletResponse.SC_OK);	
    }
}
