/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import dk.semantic_web.diy.controller.Singleton;
import frontend.controller.FrontEndResource;
import frontend.controller.resource.FrontPageResource;

/**
 *
 * @author Pumba
 */
public class ReportListResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";
    private static final ReportListResource INSTANCE = new ReportListResource(FrontPageResource.getInstance());

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

}
