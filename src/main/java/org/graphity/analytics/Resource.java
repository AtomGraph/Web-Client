/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.graphity.RDFResourceImpl;

/**
 *
 * @author Pumba
 */
@Path("/{path}")
public class Resource extends RDFResourceImpl
{
    @GET
    @Produces("text/html")
    public Response html()
    {
	View view = new View(this);
	return view.build();
    }
}
