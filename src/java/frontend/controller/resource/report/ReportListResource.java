/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.http.HttpClient;
import dk.semantic_web.diy.http.HttpResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.form.ReportForm;
import frontend.controller.form.ScatterChartForm;
import frontend.controller.resource.FrontPageResource;
import frontend.view.report.ReportCreateView;
import frontend.view.report.ReportListView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;
import model.ScatterChart;
import model.ScatterChart;
import thewebsemantic.binding.Jenabean;
import util.TalisAuthenticator;

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

	    if (request.getParameter("action") != null && request.getParameter("action").equals("save")) create(request, response);
	    if (request.getParameter("action") != null && request.getParameter("action").equals("update")) update(request, response);
	}

	return view;
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
    {
	ReportForm form = new ReportForm(request);
	//String title = request.getParameter("title");
	//String queryString = request.getParameter("query-string");
	
	OntModel model = ModelFactory.createOntologyModel();
	Jenabean.instance().bind(model);

	User user = new User();
	user.setName("RandomUserName");
	user.setCreatedAt(new Date());

	Collection<Visualization> visualizations = new ArrayList<Visualization>();
	for (String visType : form.getVisualizations())
	{
	    if (visType.equals("table")) visualizations.add(new Table());
	    if (visType.equals("scatter-chart")) visualizations.add(new ScatterChart());
	    if (visType.equals("line-chart")) visualizations.add(new LineChart());
	    if (visType.equals("pie-chart")) visualizations.add(new PieChart());
	    if (visType.equals("map")) visualizations.add(new Map());
	}
	//ScatterChart chart = new ScatterChart();
	//chart.setXBinding("area");
	//chart.addYBinding("population");
	
	Query query = new Query();
	query.setQueryString(form.getQueryString());

	Report report = new Report(form.getTitle(), query, user);
	//report.addVisualization(chart);
	report.setVisualizations(visualizations);
		
	ReportResource resource = new ReportResource(report, this);
	resource.setController(getController());
	report.resource = resource; // report.setFrontEndResource(resource);
	
	report.save();

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

    private void update(HttpServletRequest request, HttpServletResponse response)
    {
	ScatterChartForm form = new ScatterChartForm(request);
	ScatterChart chart = new ScatterChart();
	
    }
}
