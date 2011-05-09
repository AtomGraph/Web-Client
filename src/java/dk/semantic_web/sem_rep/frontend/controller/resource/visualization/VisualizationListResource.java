/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.visualization;

import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.FrontPageResource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pumba
 */
public class VisualizationListResource extends FrontEndResource implements Singleton
{
    private static final String RELATIVE_URI = "visualizations";
    private static final VisualizationListResource INSTANCE = new VisualizationListResource(FrontPageResource.getInstance());

    private VisualizationListResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static VisualizationListResource getInstance()
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
	    Logger.getLogger(VisualizationListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

}
