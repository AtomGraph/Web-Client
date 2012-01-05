/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.analytics.form;

import dk.semantic_web.diy.controller.Error;
import org.graphity.Form;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pumba
 */
public class EmbedForm extends Form
{
    String queryString = null;
    String endpointUri = null;
    String visualizationUri = null;

    public EmbedForm(HttpServletRequest request)
    {
	super(request);
	queryString = request.getParameter("query");
	endpointUri = request.getParameter("endpoint");
	visualizationUri = request.getParameter("visualization");
    }

    public String getEndpointUri()
    {
	return endpointUri;
    }

    public String getQueryString()
    {
	return queryString;
    }

    public String getVisualizationUri()
    {
	return visualizationUri;
    }

    @Override
    public List<Error> validate()
    {
	List<Error> errors = new ArrayList<Error>();

	if (getEndpointUri() == null) errors.add(new Error("noEndpoint"));
	if (getQueryString() == null) errors.add(new Error("noQueryString"));
	if (getVisualizationUri() == null) errors.add(new Error("noVisualization"));

	return errors;
    }

}
