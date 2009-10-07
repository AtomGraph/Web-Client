/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.util.FileUtils;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.form.RDFForm;
import frontend.controller.form.ReportForm;
import frontend.controller.form.ReportRDFForm;
import frontend.controller.form.ScatterChartForm;
import frontend.controller.resource.FrontPageResource;
import frontend.view.report.ReportCreateView;
import frontend.view.report.ReportListView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import thewebsemantic.binding.Jenabean;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportListResource extends FrontEndResource implements Singleton
{
    private static final String RELATIVE_URI = "reports";
    private static final ReportListResource INSTANCE = new ReportListResource(FrontPageResource.getInstance());
    private View view = null;
    
    private ReportListResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static ReportListResource getInstance()
    {
	return INSTANCE;
    }
    
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(RELATIVE_URI, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else
	{
	    view = new ReportListView(this);
	    
	    if (request.getParameter("view") != null && request.getParameter("view").equals("create")) view = new ReportCreateView(this);

	    if (request.getParameter("action") != null && request.getParameter("action").equals("query")) query(request, response);
	    if (request.getParameter("action") != null && request.getParameter("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void query(HttpServletRequest request, HttpServletResponse response)
    {
	//ReportForm form = new ReportForm(request);
	ReportRDFForm form = new ReportRDFForm(request);
//Jenabean.instance().bind(form.getModel());

	String queryResults = null;
	try
	{
	    queryResults = QueryXMLResult.queryRemote(form.getEndpoint(), form.getQueryString());
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	
	request.setAttribute("query-results", queryResults);
	request.setAttribute("report-model", form.getModel());
	request.setAttribute("query-result", Boolean.TRUE);
    }
    
    private void save(HttpServletRequest request, HttpServletResponse response)
    {
	RDFForm rdfForm = new RDFForm(request);
	
	ReportForm reportForm = new ReportForm(request);

	Jenabean.instance().bind(SDB.getInstanceModel());

	User user = new User();
	user.setName("RandomUserName");
	user.setCreatedAt(new Date());

	Collection<Visualization> visualizations = new ArrayList<Visualization>();
	for (String visType : reportForm.getVisualizations())
	{
	    if (visType.equals("table"))
	    {
		visualizations.add(new Table());
	    }
	    if (visType.equals("scatter-chart"))
	    {
		ScatterChartForm chartForm = new ScatterChartForm(request);
		
		ScatterChart chart = new ScatterChart();
		chart.setXBinding(chartForm.getXBinding());
		for (String yBinding : chartForm.getYBinding())
		    chart.addYBinding(yBinding);
		visualizations.add(chart);
	    }
	    if (visType.equals("line-chart"))
	    {
		visualizations.add(new LineChart());
	    }
	    if (visType.equals("pie-chart"))
	    {
		visualizations.add(new PieChart());
	    }
	    if (visType.equals("map"))
	    {
		visualizations.add(new Map());
	    }
	}
	Query query = new Query();
	query.setQueryString(reportForm.getQueryString());
	
	try
	{
	    query.setEndpoint(new URI(reportForm.getEndpoint()));
	} catch (URISyntaxException ex)
	{
	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	
	Report report = new Report(reportForm.getTitle(), query, user);   
	report.setVisualizations(visualizations);

	ReportResource resource = new ReportResource(report, this);
	resource.setController(getController());
	report.setFrontEndResource(resource);
	report.save();

	SPINModuleRegistry.get().init();

	//queryModel.setNsPrefix("rdf", RDF.getURI());
	//queryModel.setNsPrefix("ex", "http://example.org/demo#");
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(SDB.getDefaultModel(), reportForm.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(SDB.getDefaultModel());
	//arq2Spin.setVarNamespace("http://www.semanticreports.com/queries/");
	Select spinQuery = (Select) arq2Spin.createQuery(arqQuery, "http://spinrdf.org/sp#Select/" + query.hashCode());
	SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);
    }

    /*
    // Save model to Talis store
    private static void saveModel(Model model)
    {
	try
	{
	    URL metaUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/meta");
	    Authenticator.setDefault(new TalisAuthenticator());

	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(metaUrl.getHost());
	    remoteRequest.setPathInfo(metaUrl.getPath());
	    remoteRequest.setHeader("Content-Type", "application/rdf+xml");

	    model.write(remoteRequest.getOutputStream());
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    */
}
