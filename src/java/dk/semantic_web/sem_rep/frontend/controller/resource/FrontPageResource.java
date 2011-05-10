/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource;

import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
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
    private static final String PATH = "";
    private static final FrontPageResource INSTANCE = new FrontPageResource(null);

    private FrontPageResource(FrontEndResource parent)
    {
	super(parent);
    }
    
    @Override
    public String getPath()
    {
	try
	{
	    return URLEncoder.encode(PATH, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(FrontPageResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return PATH;
    }

    public static FrontPageResource getInstance()
    {
	return INSTANCE;
    }
}
