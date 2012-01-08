/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import com.hp.hpl.jena.rdf.model.Model;
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
    @GET
    @Produces("text/html")
    public Response html() throws TransformerConfigurationException
    {
	View view = new View(this);
	return view.build();
	
	//return getModel();
    }
    
    @Override
    public String getURI()
    {
	return getUriInfo().getAbsolutePathBuilder().
		host("local.heltnormalt.dk").
		port(-1).
		replacePath("striben").
		build().toString();
    }
}
