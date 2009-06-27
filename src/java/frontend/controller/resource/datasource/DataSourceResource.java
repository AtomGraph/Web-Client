/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.datasource;

import frontend.controller.FrontEndResource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lt.xml.diy.view.View;

/**
 *
 * @author Pumba
 */
public class DataSourceResource
{
    private View view = null;
    
    public DataSourceResource(FrontEndResource parent)
    {
	super(parent);
    }

    public String getRelativeURI()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else view = new DataSourceView(this);

	return view;
    }
}
