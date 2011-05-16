/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.visualization;

import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.sun.jersey.spi.resource.PerRequest;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.model.Namespaces;
import java.net.URI;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Pumba
 */

@PerRequest
public class VisualizationResource extends FrontEndResource
{
    public static final UriBuilder URI_BUILDER = VisualizationListResource.URI_BUILDER.clone().path("{visualization}");
    //private static final VisualizationResource INSTANCE = new VisualizationResource(VisualizationListResource.getInstance());

    private VisualizationResource(VisualizationListResource parent, @Context UriInfo uriInfo)
    {
	super(parent, uriInfo);
    }

    @Override
    @GET
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	response.getOutputStream().println(request.getParameter("query"));
	return super.doGet(request, response);
    }

    @Override
    public String getPath() {
	return getOntologyLabel(getTopicOntology());
    }

    public Ontology getTopicOntology() {
	return getTopicResource().as(Ontology.class);
    }

    public static String getOntologyLabel(Ontology ontology)
    {	// ontology.getLocalName() ???
	Property prefixProp = ontology.getModel().createProperty(Namespaces.VANN_NS, "preferredNamespacePrefix");
	if (ontology.getPropertyValue(prefixProp) != null && !ontology.getPropertyValue(prefixProp).toString().isEmpty()) return ontology.getPropertyValue(prefixProp).asLiteral().getString();
	if (ontology.getLabel(null) != null && !ontology.getLabel(null).isEmpty()) return ontology.getLabel(null);
	if (ontology.getPropertyValue(DC.title) != null && !ontology.getPropertyValue(DC.title).asLiteral().getString().isEmpty()) return ontology.getPropertyValue(DC.title).asLiteral().getString();
	if (ontology.getPropertyValue(DCTerms.title) != null && !ontology.getPropertyValue(DCTerms.title).asLiteral().getString().isEmpty()) return ontology.getPropertyValue(DCTerms.title).asLiteral().getString();
	return String.valueOf(UUID.randomUUID()); // should be saved w/ dc:identifier???
    }

    @Override
    public UriBuilder getUriBuilder() {
	return URI_BUILDER;
    }

    public static URI buildURI(Ontology ontology) {
        return URI_BUILDER.build(getOntologyLabel(ontology));
    }

}
