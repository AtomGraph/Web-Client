/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.endpoint;

import com.hp.hpl.jena.ontology.Individual;
import com.sun.jersey.spi.resource.PerRequest;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.frontend.controller.resource.instance.InstanceResource;
import dk.semantic_web.sem_rep.frontend.view.endpoint.EndpointReadView;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.model.Endpoint;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Pumba
 */

@PerRequest
public class EndpointResource extends FrontEndResource
{
    public static final UriBuilder URI_BUILDER = EndpointListResource.URI_BUILDER.clone().path("{endpoint}");

    private Endpoint endpoint = null;

    public EndpointResource(EndpointListResource parent, @Context UriInfo uriInfo) // Endpoint endpoint
    {
	super(parent, uriInfo);
	//setEndpoint(endpoint);
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
    public String getPath() {
	return InstanceResource.getIndividualPath(getTopicEndpoint());
    }

    public Individual getTopicEndpoint() {
	return getTopicResource().as(Individual.class);
    }

    @Override
    @GET
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, TransformerConfigurationException, URISyntaxException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

	return new EndpointReadView(this);
    }

    @Override
    public UriBuilder getUriBuilder()
    {
	return URI_BUILDER;
    }

}
