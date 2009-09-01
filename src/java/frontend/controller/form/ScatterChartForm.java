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
public class ScatterChartForm extends Form
{
    private String xBinding = null;
    private String[] yBindings = null;
    
    public ScatterChartForm(HttpServletRequest request)
    {
	super(request);
	xBinding = request.getParameter("x-binding");
	yBindings = request.getParameterValues("y-binding");
    }

    public String getXBinding()
    {
	return xBinding;
    }

    public String[] getYBinding()
    {
	return yBindings;
    }
    
    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
