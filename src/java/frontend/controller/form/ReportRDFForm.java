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
import javax.servlet.http.HttpServletRequest;
import model.Namespaces;

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
        Resource reportClass = getModel().createResource(Namespaces.REPORT_NS + "Report");
        ResIterator iter = getModel().listResourcesWithProperty(RDF.type, reportClass);
        if (iter.hasNext())
            report = iter.next();
        return report;
    }

    public Resource getQueryResource()
    {
        Resource query = null;
        Resource queryClass = getModel().createResource(Namespaces.SPIN_NS + "Select");
        ResIterator iter = getModel().listResourcesWithProperty(RDF.type, queryClass);
        if (iter.hasNext())
            query = iter.next();
        return query;
    }

    public String getEndpoint()
    {
	String endpoint = null;
	Property fromProperty = getModel().createProperty(Namespaces.SPIN_NS, "from");
	Statement stmt = getModel().getProperty(getQueryResource(), fromProperty);
	if (stmt.getObject() != null) endpoint = stmt.getObject().toString();
	return endpoint;
    }
    
    public String getQueryString()
    {
	String queryString = null;
	Property textProperty = getModel().createProperty(Namespaces.SPIN_NS, "text");
	Statement stmt = getModel().getProperty(getQueryResource(), textProperty);
	if (stmt.getObject() != null) queryString = stmt.getString();
	return queryString;
    }
}
