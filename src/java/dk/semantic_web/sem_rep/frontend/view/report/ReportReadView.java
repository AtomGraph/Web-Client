/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import dk.semantic_web.sem_rep.frontend.controller.resource.report.ReportResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import dk.semantic_web.sem_rep.view.QueryStringBuilder;
import dk.semantic_web.sem_rep.view.QueryResult;
import dk.semantic_web.sem_rep.view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportReadView extends ReportView
{

    public ReportReadView(ReportResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setVisualizationTypes(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/visualization-types.rq"))));
        setBindingTypes(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/binding-types.rq"))));
        setDataTypes(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/data-types.rq"))));

	//setQueryResults(QueryResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()), RESULTS_LIMIT);
        setReport(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/report.rq"), getResource().getURI())));
        setVisualizations(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/visualizations.rq"), getResource().getURI())));
        setBindings(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/bindings.rq"), getResource().getURI())));
        setVariables(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/variables.rq"), getResource().getURI())));
	setOptions(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/options.rq"), getResource().getURI())));

        setQueryUris(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/uris.rq"), getResource().getURI())));
        setComments(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/comments.rq"), getResource().getURI())));

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
