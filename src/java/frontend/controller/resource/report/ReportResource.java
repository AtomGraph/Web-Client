/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import dk.semantic_web.diy.controller.Resource;
import dk.semantic_web.diy.controller.Singleton;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.resource.FrontPageResource;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";
    private static final ReportResource INSTANCE = new ReportResource(FrontPageResource.getInstance());
    
    private View view = null;
    
    public ReportResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static Resource getInstance()
    {
	return INSTANCE;
    }
    
    public String getRelativeURI()
    {
	return RELATIVE_URI;
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
