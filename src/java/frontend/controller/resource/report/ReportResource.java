/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
import dk.semantic_web.diy.http.HttpClient;
import dk.semantic_web.diy.http.HttpResponse;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportReadView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import model.Namespaces;
import model.Query;
import model.Report;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import thewebsemantic.binding.Jenabean;
import util.TalisAuthenticator;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource
{
    private View view = null;
    private Report report = null;
    
    public ReportResource(Report report, ReportListResource parent)
    {
	super(parent);
	setReport(report);
	report.setFrontEndResource(this);
    }
    
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(getReport().getTitle(), "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return getReport().getTitle();
    }

    public Report getReport()
    {
	return report;
    }

    public void setReport(Report report)
    {
	this.report = report;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else view = new ReportReadView(this);

	return view;
    }

    @Override
    public View doPost(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doPost(request, response);
	if (parent != null) view = parent;
	else
	{
	    view = new ReportReadView(this);
	    
	    if (request.getParameter("action") != null && request.getParameter("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
    {
	String title = request.getParameter("title");
	String queryString = request.getParameter("query-string");

	SPINModuleRegistry.get().init();
	Model queryModel = ModelFactory.createDefaultModel();
	//queryModel.setNsPrefix("rdf", RDF.getURI());
	//queryModel.setNsPrefix("ex", "http://example.org/demo#");
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(queryModel, queryString);
	ARQ2SPIN arq2SPIN = new ARQ2SPIN(queryModel);
	Select spinQuery = (Select) arq2SPIN.createQuery(arqQuery, null);
	queryModel.write(System.out, FileUtils.langXMLAbbrev);

	OntModel model = ModelFactory.createOntologyModel();
	Jenabean.instance().bind(model);

	Query query = new Query();
	query.setQueryString(queryString);

	Report report = new Report();
	report.setTitle(title);
	report.setQuery(query);
	report.setFrontEndResource(this);
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
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    private void save1(HttpServletRequest request, HttpServletResponse response)
    {
	String queryString = request.getParameter("query-string");
	
	try
	{
	    URL itemsUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/items");
	    URL metaUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/meta");	    
	    Authenticator.setDefault(new TalisAuthenticator());

	    /*
	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(itemsUrl.getHost());
	    remoteRequest.setPathInfo(itemsUrl.getPath());
	    remoteRequest.setHeader("Content-Type", "text/plain");
	    remoteRequest.setQueryString(queryString); // QUIRK - it's actually request body
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	    */

	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(metaUrl.getHost());
	    remoteRequest.setPathInfo(metaUrl.getPath());
	    remoteRequest.setHeader("Content-Type", "application/rdf+xml");
	    //remoteRequest.setQueryString(queryString); // QUIRK - it's actually request body
		    
	    OntModel model = ModelFactory.createOntologyModel();
	    Individual reportInd = model.createIndividual(model.createClass(Namespaces.REPORT_NS + "Post"));
	    DatatypeProperty queryStringProperty = model.createDatatypeProperty(Namespaces.REPORT_NS + "sparqlQueryString");
	    DatatypeProperty dateProperty = model.createDatatypeProperty(Namespaces.REPORT_NS + "sparqlQueryString");
	    Literal queryStringLiteral = model.createLiteral(queryString);
	    reportInd.setPropertyValue(queryStringProperty, queryStringLiteral);
	    
	    model.write(remoteRequest.getOutputStream());
	    
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	    System.out.println(remoteResponse.getStatus());
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
   
}
