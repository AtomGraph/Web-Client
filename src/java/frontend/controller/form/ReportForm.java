/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Form;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pumba
 */
public class ReportForm extends Form
{
    String title = null;
    String queryString = null;
    String[] visualizations = null;
    
    public ReportForm(HttpServletRequest request)
    {
	super(request);
	title = request.getParameter("title");
	queryString = request.getParameter("query-string");
	visualizations = request.getParameterValues("visualization");
    }

    public String getTitle()
    {
	return title;
    }

    public String getQueryString()
    {
	return queryString;
    }

    public String[] getVisualizations()
    {
	return visualizations;
    }

    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
