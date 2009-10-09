/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend.view.report;

import com.hp.hpl.jena.rdf.model.Model;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ReportCreateView extends FrontEndView
{

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
        
	if (request.getAttribute("query-result") != null)
	{
            setReport(request, response);

            getResolver().setArgument("results", (String) request.getAttribute("query-results"));
	    getTransformer().setParameter("query-result", true);
	    //getTransformer().setParameter("query-string", request.getParameter("query-string"));
	}

        UUID id = UUID.randomUUID();
        getTransformer().setParameter("report-id", String.valueOf(id));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setVisualizationTypes(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
    {
	String visTypes = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/visualization-types.rq")));

	getResolver().setArgument("visualization-types", visTypes);
    }

    protected void setReport(HttpServletRequest request, HttpServletResponse response)
    {
	try
	{
            Model model = (Model)request.getAttribute("report-model");
            String reportId = "http://localhost:8084/semantic-reports/reports/" + request.getParameter("report-id");
            
	    String report = QueryXMLResult.select(model, QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/report.rq"), reportId));
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

}
