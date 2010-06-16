/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend.view.report;

import com.hp.hpl.jena.rdf.model.Model;
import dk.semantic_web.diy.controller.Form;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.FormResultView;
import view.QueryStringBuilder;
import view.QueryXMLResult;
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
    private String queryResults = null;
    private Model model = null;
    
    public ReportCreateView(ReportListResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportCreateView.xsl")));

	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");

        setEndpoints(request, response);
        setVisualizationTypes(request, response);
        setBindingTypes(request, response);
        setDataTypes(request, response);

	if (getResult() != null)
        {
            setReport(request, response);
            
            if (getResult())
            {
                setVisualizations(request, response);

                getResolver().setArgument("results", getQueryResults());
                //getTransformer().setParameter("query-string", request.getParameter("query-string"));
            }
            else
            {
                getResolver().setArgument("query-errors", XMLSerializer.serialize(getErrors()));
                //getTransformer().setParameter("query-result", "failure");
            }
            getTransformer().setParameter("query-result", getResult());
        }

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setEndpoints(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String endpoints = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/endpoints.rq")));

	getResolver().setArgument("endpoints", endpoints);
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

    protected void setDataTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String dataTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/ontology/data-types.rq")));

	getResolver().setArgument("data-types", dataTypes);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
            String reportUri = request.getParameter("report-uri");  // after 1st request ("Query" submit), $report-uri is known
            
	    String report = QueryXMLResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/report.rq"), reportUri));
	    setDocument(report);

	    getResolver().setArgument("report", report);
	} catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportCreateView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportCreateView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    protected void setVisualizations(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
            String reportUri = request.getParameter("report-uri"); // after 1st request ("Query" submit), $report-uri is known

	    String visualizations = QueryXMLResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/visualizations.rq"), reportUri));
	    String variables = QueryXMLResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/read/variables.rq"), reportUri));

	    getResolver().setArgument("visualizations", visualizations);
	    getResolver().setArgument("variables", variables);
        } catch (FileNotFoundException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportReadView.class.getName()).log(Level.SEVERE, null, ex);
	}
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

    public String getQueryResults()
    {
        return queryResults;
    }

    public void setQueryResults(String queryResults)
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
