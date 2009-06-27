/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import frontend.controller.form.ChartForm;
import frontend.controller.resource.report.ReportResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import view.QueryResultChart;
import view.QueryStringBuilder;
import view.QueryXMLResult;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportView extends FrontEndView
{

    public ReportView(ReportResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	ChartForm form = new ChartForm(request);
	
	setStyleSheet(new File(getServlet().getServletConfig().getServletContext().getRealPath("/xslt/chart/ChartView.xsl")));

	String queryString = null;
	if (request.getParameter("query-string") != null) queryString = request.getParameter("query-string");
	else queryString = QueryStringBuilder.build(getServlet().getServletConfig().getServletContext().getRealPath("/sparql/companiesByRevenue.rq"));
	
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        ResultSet resultSet = qe.execSelect();
	Iterator<QuerySolution> resultIterator = qe.execSelect();
	//qe.close();

	List variables = resultSet.getResultVars();
	
	String xVariable = (String)variables.get(2); // QUIRK
	if (form.getXAxisVariable() != null) xVariable = form.getXAxisVariable();
	String yVariable = (String)variables.get(3); // QUIRK
	if (form.getYAxisVariable() != null) yVariable = form.getYAxisVariable();
	String labelVariable = "label"; // QUIRK
	if (form.getLabelVariable() != null) labelVariable = form.getLabelVariable();

	String results = QueryXMLResult.query(resultSet);
	String chartUrl = QueryResultChart.getURL(resultIterator, labelVariable, xVariable, yVariable, form.getTitle());
	
	setDocument(results);

	getTransformer().setParameter("query-string", queryString);
	getTransformer().setParameter("query-result", true);
	getTransformer().setParameter("chart-url", chartUrl);
	getTransformer().setParameter("x-variable-default", xVariable);
	getTransformer().setParameter("y-variable-default", yVariable);
	getTransformer().setParameter("label-variable-default", labelVariable);

	if (form.getType() != null) getTransformer().setParameter("chart-result", true); // QUIRK
	
	getResolver().setArgument("results", results);
	getResolver().setArgument("chart-form", XMLSerializer.serialize(form));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);	    
    }
}
