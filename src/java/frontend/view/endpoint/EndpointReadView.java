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
import model.sdb.SDB;
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

	String queryString = QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/read/reports.rq"), getResource().getEndpoint().getURI().toString(), "dateCreated", 0, 20); // QUIRK!!!
	setReports(QueryResult.select(SDB.getDataset(), queryString));

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
