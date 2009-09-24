/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportCreateView extends FrontEndView
{

    public ReportCreateView(ReportListResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportCreateView.xsl")));

	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");

	String visTypes = QueryXMLResult.query(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/visualization-types.rq")));

	if (request.getAttribute("query-result") != null)
	{
	    getResolver().setArgument("results", (String)request.getAttribute("query-results"));
	    getTransformer().setParameter("query-result", true);
	    getTransformer().setParameter("query-string", request.getParameter("query-string"));
	}

	getResolver().setArgument("visualization-types", visTypes);
	    
	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);    
    }
}
