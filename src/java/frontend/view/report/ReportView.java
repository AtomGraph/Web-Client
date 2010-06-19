/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.resource.report.ReportResource;
import frontend.view.FrontEndView;
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
        setVisualizationTypes(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/visualization-types.rq"))));
        setBindingTypes(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/binding-types.rq"))));

	setQueryResults(QueryXMLResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()));

        setReport(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/report.rq"), getResource().getAbsoluteURI())));
        setVisualizations(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI())));
        setBindings(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI())));
        setVariables(QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/variables.rq"), getResource().getAbsoluteURI())));

        super.display(request, response);
    }

    public void setQueryResults(String results)
    {
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
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

    protected void setQueryResult(String results)
    {
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", results);
    }

    protected void setReport(String report)
    {
        setDocument(report);
        getResolver().setArgument("report", report);
    }

    protected void setEndpoints(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String endpoints = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/endpoints.rq")));

	getResolver().setArgument("endpoints", endpoints);
    }

    protected void setVisualizationTypes(String visTypes)
    {
	getResolver().setArgument("visualization-types", visTypes);
    }

    protected void setVisualizations(String visualizations)
    {
        getResolver().setArgument("visualizations", visualizations);
    }

    protected void setBindingTypes(String bindingTypes)
    {
	getResolver().setArgument("binding-types", bindingTypes);
    }

    protected void setBindings(String bindings)
    {
	getResolver().setArgument("bindings", bindings);
    }

    protected void setVariables(String variables)
    {
        getResolver().setArgument("variables", variables);
    }

}
