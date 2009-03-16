/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.chart;

import frontend.controller.FrontEndResource;
import frontend.view.chart.ChartView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lt.xml.diy.view.View;

/**
 *
 * @author Pumba
 */
public class ChartResource extends FrontEndResource
{
    private View view = null;
    
    public ChartResource(FrontEndResource parent)
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
	else view = new ChartView(this);

	return view;
    }
}
