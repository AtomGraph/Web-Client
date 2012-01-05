/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.analytics.form;

import org.graphity.Form;
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
    String endpoint = null;
    String[] visualizations = null;
    
    public ReportForm(HttpServletRequest request)
    {
	super(request);
	title = request.getParameter("title");
	queryString = request.getParameter("query-string");
	endpoint = request.getParameter("endpoint");
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

    public String getEndpoint()
    {
	return endpoint;
    }

    public String[] getVisualizations()
    {
	return visualizations;
    }

    @Override
    public List<Exception> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
