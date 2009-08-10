/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ReportReadView extends FrontEndView
{

    public ReportReadView(ReportResource resource)
    {
	super(resource);
    }

    @Override
    public ReportResource getResource()
    {
	return (ReportResource)super.getResource();
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportReadView.xsl")));

	setReport(request, response);
	setQueryResult(request, response);
	
	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private void setQueryResult(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String queryString = getResource().getReport().getQuery().getQueryString();
if (queryString == null) queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/citiesByPopulation.rq")); // QUIRK

	String results = QueryXMLResult.queryRemote("http://dbpedia.org/sparql", queryString);

	//setDocument(results);

	getTransformer().setParameter("query-string", queryString);
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }

    private void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String report = QueryXMLResult.queryRemote("http://api.talis.com/stores/mjusevicius-dev1/services/sparql", QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/report.rq"), getResource().getAbsoluteURI()));
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
