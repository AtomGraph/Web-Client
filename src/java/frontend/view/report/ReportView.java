/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportResource;
import frontend.view.FrontEndView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import view.QueryStringBuilder;
import view.QueryXMLResult;

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
    public ReportResource getResource()
    {
	return (ReportResource)super.getResource();
    }
    
    protected void setQueryResult(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String queryString = getResource().getReport().getQuery().getQueryString();
if (queryString == null) queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/citiesByPopulation.rq")); // QUIRK

	String results = QueryXMLResult.queryRemote("http://dbpedia.org/sparql", queryString);

	//setDocument(results);

	getTransformer().setParameter("query-string", queryString);
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String report = QueryXMLResult.queryRemote("http://api.talis.com/stores/mjusevicius-dev1/services/sparql", QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/report.rq"), "http://localhost:8080/semantic-reports/reports/qqqqqqqqqqqqqqqq")); // getResource().getAbsoluteURI()
	    setDocument(report);
	    
	    getResolver().setArgument("report", report);	    
	} catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
}
