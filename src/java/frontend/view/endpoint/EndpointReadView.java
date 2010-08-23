/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.endpoint;

import com.hp.hpl.jena.query.ResultSetRewindable;
import frontend.controller.resource.endpoint.EndpointResource;
import frontend.view.FrontEndView;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryResult;
import view.QueryStringBuilder;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class EndpointReadView extends FrontEndView
{

    public EndpointReadView(EndpointResource resource) throws MalformedURLException, TransformerConfigurationException, URISyntaxException
    {
	super(resource);
        setStyleSheet(getController().getServletContext().getResource(XSLT_PATH + "endpoint/" + getClass().getSimpleName() + ".xsl").toURI().toString());
    }

    @Override
    public EndpointResource getResource()
    {
	return (EndpointResource)super.getResource();
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setEndpoint(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/read/endpoint.rq"), getResource().getEndpoint().getURI())));

	super.display(request, response);
    }

    private void setEndpoint(ResultSetRewindable endpoint)
    {
	setDocument(XMLSerializer.serialize(endpoint));
    }

}
