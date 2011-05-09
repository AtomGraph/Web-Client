/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.visualization;

import dk.semantic_web.diy.view.View;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Pumba
 */
public class VisualizationEmbedResource extends FrontEndResource
{

    private static final String RELATIVE_URI = "embed";
    private static final VisualizationEmbedResource INSTANCE = new VisualizationEmbedResource(VisualizationListResource.getInstance());

    private VisualizationEmbedResource(VisualizationListResource parent)
    {
	super(parent);
    }

    public static VisualizationEmbedResource getInstance()
    {
	return INSTANCE;
    }

    @Override
    public String getPath()
    {
	try
	{
	    return URLEncoder.encode(RELATIVE_URI, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(VisualizationEmbedResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	response.getOutputStream().println(request.getParameter("query"));
	return super.doGet(request, response);
    }

}
