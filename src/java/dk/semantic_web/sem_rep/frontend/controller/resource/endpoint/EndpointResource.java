/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.endpoint;

import dk.semantic_web.sem_rep.controller.LeafResource;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import dk.semantic_web.sem_rep.frontend.view.endpoint.EndpointReadView;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.model.Endpoint;

/**
 *
 * @author Pumba
 */
public class EndpointResource extends FrontEndResource implements LeafResource
{
    private Endpoint endpoint = null;

    public EndpointResource(Endpoint endpoint, EndpointListResource parent)
    {
	super(parent);
	setEndpoint(endpoint);
    }

    public Endpoint getEndpoint()
    {
	return endpoint;
    }

    public void setEndpoint(Endpoint endpoint)
    {
	this.endpoint = endpoint;
    }

    @Override
    public String getPath()
    {
	try
	{
	    return URLEncoder.encode(getEndpoint().getURI().toString(), "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(EndpointResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return getEndpoint().getURI().toString();
    }

   @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, TransformerConfigurationException, URISyntaxException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

	return new EndpointReadView(this);
    }

}
