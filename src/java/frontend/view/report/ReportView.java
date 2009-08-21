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
import model.Query;
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
	Query query = getResource().getReport().getQuery();
	String queryString = query.getQueryString();
//if (queryString == null) queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/citiesByPopulation.rq")); // QUIRK

	//String results = QueryXMLResult.queryRemote("http://dbpedia.org/sparql", queryString);
	String results = QueryXMLResult.queryRemote(query.getEndpoint().toString(), queryString);

	//setDocument(results);

	getTransformer().setParameter("query-string", queryString);
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
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
    
    protected void setVisualizations(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String visualizations = QueryXMLResult.queryRemote("http://api.talis.com/stores/mjusevicius-dev1/services/sparql", QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/visualizations.rq"), getResource().getAbsoluteURI()));
	    
	    getResolver().setArgument("visualizations", visualizations);
	} catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
}
