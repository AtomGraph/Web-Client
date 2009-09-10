/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource;

import dk.semantic_web.diy.controller.Singleton;
import frontend.controller.FrontEndResource;
import frontend.controller.resource.report.ReportListResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pumba
 */
public class FrontPageResource extends FrontEndResource implements Singleton
{
    private static final String RELATIVE_URI = "";    
    private static final FrontPageResource INSTANCE = new FrontPageResource(null);

    private FrontPageResource(FrontEndResource parent)
    {
	super(parent);
    }
    
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(RELATIVE_URI, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(FrontPageResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

    public static FrontPageResource getInstance()
    {
	return INSTANCE;
    }
}
