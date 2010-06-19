/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import controller.LeafResource;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportReadView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.form.CommentRDFForm;
import frontend.controller.form.ReportRDFForm;
import frontend.view.report.ReportUpdateView;
import frontend.view.report.ReportView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.transform.TransformerConfigurationException;
import model.vocabulary.DublinCore;
import model.Report;
import model.SDB;
import model.vocabulary.Sioc;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import view.QueryXMLResult;

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
    
    @Override
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
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
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
    public View doPost(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	View parent = super.doPost(request, response);
	if (parent != null) view = parent;
	else
	{
	    view = new ReportReadView(this);
	    
	    if (request.getParameter("action") != null && request.getParameter("action").equals("update")) update(request, response);
	    if (request.getParameter("action") != null && request.getParameter("action").equals("comment")) comment(request, response);
        }

	return view;
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
    {
	ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, form.getQueryResource().getURI()); // change to query URI

        // add some metadata
        String userUri = getController().getMapping().getHost() + "users/pumba";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Model newModel = form.getModel();
        newModel.add(form.getReportResource(), newModel.createProperty(DublinCore.MODIFIED), newModel.createTypedLiteral(calendar));
        newModel.add(form.getReportResource(), newModel.createProperty(DublinCore.CREATOR), newModel.createResource(userUri));
        newModel.add(form.getReportResource(), RDF.type, newModel.createResource(Sioc.FORUM));
        newModel.add(newModel.createResource(userUri), RDF.type, newModel.createResource(Sioc.USER));
        newModel.add(newModel.createResource(userUri), newModel.createProperty(Sioc.NAME), newModel.createTypedLiteral("RandomUserName"));
//newModel.write(System.out, FileUtils.langXMLAbbrev);

Resource reportResource = SDB.getInstanceModel().createResource(form.getReportResource().getURI());
Resource endpointResource = SDB.getInstanceModel().createResource(form.getEndpointResource().getURI());
System.out.println("Report resource: " + reportResource);
System.out.println("Endpoint resource: " + endpointResource);

Model oldModel = ResourceUtils.reachableClosure(reportResource);
List<Statement> keepStatements = new ArrayList<Statement>();
keepStatements.add(oldModel.getProperty(reportResource, oldModel.createProperty(DublinCore.CREATED)));
keepStatements.add(oldModel.getProperty(endpointResource, RDF.type));
keepStatements.add(oldModel.getProperty(endpointResource, oldModel.createProperty(DublinCore.TITLE))); // can be null!
oldModel.remove(keepStatements); // do not delete creation date, endpoint metadata etc.
//oldModel.write(System.out, FileUtils.langXMLAbbrev);
        SDB.getInstanceModel().remove(oldModel);
        SDB.getInstanceModel().add(newModel);

        //SDB.getInstanceModel().add(model); // save report
	//SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);
//form.getModel().write(System.out);

        try {
            // save report
            //SDB.getDefaultModel().write(System.out, FileUtils.langXMLAbbrev);
            response.sendRedirect(form.getReportResource().getURI());
        } catch (IOException ex) {
            Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void comment(HttpServletRequest request, HttpServletResponse response)
    {
	CommentRDFForm form = new CommentRDFForm(request);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        form.getModel().add(form.getCommentResource(), form.getModel().createProperty(DublinCore.CREATED), form.getModel().createTypedLiteral(calendar));

        SDB.getInstanceModel().add(form.getModel());
    }
}
