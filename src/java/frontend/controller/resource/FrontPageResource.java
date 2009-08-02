/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource;

import dk.semantic_web.diy.controller.Singleton;
import frontend.controller.FrontEndResource;

/**
 *
 * @author Pumba
 */
public class FrontPageResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";    
    private static final FrontPageResource INSTANCE = new FrontPageResource(null);

    private FrontPageResource(FrontEndResource parent)
    {
	super(parent);
    }
    
    public String getRelativeURI()
    {
	return RELATIVE_URI;
    }

    public static FrontPageResource getInstance()
    {
	return INSTANCE;
    }
}
