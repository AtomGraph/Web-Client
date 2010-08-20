/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.endpoint;

import frontend.controller.resource.endpoint.EndpointResource;
import frontend.view.FrontEndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Pumba
 */
public class EndpointReadView extends FrontEndView
{

    public EndpointReadView(EndpointResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response)
    {

    }
}
