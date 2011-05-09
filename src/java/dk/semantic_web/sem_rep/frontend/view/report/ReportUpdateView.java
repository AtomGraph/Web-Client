/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import dk.semantic_web.diy.controller.Form;
import dk.semantic_web.diy.controller.Error;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.model.sdb.SDB;
import dk.semantic_web.sem_rep.frontend.controller.resource.report.ReportResource;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import dk.semantic_web.sem_rep.view.FormResultView;
import dk.semantic_web.sem_rep.view.JSONSerializer;
import dk.semantic_web.sem_rep.view.QueryStringBuilder;
import dk.semantic_web.sem_rep.view.QueryResult;
import dk.semantic_web.sem_rep.view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportUpdateView extends ReportView implements FormResultView
{
    private Form form = null;
    private List<Error> errors = null;
    private Boolean result = null;
    private ResultSetRewindable queryResults = null;
    private Model model = null;

    public ReportUpdateView(ReportResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
	//setStyleSheet(new File(getController().getServletContext().getResourceAsStream("/WEB-INF/xslt/report/ReportCreateView.xsl")));
        setStyleSheet(getController().getServletContext().getResource(XSLT_PATH + "report/" + ReportCreateView.class.getSimpleName() + ".xsl").toURI().toString());
    }

    @Override
    public ReportResource getResource()
    {
	return (ReportResource)super.getResource();
    }
    
    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setEndpoints(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/list/endpoints.rq"))));
        setVisualizationTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/visualization-types.rq"))));
	setDataTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/data-types.rq"))));
        setOptionTypes(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/ontology/option-types.rq"))));

        //setComments(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/comments.rq"), getResource().getAbsoluteURI())));

        if (getResult() != null) // as in Create
        {
            setReport(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/report.rq"), getResource().getAbsoluteURI())));

            getTransformer().setParameter("query-result", getResult());
            
            if (getResult())
            {
                setVisualizations(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI())));
                setBindings(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI())));
                setVariables(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/variables.rq"), getResource().getAbsoluteURI())));
		setOptions(QueryResult.select(getModel(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/options.rq"), getResource().getAbsoluteURI())));

                getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));
            }
            else
            {
                getResolver().setArgument("query-errors", XMLSerializer.serialize(getErrors()));
            }
        }
        else // as in Read
        {
            //setQueryResults(QueryResult.selectRemote(getResource().getReport().getQuery().getEndpoint().toString(), getResource().getReport().getQuery().getQueryString()), RESULTS_LIMIT);
            setReport(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/report.rq"), getResource().getAbsoluteURI())));
            setVisualizations(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/visualizations.rq"), getResource().getAbsoluteURI())));
            setBindings(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/bindings.rq"), getResource().getAbsoluteURI())));
            setVariables(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/variables.rq"), getResource().getAbsoluteURI())));
	    setOptions(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/read/options.rq"), getResource().getAbsoluteURI())));

            getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));
        }

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setVisualizationTypes(ResultSetRewindable visTypes)
    {
	getResolver().setArgument("visualization-types", XMLSerializer.serialize(visTypes));
	getTransformer().setParameter("visualization-types-json", JSONSerializer.serialize(visTypes));
    }

    @Override
    protected void setDataTypes(ResultSetRewindable dataTypes)
    {
	getResolver().setArgument("data-types", XMLSerializer.serialize(dataTypes));
	getTransformer().setParameter("data-types-json", JSONSerializer.serialize(dataTypes));
    }

    protected void setOptionTypes(ResultSetRewindable optionTypes)
    {
	getResolver().setArgument("option-types", XMLSerializer.serialize(optionTypes));
	getTransformer().setParameter("option-types-json", JSONSerializer.serialize(optionTypes));
    }

    @Override
    public Boolean getResult()
    {
        return result;
    }

    @Override
    public void setResult(Boolean result)
    {
        this.result = result;
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

    public Model getModel()
    {
        return model;
    }

    public void setModel(Model model)
    {
        this.model = model;
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

}
