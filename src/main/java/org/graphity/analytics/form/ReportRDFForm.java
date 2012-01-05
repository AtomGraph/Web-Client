/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.analytics.form;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.graphity.form.RDFForm;
import org.graphity.analytics.model.vocabulary.DublinCore;
import org.graphity.analytics.model.vocabulary.Reports;
import org.graphity.analytics.model.vocabulary.Spin;
import org.graphity.analytics.util.IDGenerator;

/**
 *
 * @author Pumba
 */
public class ReportRDFForm extends RDFForm 
{
    public ReportRDFForm(HttpServletRequest request)
    {
	super(request);

	// override endpoint & query if used as a proxy
	if (request.getParameter("endpoint") != null && request.getParameter("query") != null)
	{
	    //Controller.getHost(request)
	    String reportUri = "reports/" + IDGenerator.generate();
	    String queryUri = "queries/" + IDGenerator.generate();
	    String endpointUri = request.getParameter("endpoint");

	    Resource reportResource = getModel().createResource(reportUri);
	    Resource queryResource = getModel().createResource(queryUri);
	    Resource endpointResource = getModel().createResource(endpointUri);
	    
	    getModel().add(reportResource, RDF.type, getModel().createResource(Reports.Report));
	    getModel().add(queryResource, RDF.type, getModel().createResource(Spin.Select));
	    getModel().add(endpointResource, RDF.type, getModel().createResource(Reports.Endpoint));

	    getModel().add(reportResource, getModel().createProperty(Reports.query), queryResource);
	    getModel().add(queryResource, getModel().createProperty(Spin.from), endpointResource);
	    getModel().add(queryResource, getModel().createProperty(Spin.text), getModel().createTypedLiteral(request.getParameter("query")));
	}
    }

    public Resource getReport()
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

    public Resource getEndpoint()
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
	Statement stmt = getModel().getProperty(getReport(), titleProperty);
	if (stmt != null) title = stmt.getString();
	return title;
    }

    public String getEndpointTitle()
    {
	String title = null;
	Property titleProperty = getModel().createProperty(DublinCore.TITLE);
	Statement stmt = getModel().getProperty(getEndpoint(), titleProperty);
	if (stmt != null) title = stmt.getString();
	return title;
    }

    @Override
    public List<Exception> validate()
    {
        //if (getTitle() == null || getTitle().equals("")) getErrors().add(new Error("noTitle"));
	/*
        if (getEndpoint() == null) errors.add(new Error("noEndpoint"));
        if (getQueryString() == null || getQueryString().equals("")) errors.add(new Error("noQueryString"));

        return errors;
	 */
	return null;
    }

    public List<Error> validateWithTitle()
    {
	/*
        if (getTitle() == null || getTitle().equals("")) errors.add(new Error("noTitle"));
        if (getEndpoint() == null) errors.add(new Error("noEndpoint"));
        if (getQueryString() == null || getQueryString().equals("")) errors.add(new Error("noQueryString"));

        return errors;
	 */
	return null;
    }

}
