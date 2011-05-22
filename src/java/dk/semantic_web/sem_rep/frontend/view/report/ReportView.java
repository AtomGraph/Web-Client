/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import dk.semantic_web.rdf_editor.frontend.controller.resource.instance.InstanceResource;
import dk.semantic_web.rdf_editor.frontend.view.instance.InstanceView;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.view.JSONSerializer;
import dk.semantic_web.sem_rep.view.XMLSerializer;

/**
 *
 * @author Pumba
 */
abstract public class ReportView extends InstanceView
{
    private ResultSetRewindable queryResults = null;

    public ReportView(InstanceResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
    }

    @Override
    protected String getStyleSheetPath() {
        return XSLT_BASE + "report/" + getClass().getSimpleName() + ".xsl";
    }

    public ResultSetRewindable getQueryResults()
    {
        return queryResults;
    }

    public void setQueryResults(ResultSetRewindable queryResults)
    {
        this.queryResults = queryResults;
    }

    /*
    protected void setQueryResults(ResultSetRewindable results)
    {
	getTransformer().setParameter("query-result", true);

	getResolver().setArgument("results", XMLSerializer.serialize(results));
    }
    */

    protected void setReport(ResultSetRewindable report)
    {
        setDocument(XMLSerializer.serialize(report));
        getResolver().setArgument("report", XMLSerializer.serialize(report));
    }

    protected void setEndpoints(ResultSetRewindable endpoints)
    {
	getResolver().setArgument("endpoints", XMLSerializer.serialize(endpoints));
    }

    protected void setVisualizations(ResultSetRewindable visualizations)
    {
        getResolver().setArgument("visualizations", XMLSerializer.serialize(visualizations));
	getTransformer().setParameter("visualizations-json", JSONSerializer.serialize(visualizations));
    }

    protected void setBindings(ResultSetRewindable bindings)
    {
	getResolver().setArgument("bindings", XMLSerializer.serialize(bindings));
	getTransformer().setParameter("bindings-json", JSONSerializer.serialize(bindings));
    }

    protected void setVariables(ResultSetRewindable variables)
    {
        getResolver().setArgument("variables", XMLSerializer.serialize(variables));
	getTransformer().setParameter("variables-json", JSONSerializer.serialize(variables));
    }

    protected void setOptions(ResultSetRewindable options)
    {
        getResolver().setArgument("options", XMLSerializer.serialize(options));
    }

    protected void setVisualizationTypes(ResultSetRewindable visTypes)
    {
	getResolver().setArgument("visualization-types", XMLSerializer.serialize(visTypes));
	getTransformer().setParameter("visualization-types-json", JSONSerializer.serialize(visTypes));
    }

    protected void setBindingTypes(ResultSetRewindable bindingTypes)
    {
	getResolver().setArgument("binding-types", XMLSerializer.serialize(bindingTypes));
	getTransformer().setParameter("binding-types-json", JSONSerializer.serialize(bindingTypes));
    }

    protected void setDataTypes(ResultSetRewindable dataTypes)
    {
	getResolver().setArgument("data-types", XMLSerializer.serialize(dataTypes));
	getTransformer().setParameter("data-types-json", JSONSerializer.serialize(dataTypes));
    }

}
