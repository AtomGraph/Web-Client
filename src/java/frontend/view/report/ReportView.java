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
import model.SDB;
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
        String endpointUri = getResource().getReport().getQuery().getEndpoint().toString();
        String queryString = getResource().getReport().getQuery().getQueryString();
        String results = QueryXMLResult.selectRemote(endpointUri, queryString);

	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String report = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/report.rq"), getResource().getAbsoluteURI()));
	    setDocument(report);
	    
	    getResolver().setArgument("report", report);
	} catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    protected void setEndpoints(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String endpoints = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/endpoints.rq")));

	getResolver().setArgument("endpoints", endpoints);
    }

    protected void setVisualizations(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String visualizations = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI()));
	    
	    getResolver().setArgument("visualizations", visualizations);
        } catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    protected void setBindings(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String bindings = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("bindings", bindings);
    }

    protected void setVariables(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
	    String variables = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/variables.rq"), getResource().getAbsoluteURI()));

	    getResolver().setArgument("variables", variables);
        } catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

}
