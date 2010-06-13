/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportResource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{

    public ReportReadView(ReportResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportReadView.xsl")));

	setReport(request, response);
	setQueryResult(request, response);
	setVisualizations(request, response);
        setBindings(request, response);
        setVariables(request, response);
        setVisualizationTypes(request, response);
        setBindingTypes(request, response);
        setQueryUris(request, response);
        setComments(request, response);

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setVisualizationTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String visTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/visualization-types.rq")));

	getResolver().setArgument("visualization-types", visTypes);
    }

    protected void setBindingTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String bindingTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/binding-types.rq")));

	getResolver().setArgument("binding-types", bindingTypes);
    }

    /*
    protected void setQueryObjects(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String objects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/objects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-objects", objects);
    }

    protected void setQuerySubjects(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String subjects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/subjects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-subjects", subjects);
    }
    */

    protected void setQueryUris(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String uris = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/uris.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-uris", uris);
    }

    protected void setComments(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String comments = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/comments.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("comments", comments);
    }

}
