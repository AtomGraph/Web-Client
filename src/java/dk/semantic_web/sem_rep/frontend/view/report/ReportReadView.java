/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import dk.semantic_web.rdf_editor.frontend.controller.resource.instance.InstanceResource;
import dk.semantic_web.rdf_editor.model.Model;
import dk.semantic_web.rdf_editor.view.QueryResult;
import dk.semantic_web.rdf_editor.view.QueryStringBuilder;
import dk.semantic_web.rdf_editor.view.XMLSerializer;
import dk.semantic_web.sem_rep.model.Report;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thewebsemantic.RDF2Bean;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{
    public static final long RESULTS_LIMIT = 50;
    static final Logger logger = LoggerFactory.getLogger(ReportReadView.class);

    private Report report = null;
    
    public ReportReadView(InstanceResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
	RDF2Bean reader = new RDF2Bean(Model.getInstance().getData());
	reader.bindAll("dk.semantic_web.sem_rep.model");
	String reportUri = getResource().getTopicResource().getURI();
	logger.info("Report URI: {}", reportUri);
	Report rep = reader.load(Report.class, reportUri);
	logger.info("Report: {}", rep);
	setReport(rep);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response, OutputStream out) throws IOException, TransformerException, ParserConfigurationException
    {
	setVisualizationTypes(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/visualization-types.rq"))));
        setBindingTypes(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/binding-types.rq"))));
        setDataTypes(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/data-types.rq"))));

	//setQueryResults(QueryResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()), RESULTS_LIMIT);
        setReport(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/report.rq"), getResource().getURI())));
        setVisualizations(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/visualizations.rq"), getResource().getURI())));
        setBindings(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/bindings.rq"), getResource().getURI())));
        setVariables(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/variables.rq"), getResource().getURI())));
	setOptions(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/options.rq"), getResource().getURI())));

        setQueryUris(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/uris.rq"), getResource().getURI())));
        setComments(QueryResult.select(Model.getInstance().getDataset(), QueryStringBuilder.build(getQueryString("report/read/comments.rq"), getResource().getURI())));

	setQueryResults(QueryResult.selectRemote(getReport().getQuery().getEndpoint().toString(), getReport().getQuery().getQueryString(), RESULTS_LIMIT));
        getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));

	super.display(request, response, out);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    /*
    protected void setQueryObjects(HttpServletRequest request, HttpServletResponse response)
    {
	String objects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getQueryString("report/read/objects.rq"), getResource().getAbsoluteURI()));

	getResolver().setArgument("query-objects", objects);
    }

    protected void setQuerySubjects(HttpServletRequest request, HttpServletResponse response)
    {
	String subjects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getQueryString("report/read/subjects.rq"), getResource().getAbsoluteURI()));

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

    public Report getReport()
    {
	return report;
    }

    public final void setReport(Report report)
    {
	this.report = report;
    }

}
