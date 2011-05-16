/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.endpoint;

import com.sun.jersey.spi.resource.Singleton;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.frontend.controller.resource.FrontPageResource;
import dk.semantic_web.rdf_editor.model.Model;
import dk.semantic_web.sem_rep.frontend.view.endpoint.EndpointListView;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerConfigurationException;

/**
 *
 * @author Pumba
 */

@Singleton
@Path(EndpointListResource.PATH)
public class EndpointListResource extends FrontEndResource
{
    public static final String PATH = "endpoints";
    public static final UriBuilder URI_BUILDER = FrontPageResource.URI_BUILDER.clone().path(PATH);
    //private static final EndpointListResource INSTANCE = new EndpointListResource(FrontPageResource.getInstance());

    public EndpointListResource(FrontPageResource parent, @Context UriInfo uriInfo)
    {
	super(parent, uriInfo);
    }

    @Path("{id}")
    public EndpointResource getEndpointResource(@Context UriInfo uriInfo) {
	EndpointResource resource = null;
	if (Model.getInstance().getSystemOnt().getIndividual(uriInfo.getAbsolutePath().toString()) != null)
	    resource = new EndpointResource(this, uriInfo);
	if (resource == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	return resource;
    }

    @Override
    @GET
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

        return new EndpointListView(this);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public String getAbsolutePath()
    {
	return getPath();
    }

    @Override
    public URI getRealURI()
    {
	return URI_BUILDER.build();
    }

    @Override
    public UriBuilder getUriBuilder() {
	return URI_BUILDER;
    }

}
