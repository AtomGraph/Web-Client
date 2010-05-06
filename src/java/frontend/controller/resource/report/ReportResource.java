/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import controller.LeafResource;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportReadView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.form.ReportRDFForm;
import frontend.view.report.ReportUpdateView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import model.vocabulary.DublinCore;
import model.Report;
import model.SDB;
import model.vocabulary.Sioc;
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
	ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, form.getQueryResource().getURI()); // change to query URI

        // add some metadata
        String userUri = getController().getMapping().getHost() + "users/pumba";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Model model = form.getModel();
        model.add(form.getReportResource(), model.createProperty(DublinCore.MODIFIED), model.createTypedLiteral(calendar));
        model.add(form.getReportResource(), model.createProperty(DublinCore.CREATOR), model.createResource(userUri));
        model.add(model.createResource(userUri), RDF.type, model.createResource(Sioc.USER));
        //model.add(model.createResource(userUri), model.createProperty(DublinCore.DATE), model.createTypedLiteral(calendar));
        model.add(model.createResource(userUri), model.createProperty(Sioc.NAME), model.createTypedLiteral("RandomUserName"));

        Model intersection = SDB.getInstanceModel().intersection(model);
        System.out.print("INTERSECTION: " + intersection);
        model = model.difference(intersection);
        
        SDB.getInstanceModel().add(model); // save report
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

}
