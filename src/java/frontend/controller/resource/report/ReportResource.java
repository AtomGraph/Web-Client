/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import frontend.controller.FrontEndResource;
import frontend.view.report.ReportView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource
{
    private View view = null;
    
    public ReportResource(FrontEndResource parent)
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
	else view = new ReportView(this);

	return view;
    }
}
