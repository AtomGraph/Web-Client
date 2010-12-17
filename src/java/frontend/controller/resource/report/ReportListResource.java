/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerConfigurationException;
import model.vocabulary.DublinCore;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.exception.InvalidFormException;
import frontend.controller.exception.NoResultsException;
import frontend.controller.form.ReportRDFForm;
import frontend.controller.resource.FrontPageResource;
import frontend.view.report.ReportCreateView;
import frontend.view.report.ReportListView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;
import model.vocabulary.Sioc;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import view.QueryResult;

/**
 *
 * @author Pumba
 */
public class ReportListResource extends FrontEndResource implements Singleton
{
    private static final String RELATIVE_URI = "reports";
    private static final ReportListResource INSTANCE = new ReportListResource(FrontPageResource.getInstance());
    //private View view = null;
    
    private ReportListResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static ReportListResource getInstance()
    {
	return INSTANCE;
    }
    
    @Override
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

	    saveModel(form);
	    
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

    public void saveModel(ReportRDFForm form)
    {
	//ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	//arq2Spin.setVarNamespace("http://www.semanticreports.com/queries/");
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, form.getQueryResource().getURI());

        // add some metadata
        String userUri = getController().getMapping().getHost() + "users/admin";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Model model = form.getModel();
        model.add(form.getReport(), model.createProperty(DublinCore.CREATED), model.createTypedLiteral(calendar));
        model.add(form.getReport(), model.createProperty(DublinCore.CREATOR), model.createResource(userUri));
        model.add(form.getReport(), RDF.type, model.createResource(Sioc.Forum));
        model.add(form.getReport(), model.createProperty(Sioc.has_creator), model.createResource(userUri));
        model.add(model.createResource(userUri), RDF.type, model.createResource(Sioc.UserAccount));
        model.add(model.createResource(userUri), model.createProperty(Sioc.name), model.createTypedLiteral("Admin"));

        SDB.getInstanceModel().add(model); // save report
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

}
