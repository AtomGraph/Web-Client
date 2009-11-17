/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
import controller.LeafResource;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportReadView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.form.ReportForm;
import frontend.view.report.ReportUpdateView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import model.Report;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource implements LeafResource
{
    private View view = null;
    private Report report = null;
    
    public ReportResource(Report report, ReportListResource parent)
    {
	super(parent);
	setReport(report);
    }
    
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(getReport().getId(), "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return getReport().getId();
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
	else
	{
	    view = new ReportReadView(this);
	    
	    if (request.getParameter("view") != null && request.getParameter("view").equals("update")) view = new ReportUpdateView(this);
	}

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
	    
	    if (request.getParameter("action") != null && request.getParameter("action").equals("update")) update(request, response);
	}

	return view;
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
    {
	ReportForm form = new ReportForm(request);

	SPINModuleRegistry.get().init();
	Model queryModel = ModelFactory.createDefaultModel();
	//queryModel.setNsPrefix("rdf", RDF.getURI());
	//queryModel.setNsPrefix("ex", "http://example.org/demo#");
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(queryModel, form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(queryModel);
	//arq2Spin.setVarNamespace("http://www.semanticreports.com/queries/");
	Select spinQuery = (Select) arq2Spin.createQuery(arqQuery, "http://www.semanticreports.com/queries/");
	queryModel.write(System.out, FileUtils.langXMLAbbrev);

	/*
	OntModel model = ModelFactory.createOntologyModel();
	Jenabean.instance().bind(model);

	Query query = new Query();
	query.setQueryString(queryString);

	Report report = new Report();
	report.setTitle(title);
	report.setQuery(query);
	report.setCreatedAt(new Date());
	report.resource = this; //report.setFrontEndResource(this);
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

System.out.println(model.toString());
	    model.write(remoteRequest.getOutputStream());
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	*/
    }

}
