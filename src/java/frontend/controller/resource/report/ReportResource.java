/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import dk.semantic_web.diy.controller.Resource;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.http.HttpClient;
import dk.semantic_web.diy.http.HttpResponse;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.resource.FrontPageResource;
import java.net.Authenticator;
import java.net.URL;
import model.Namespaces;
import util.TalisAuthenticator;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";
    private static final ReportResource INSTANCE = new ReportResource(FrontPageResource.getInstance());
    
    private View view = null;
    
    public ReportResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static Resource getInstance()
    {
	return INSTANCE;
    }
    
    public String getRelativeURI()
    {
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else view = new ReportView(this);

	return view;
    }

    @Override
    public View doPost(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doPost(request, response);
	if (parent != null) view = parent;
	else
	{
	    view = new ReportView(this);
	    
	    if (request.getParameter("action") != null && request.getParameter("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
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
