/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import com.hp.hpl.jena.query.ResultSet;
import frontend.controller.resource.report.ReportResource;
import frontend.view.FrontEndView;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryStringBuilder;
import view.QueryResult;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportView extends FrontEndView
{

    public ReportView(ReportResource resource)
    {
	super(resource);
    }

    @Override
    public ReportResource getResource()
    {
	return (ReportResource)super.getResource();
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setVisualizationTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/visualization-types.rq"))));
        setBindingTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/binding-types.rq"))));

	setQueryResults(QueryResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()));

        setReport(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/report.rq"), getResource().getAbsoluteURI())));
        setVisualizations(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI())));
        setBindings(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI())));
        setVariables(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/variables.rq"), getResource().getAbsoluteURI())));

        super.display(request, response);
    }

    public void setQueryResults(ResultSet results)
    {
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", XMLSerializer.serialize(results));
    }

/*
    protected void setQueryResult(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
        String endpointUri = getResource().getReport().getQuery().getEndpoint().toString();
        String queryString = getResource().getReport().getQuery().getQueryString();
        String results = QueryXMLResult.selectRemote(endpointUri, queryString);

	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }
*/

    protected void setQueryResult(ResultSet results)
    {
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", XMLSerializer.serialize(results));
    }

    protected void setReport(ResultSet report)
    {
        setDocument(XMLSerializer.serialize(report));
        getResolver().setArgument("report", XMLSerializer.serialize(report));
    }

    protected void setEndpoints(ResultSet endpoints)
    {
	getResolver().setArgument("endpoints", XMLSerializer.serialize(endpoints));
    }

    protected void setVisualizationTypes(ResultSet visTypes)
    {
	getResolver().setArgument("visualization-types", XMLSerializer.serialize(visTypes));
    }

    protected void setVisualizations(ResultSet visualizations)
    {
        getResolver().setArgument("visualizations", XMLSerializer.serialize(visualizations));
    }

    protected void setBindingTypes(ResultSet bindingTypes)
    {
	getResolver().setArgument("binding-types", XMLSerializer.serialize(bindingTypes));
    }

    protected void setBindings(ResultSet bindings)
    {
	getResolver().setArgument("bindings", XMLSerializer.serialize(bindings));
    }

    protected void setVariables(ResultSet variables)
    {
        getResolver().setArgument("variables", XMLSerializer.serialize(variables));
    }

}
