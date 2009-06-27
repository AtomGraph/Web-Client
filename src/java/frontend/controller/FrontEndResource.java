/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller;

import dk.semantic_web.diy.controller.ResourceImpl;

/**
 *
 * @author Pumba
 */
abstract public class FrontEndResource extends ResourceImpl
{

    public FrontEndResource(FrontEndResource parent)
    {
	super(parent);
    }

    public String getURI()
    {
	String uri = getRelativeURI();
	//if (!(this instanceof LeafResource || uri.equals(""))) uri += "/";
	if (!uri.equals("")) uri += "/";
	if (hasParent()) return getParent().getURI() + uri;
	else return uri;
    }
}
