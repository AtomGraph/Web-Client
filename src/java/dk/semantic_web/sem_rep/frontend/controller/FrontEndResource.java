/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller;

import dk.semantic_web.sem_rep.controller.LeafResource;
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

    @Override
    public String getAbsolutePath()
    {
	String uri = getPath();
	if (!(this instanceof LeafResource || uri.equals(""))) uri += "/";
	if (hasParent()) return getParent().getAbsolutePath() + uri;
	else return uri;
    }

    @Override
    public final String getURI() {
	return null;
    }
}
