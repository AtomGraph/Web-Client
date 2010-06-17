/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import model.SDB;
import frontend.controller.resource.report.ReportResource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportUpdateView extends ReportView
{

    public ReportUpdateView(ReportResource resource)
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
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportCreateView.xsl")));

	setReport(request, response);
        setEndpoints(request, response);
	setQueryResult(request, response);
	setVisualizations(request, response);
setVariables(request, response);
setBindings(request, response);
setVisualizationTypes(request, response);
setBindingTypes(request, response);
setDataTypes(request, response);

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
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
}
