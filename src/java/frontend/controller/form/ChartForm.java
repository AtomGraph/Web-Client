/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lt.xml.diy.controller.Error;
import lt.xml.diy.controller.Form;

/**
 *
 * @author Pumba
 */
public class ChartForm extends Form
{
    // chart types: http://code.google.com/apis/chart/types.html
    public final static String TYPE_SCATTER = "s";
    
    private String queryString = null;
    private String xAxis = null;
    private String yAxis = null;
    private String label = null;
    private String type = null;
    
    public ChartForm(HttpServletRequest request)
    {
	super(request);
	queryString = request.getParameter("query-string");
	xAxis = request.getParameter("x-axis");
	yAxis = request.getParameter("y-axis");
	label = request.getParameter("label");
	type = request.getParameter("type");
    }

    public String getQueryString()
    {
	return queryString;
    }

    public String getXAxis()
    {
	return xAxis;
    }

    public String getYAxis()
    {
	return yAxis;
    }
    
    public String getType()
    {
	return type;
    }

    public String getLabel()
    {
	return label;
    }
    
    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
