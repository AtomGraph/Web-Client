/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.RDFResourceImpl;

/**
 *
 * @author Pumba
 */
@Path("/{path}")
public class Resource extends RDFResourceImpl
{
    private Response response = null;
    //@QueryParam("uri") String uri; // does not inject?
    
    @GET
    @Produces("text/html")
    public Response getResponse() throws TransformerConfigurationException
    {
	//Logger.getLogger(Resource.class.getName()).debug("QueryParam('uri'): {uri}");
System.out.println("@GET");
	//if (response == null) response = new View(this);
	//return response;
	return new View(this);
    }
    
    @Override
    public String getURI()
    {
	if (getUriInfo().getQueryParameters().getFirst("uri") != null)
	    return getUriInfo().getQueryParameters().getFirst("uri");
	
	return getUriInfo().getAbsolutePathBuilder().
		host("local.heltnormalt.dk").
		port(-1).
		replacePath("striben").
		build().toString();
    }
}
