/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import dk.semantic_web.diy.controller.Error;
import model.vocabulary.DublinCore;
import model.vocabulary.Reports;
import model.vocabulary.Spin;

/**
 *
 * @author Pumba
 */
public class ReportRDFForm extends RDFForm 
{

    public ReportRDFForm(HttpServletRequest request)
    {
	super(request);
    }

    public Resource getReportResource()
    {
        Resource report = null;
        Resource reportClass = getModel().createResource(Reports.Report);
        ResIterator iter = getModel().listResourcesWithProperty(RDF.type, reportClass);
        if (iter.hasNext())
            report = iter.next();
        return report;
    }

    public Resource getQueryResource()
    {
        Resource query = null;
        Resource queryClass = getModel().createResource(Spin.Select);
        ResIterator iter = getModel().listResourcesWithProperty(RDF.type, queryClass);
        if (iter.hasNext()) query = iter.next();
        return query;
    }

    public Resource getEndpointResource()
    {
	Resource endpoint = null;
	Property fromProperty = getModel().createProperty(Spin.from);
	Statement stmt = getModel().getProperty(getQueryResource(), fromProperty);
	if (stmt != null) endpoint = stmt.getResource();
	return endpoint;
    }
    
    public String getQueryString()
    {
	String queryString = null;
	Property textProperty = getModel().createProperty(Spin.text);
	Statement stmt = getModel().getProperty(getQueryResource(), textProperty);
	if (stmt != null) queryString = stmt.getString();
	return queryString;
    }

    public String getTitle()
    {
	String title = null;
	Property titleProperty = getModel().createProperty(DublinCore.TITLE);
	Statement stmt = getModel().getProperty(getReportResource(), titleProperty);
	if (stmt != null) title = stmt.getString();
	return title;
    }

    @Override
    public List<Error> validate()
    {
        //if (getTitle() == null || getTitle().equals("")) getErrors().add(new Error("noTitle"));
        if (getEndpointResource() == null) getErrors().add(new Error("noEndpoint"));
        if (getQueryString() == null || getQueryString().equals("")) getErrors().add(new Error("noQueryString"));

        return getErrors();
    }

    public List<Error> validateWithTitle()
    {
        if (getTitle() == null || getTitle().equals("")) getErrors().add(new Error("noTitle"));
        if (getEndpointResource() == null) getErrors().add(new Error("noEndpoint"));
        if (getQueryString() == null || getQueryString().equals("")) getErrors().add(new Error("noQueryString"));

        return getErrors();
    }

}
