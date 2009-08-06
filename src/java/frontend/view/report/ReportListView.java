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
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportListView extends FrontEndView
{

    public ReportListView(ReportListResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportListView.xsl")));
	
	String queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/reports.rq"));		
	String results = QueryXMLResult.queryRemote("http://api.talis.com/stores/mjusevicius-dev1/services/sparql", queryString);

	setDocument(results);
	
	getResolver().setArgument("reports", results);
	
	super.display(request, response);
    }

}
