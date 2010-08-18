/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.endpoint;

import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.resource.FrontPageResource;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.endpoint.EndpointListView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

/**
 *
 * @author Pumba
 */
public class EndpointListResource extends FrontEndResource implements Singleton
{
    private static final String RELATIVE_URI = "endpoints";
    private static final EndpointListResource INSTANCE = new EndpointListResource(FrontPageResource.getInstance());

    public EndpointListResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static EndpointListResource getInstance()
    {
	return INSTANCE;
    }

    @Override
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(RELATIVE_URI, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

        return new EndpointListView(this);
    }

}
