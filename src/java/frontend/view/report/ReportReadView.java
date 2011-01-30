/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import frontend.controller.resource.report.ReportResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.sdb.SDB;
import view.QueryStringBuilder;
import view.QueryResult;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{

    public ReportReadView(ReportResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
        setStyleSheet(getController().getServletContext().getResource(XSLT_PATH + "report/" + getClass().getSimpleName() + ".xsl").toURI().toString());
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setVisualizationTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/visualization-types.rq"))));
        setBindingTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/binding-types.rq"))));
        setDataTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/data-types.rq"))));

	//setQueryResults(QueryResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()), RESULTS_LIMIT);
        setReport(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/report.rq"), getResource().getAbsoluteURI())));
        setVisualizations(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI())));
        setBindings(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI())));
        setVariables(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/variables.rq"), getResource().getAbsoluteURI())));
	setOptions(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/options.rq"), getResource().getAbsoluteURI())));

        setQueryUris(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/uris.rq"), getResource().getAbsoluteURI())));
        setComments(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/comments.rq"), getResource().getAbsoluteURI())));

        getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    /*
    protected void setQueryObjects(HttpServletRequest request, HttpServletResponse response)
    {
	String objects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/objects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-objects", objects);
    }

    protected void setQuerySubjects(HttpServletRequest request, HttpServletResponse response)
    {
	String subjects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/subjects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-subjects", subjects);
    }
    */

    protected void setQueryUris(ResultSetRewindable uris)
    {
	getResolver().setArgument("query-uris", XMLSerializer.serialize(uris));
    }

    protected void setComments(ResultSetRewindable comments)
    {
	getResolver().setArgument("comments", XMLSerializer.serialize(comments));
    }

}
