/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.resource.FrontPageResource;
import frontend.view.report.ReportListView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Pumba
 */
public class ReportListResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";
    private static final ReportListResource INSTANCE = new ReportListResource(FrontPageResource.getInstance());
    private View view = null;
    
    private ReportListResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static ReportListResource getInstance()
    {
	return INSTANCE;
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
	else view = new ReportListView(this);

	return view;
    }
}
