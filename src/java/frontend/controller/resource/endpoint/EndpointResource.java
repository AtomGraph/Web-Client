/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.endpoint;

import controller.LeafResource;
import frontend.controller.FrontEndResource;
import model.Endpoint;

/**
 *
 * @author Pumba
 */
public class EndpointResource extends FrontEndResource implements LeafResource
{

    public EndpointResource(Endpoint endpoint, EndpointListResource parent)
    {
	super(parent);
    }

    @Override
    public String getRelativeURI()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
