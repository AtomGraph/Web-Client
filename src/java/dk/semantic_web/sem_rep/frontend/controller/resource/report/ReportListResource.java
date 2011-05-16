/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.report;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.model.vocabulary.DublinCore;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.resource.Singleton;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.frontend.controller.resource.FrontPageResource;
import dk.semantic_web.sem_rep.frontend.controller.exception.InvalidFormException;
import dk.semantic_web.sem_rep.frontend.controller.exception.NoResultsException;
import dk.semantic_web.sem_rep.frontend.controller.form.ReportRDFForm;
import dk.semantic_web.sem_rep.frontend.view.report.ReportCreateView;
import dk.semantic_web.sem_rep.frontend.view.report.ReportListView;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.sem_rep.model.vocabulary.Namespaces;
import dk.semantic_web.sem_rep.model.vocabulary.Sioc;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import dk.semantic_web.sem_rep.view.QueryResult;
import dk.semantic_web.sem_rep.view.XMLSerializer;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Pumba
 */

@Singleton
@Path(ReportListResource.PATH)
public class ReportListResource extends FrontEndResource
{
    public static final String PATH = "reports";
    //private static final ReportListResource INSTANCE = new ReportListResource(FrontPageResource.getInstance());
    public static final UriBuilder URI_BUILDER = FrontPageResource.URI_BUILDER.clone().path(PATH);

    private ReportListResource(FrontPageResource parent, @Context UriInfo uriInfo)
    {
	super(parent, uriInfo);
    }
    
    @Path("{id}")
    public ReportResource getReportResource(@Context UriInfo uriInfo) {
	ReportResource resource = null;
	if (dk.semantic_web.rdf_editor.model.Model.getInstance().getSystemOnt().getIndividual(uriInfo.getAbsolutePath().toString()) != null)
	    resource = new ReportResource(this, uriInfo);
	if (resource == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	return resource;
    }

    @Override
    @GET
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

        if (isQueryAction(request) || isProxyQueryAction(request)) return query(request, response);
        //if (isSaveAction(request)) save(request, response);

        if (isCreateView(request)) return new ReportCreateView(this);

        return new ReportListView(this);
    }

    @Override
    @POST
    public View doPost(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
        View parent = super.doPost(request, response);
	if (parent != null) return parent;

        if (isQueryAction(request)) return query(request, response);
        if (isSaveAction(request)) return save(request, response);

        if (isCreateView(request)) return new ReportCreateView(this);

	return new ReportListView(this);
    }

    private ReportCreateView query(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
        ReportCreateView view = new ReportCreateView(this);

	ReportRDFForm form = new ReportRDFForm(request);
        List<Error> errors = form.validate();
        view.setForm(form);
        view.setModel(form.getModel());
        
	try
	{
            if (!errors.isEmpty()) throw new InvalidFormException();

	    ResultSetRewindable queryResults = QueryResult.selectRemote(form.getEndpoint().getURI(), form.getQueryString(), ReportResource.RESULTS_LIMIT);
            int count = ResultSetFormatter.consume(ResultSetFactory.copyResults(queryResults));
            if (count == 0) throw new NoResultsException();
            
            view.setQueryResults(queryResults);
            view.setResult(true);
	}
        catch (InvalidFormException ex)
	{
            view.setErrors(errors);
            view.setResult(false);
	}
        catch (NoResultsException ex)
	{
            errors.add(new Error("noResults"));

            view.setErrors(errors);
            view.setResult(false);
	}
        catch (IOException ex)
	{
            errors.add(new Error("ioError"));

            view.setErrors(errors);
            view.setResult(false);

	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
        catch (QueryException ex)
	{
            errors.add(new Error("invalidQuery", ex.getMessage()));

            view.setErrors(errors);
            view.setResult(false);

	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	
        return view;
    }

    private ReportCreateView save(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
        ReportCreateView view = new ReportCreateView(this);

	ReportRDFForm form = new ReportRDFForm(request);
        List<Error> errors = form.validateWithTitle();
        view.setForm(form);
        view.setModel(form.getModel());

	try
	{
            if (!errors.isEmpty()) throw new InvalidFormException();

	    ResultSetRewindable queryResults = QueryResult.selectRemote(form.getEndpoint().getURI(), form.getQueryString(), ReportResource.RESULTS_LIMIT);
            int count = ResultSetFormatter.consume(ResultSetFactory.copyResults(queryResults));
            if (count == 0) throw new NoResultsException();
            view.setQueryResults(queryResults);

	    saveModel(form, queryResults);
	    
            view.setResult(true);
	    response.sendRedirect(form.getReport().getURI());
	}
        catch (InvalidFormException ex)
	{
            view.setErrors(errors);
            view.setResult(false);
	}
        catch (NoResultsException ex)
	{
            errors.add(new Error("noResults"));

            view.setErrors(errors);
            view.setResult(false);
	}
        catch (IOException ex)
	{
            errors.add(new Error("ioError"));

            view.setErrors(errors);
            view.setResult(false);

	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}
        catch (QueryException ex)
	{
            errors.add(new Error("invalidQuery", ex.getMessage()));

            view.setErrors(errors);
            view.setResult(false);

	    Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
	}

        return view;
    }

    public void saveModel(ReportRDFForm form, ResultSetRewindable results)
    {
	//ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	//arq2Spin.setVarNamespace("http://www.semanticreports.com/queries/");
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, form.getQueryResource().getURI());

        // add some metadata
        String userUri = getController().getMapping().getHost() + "users/admin"; // QUIRK
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Model model = form.getModel();
        model.add(form.getReport(), model.createProperty(DublinCore.CREATED), model.createTypedLiteral(calendar));
        model.add(form.getReport(), model.createProperty(DublinCore.CREATOR), model.createResource(userUri));
        model.add(form.getReport(), RDF.type, model.createResource(Sioc.Forum));
        model.add(form.getReport(), model.createProperty(Sioc.has_creator), model.createResource(userUri));
        model.add(model.createResource(userUri), RDF.type, model.createResource(Sioc.UserAccount));
        model.add(model.createResource(userUri), model.createProperty(Sioc.name), model.createTypedLiteral("Admin"));

String xmlString = XMLSerializer.serialize(results);
xmlString = xmlString.substring("<?xml version='1.0'?>".length());
model.add(form.getQueryResource(), model.createProperty(Namespaces.REPORT_NS + "lastResult"), model.createLiteral(xmlString, true));
	
        dk.semantic_web.rdf_editor.model.Model.getInstance().getData().add(model); // save report
//SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);
//form.getModel().write(System.out);
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

    protected boolean isCreateView(HttpServletRequest request)
    {
        return (request.getParameter("view") != null && request.getParameter("view").equals("create"));
    }

    protected boolean isQueryAction(HttpServletRequest request)
    {
        return (request.getParameter("action") != null && request.getParameter("action").equals("query"));
    }

    protected boolean isProxyQueryAction(HttpServletRequest request)
    {
        return ((request.getParameter("endpoint") != null && request.getParameter("query") != null));
    }

    protected boolean isSaveAction(HttpServletRequest request)
    {
        return (request.getParameter("action") != null && request.getParameter("action").equals("save"));
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public String getAbsolutePath()
    {
	return getPath();
    }

    @Override
    public URI getRealURI()
    {
	return URI_BUILDER.build();
    }

    @Override
    public UriBuilder getUriBuilder() {
	return URI_BUILDER;
    }

}
