/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.endpoint;

import frontend.controller.FrontEndResource;

/**
 *
 * @author Pumba
 */
public class EndpointResource extends FrontEndResource
{

    public EndpointResource(EndpointListResource parent)
    {
	super(parent);
    }

    @Override
    public String getRelativeURI()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
