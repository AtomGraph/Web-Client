/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.endpoint;

import com.hp.hpl.jena.query.ResultSetRewindable;
import dk.semantic_web.rdf_editor.frontend.view.FrontEndView;
import dk.semantic_web.rdf_editor.view.QueryResult;
import dk.semantic_web.rdf_editor.view.QueryStringBuilder;
import dk.semantic_web.rdf_editor.view.XMLSerializer;
import dk.semantic_web.sem_rep.frontend.controller.resource.endpoint.EndpointResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
public class EndpointReadView extends FrontEndView
{

    public EndpointReadView(EndpointResource resource) throws MalformedURLException, TransformerConfigurationException, URISyntaxException
    {
	super(resource);
    }

    @Override
    protected String getStyleSheetPath() {
        return XSLT_BASE + "endpoint/" + getClass().getSimpleName() + ".xsl";
    }

    @Override
    public EndpointResource getResource()
    {
	return (EndpointResource)super.getResource();
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setEndpoint(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/read/endpoint.rq"), getResource().getEndpoint().getURI())));

	String queryString = QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/read/reports.rq"), getResource().getEndpoint().getURI().toString(), "dateCreated", 0, 20); // QUIRK!!!
	setReports(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), queryString));

	super.display(request, response);
    }

    private void setEndpoint(ResultSetRewindable endpoint)
    {
	setDocument(XMLSerializer.serialize(endpoint));
    }

    protected void setReports(ResultSetRewindable reports)
    {
	getResolver().setArgument("reports", XMLSerializer.serialize(reports));
    }

}
