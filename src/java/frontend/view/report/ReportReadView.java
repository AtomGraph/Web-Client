/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportResource;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{

    public ReportReadView(ReportResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportReadView.xsl")));

	setReport(request, response);
	setQueryResult(request, response);
	setVisualizations(request, response);
	
	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }
}
