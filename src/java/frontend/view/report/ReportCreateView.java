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
import java.util.UUID;
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
    private boolean successful;
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

        setVisualizationTypes(request, response);
        setBindingTypes(request, response);
        
	if (isSuccessful())
	{
            setReport(request, response);
            setVisualizations(request, response);

            getResolver().setArgument("results", getQueryResults());
	    getTransformer().setParameter("query-result", "success");
	    //getTransformer().setParameter("query-string", request.getParameter("query-string"));
	}
        else
        {
            getResolver().setArgument("query-errors", XMLSerializer.serialize(getErrors()));
            getTransformer().setParameter("query-result", "failure");
        }

        //UUID id = UUID.randomUUID();
        //getTransformer().setParameter("report-id", String.valueOf(id));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setVisualizationTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String visTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/visualization-types.rq")));

	getResolver().setArgument("visualization-types", visTypes);
    }

    protected void setBindingTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String bindingTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/binding-types.rq")));

	getResolver().setArgument("binding-types", bindingTypes);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
            String reportUri = request.getParameter("report-uri");
            
	    String report = QueryXMLResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/report.rq"), reportUri));
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
            String reportId = "http://localhost:8084/semantic-reports/reports/" + request.getParameter("report-id");

	    String visualizations = QueryXMLResult.select(getModel(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/visualizations.rq"), reportId));
	    String variables = QueryXMLResult.select(model, QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/variables.rq"), reportId));

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

    public Form getForm()
    {
        return form;
    }

    public void setForm(Form form)
    {
        this.form = form;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
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

    public List<Error> getErrors()
    {
        return errors;
    }

    public void setErrors(List<Error> errors)
    {
        this.errors = errors;
    }

}
