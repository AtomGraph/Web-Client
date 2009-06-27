/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Form;

/**
 *
 * @author Pumba
 */
public class ChartForm extends Form
{
    // chart types: http://code.google.com/apis/chart/types.html
    public final static String TYPE_SCATTER = "s";
    
    private String queryString = null;
    private String xVariable = null;
    private String yVariable = null;
    private String labelVariable = null;
    private String type = null;
    private String title = null;
    
    public ChartForm(HttpServletRequest request)
    {
	super(request);
	queryString = request.getParameter("query-string");
	xVariable = request.getParameter("x-variable");
	yVariable = request.getParameter("y-variable");
	labelVariable = request.getParameter("label-variable");
	type = request.getParameter("type");
	title = request.getParameter("title");
    }

    public String getQueryString()
    {
	return queryString;
    }

    public String getXAxisVariable()
    {
	return xVariable;
    }

    public String getYAxisVariable()
    {
	return yVariable;
    }
    
    public String getLabelVariable()
    {
	return labelVariable;
    }
    
    public String getType()
    {
	return type;
    }

    public String getTitle()
    {
	return title;
    }
    
    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
