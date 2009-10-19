/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.rdf.model.Model;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.view.View;
import frontend.controller.FrontEndResource;
import frontend.controller.form.ReportRDFForm;
import frontend.controller.resource.FrontPageResource;
import frontend.view.report.ReportCreateView;
import frontend.view.report.ReportListView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
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
	    // saving== should
            if (request.getParameter("action") != null && request.getParameter("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void query(HttpServletRequest request, HttpServletResponse response)
    {
	ReportRDFForm form = new ReportRDFForm(request);

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
	ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	//arq2Spin.setVarNamespace("http://www.semanticreports.com/queries/");
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, "http://temp.com/query/123"); // change to query URI
       
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        Model model = form.getModel();
        model.add(form.getReportResource(), model.createProperty(DublinCore.DATE), model.createTypedLiteral(c));
        model.add(form.getReportResource(), model.createProperty(DublinCore.CREATOR), model.createResource("http://rdfs.org/sioc/ns#User/RandomUserName"));

        SDB.getInstanceModel().add(form.getModel()); // save report
	//SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);

        try {
            // save report
            //SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);
            response.sendRedirect(form.getReportResource().getURI());
        } catch (IOException ex) {
            Logger.getLogger(ReportListResource.class.getName()).log(Level.SEVERE, null, ex);
        }
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
