/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import com.hp.hpl.jena.query.ResultSet;
import frontend.controller.resource.report.ReportResource;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryStringBuilder;
import view.QueryResult;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{

    public ReportReadView(ReportResource resource) throws TransformerConfigurationException
    {
	super(resource);
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportReadView.xsl")));
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setQueryUris(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/uris.rq"), getResource().getAbsoluteURI())));
        setComments(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/comments.rq"), getResource().getAbsoluteURI())));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    /*
    protected void setQueryObjects(HttpServletRequest request, HttpServletResponse response)
    {
	String objects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/objects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-objects", objects);
    }

    protected void setQuerySubjects(HttpServletRequest request, HttpServletResponse response)
    {
	String subjects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/subjects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-subjects", subjects);
    }
    */

    protected void setQueryUris(ResultSet uris)
    {
	getResolver().setArgument("query-uris", XMLSerializer.serialize(uris));
    }

    protected void setComments(ResultSet comments)
    {
	getResolver().setArgument("comments", XMLSerializer.serialize(comments));
    }

}
