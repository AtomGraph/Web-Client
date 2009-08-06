/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
	super.display(request, response);
    }

}
