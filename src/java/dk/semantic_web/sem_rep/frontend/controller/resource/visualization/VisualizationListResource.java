/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.visualization;

import com.sun.jersey.spi.resource.Singleton;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.frontend.controller.resource.FrontPageResource;
import java.net.URI;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Pumba
 */

@Singleton
@Path(VisualizationListResource.PATH)
public class VisualizationListResource extends FrontEndResource
{
    public static final String PATH = "visualizations";
    //private static final VisualizationListResource INSTANCE = new VisualizationListResource(FrontPageResource.getInstance());
    public static final UriBuilder URI_BUILDER = FrontPageResource.URI_BUILDER.clone().path(PATH);

    private VisualizationListResource(FrontPageResource parent, @Context UriInfo uriInfo)
    {
	super(parent, uriInfo);
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
