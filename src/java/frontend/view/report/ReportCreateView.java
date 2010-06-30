/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend.view.report;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import dk.semantic_web.diy.controller.Form;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.FormResultView;
import view.QueryStringBuilder;
import view.QueryResult;
import view.XMLSerializer;
import dk.semantic_web.diy.controller.Error;

/**
 *
 * @author Pumba
 */
public class ReportCreateView extends FrontEndView implements FormResultView
{
    private Form form = null;
    private List<Error> errors = null;
    private Boolean result = null;
    private ResultSetRewindable queryResults = null;
    private Model model = null;
    
    public ReportCreateView(ReportListResource resource) throws TransformerConfigurationException
    {
	super(resource);
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportCreateView.xsl")));
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");

        setEndpoints(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/endpoints.rq"))));
        setVisualizationTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/visualization-types.rq"))));
        setBindingTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/binding-types.rq"))));
        setDataTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/data-types.rq"))));

	if (getResult() != null)
        {
            String reportUri = request.getParameter("report-uri"); // after 1st request ("Query" submit), $report-uri is known
            setReport(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/report.rq"), reportUri)));

            getTransformer().setParameter("query-result", getResult());
            
            if (getResult())
            {
                setVisualizations(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/visualizations.rq"), reportUri)));
                setVariables(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/variables.rq"), reportUri)));

                getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));
            }
            else
            {
                getResolver().setArgument("query-errors", XMLSerializer.serialize(getErrors()));
            }
        }

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setEndpoints(ResultSetRewindable endpoints)
    {
	getResolver().setArgument("endpoints", XMLSerializer.serialize(endpoints));
    }

    protected void setVisualizationTypes(ResultSetRewindable visTypes)
    {
	getResolver().setArgument("visualization-types", XMLSerializer.serialize(visTypes));
    }

    protected void setBindingTypes(ResultSetRewindable bindingTypes)
    {
	getResolver().setArgument("binding-types", XMLSerializer.serialize(bindingTypes));
    }

    protected void setDataTypes(ResultSetRewindable dataTypes)
    {
	getResolver().setArgument("data-types", XMLSerializer.serialize(dataTypes));
    }

    protected void setReport(ResultSetRewindable report)
    {
        getResolver().setArgument("report", XMLSerializer.serialize(report));
    }

    protected void setVisualizations(ResultSetRewindable visualizations)
    {
        getResolver().setArgument("visualizations", XMLSerializer.serialize(visualizations));
    }

    protected void setVariables(ResultSetRewindable variables)
    {
        getResolver().setArgument("variables", XMLSerializer.serialize(variables));
    }

    @Override
    public Form getForm()
    {
        return form;
    }

    @Override
    public void setForm(Form form)
    {
        this.form = form;
    }

    @Override
    public Boolean getResult()
    {
        return result;
    }

    @Override
    public void setResult(Boolean successful)
    {
        this.result = successful;
    }

    public ResultSetRewindable getQueryResults()
    {
        return queryResults;
    }

    public void setQueryResults(ResultSetRewindable queryResults)
    {
        this.queryResults = queryResults;
    }

    public Model getModel()
    {
        return model;
    }

    public void setModel(Model model)
    {
        this.model = model;
    }

    @Override
    public List<Error> getErrors()
    {
        return errors;
    }

    @Override
    public void setErrors(List<Error> errors)
    {
        this.errors = errors;
    }

}
