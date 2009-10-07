/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import controller.ResourceMapping;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Form;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import model.Namespaces;

/**
 *
 * @author Pumba
 */
public class RDFForm extends Form
{
    Model model = ModelFactory.createDefaultModel();
    //Resource visualization = null;
    //LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
    
    public RDFForm(HttpServletRequest request)
    {
	super(request);
	
	initParamMap(request);
	initModel();
    }

    private void initParamMap(HttpServletRequest request)
    {
	String queryString = request.getQueryString();
	String[] params = queryString.split("&");

	for (String param : params)
	{
	    System.out.println(param);
	    String[] array = param.split("=");
	    String key = ResourceMapping.urlDecode(array[0]);
	    String value = null;
	    if (array.length > 1) value = ResourceMapping.urlDecode(array[1]);

	    keys.add(key);
	    values.add(value);
	    //model.createResource(param)
	    //model.set
	}	
    }
    
    // http://www.lsrn.org/semweb/rdfpost.html
    private void initModel()
    {
	Statement stmt = null;
	Resource subject = null;
	Property property = null;
	RDFNode object = null;

	for (int i = 0; i < keys.size(); i++)
	{
	    if (keys.get(i).equals("v")) model.setNsPrefix("", values.get(i)); // default namespace
	    if (keys.get(i).equals("n") && keys.get(i + 1).equals("v")) model.setNsPrefix(values.get(i), values.get(i + 1)); // namespace with prefix
	    if (keys.get(i).equals("sb") || keys.get(i).equals("su") || keys.get(i).equals("sv") || keys.get(i).equals("sn"))
	    {
		property = null; object = null;
		if (keys.get(i).equals("sb")) subject = model.createResource(); // blank node
		if (keys.get(i).equals("su")) subject = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("sv")) subject = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("sn") && keys.get(i + 1).equals("sv")) subject = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
	    }
	    if (keys.get(i).equals("pu") || keys.get(i).equals("pv") || keys.get(i).equals("pn") || keys.get(i).equals("sn"))
	    {
		object = null;
		if (keys.get(i).equals("pu")) property = model.createProperty(values.get(i));
		if (keys.get(i).equals("pv")) property = model.createProperty(model.getNsPrefixURI(""), values.get(i));
		if (keys.get(i).equals("pn") && keys.get(i + 1).equals("pv")) property = model.createProperty(values.get(i), values.get(i + 1));
	    }
	    if (keys.get(i).equals("ob") || keys.get(i).equals("ou") || keys.get(i).equals("ov") || keys.get(i).equals("on") || keys.get(i).equals("ol"))
	    {
		if (keys.get(i).equals("ob")) object = model.createResource(); // blank node
		if (keys.get(i).equals("ou")) object = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("ov")) object = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("on") && keys.get(i + 1).equals("ov")) object = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
		if (keys.get(i).equals("ol")) object = model.createLiteral(values.get(i)); // literal
		
	    }
	    
	    if (subject != null && property != null && object != null)
	    {
		stmt = model.createStatement(subject, property, object);
		model.add(stmt);
	    }
	}
	model.write(System.out);
    }
    
    public Model getModel()
    {
	return model;
    }

    public Resource getQueryResource()
    {
	Resource query = null;
	Resource queryClass = getModel().createResource(Namespaces.SPIN_NS + "Select");
	Statement stmt = getModel().getProperty(queryClass, RDF.type);
	if (stmt.getObject() != null) query = (Resource)stmt.getObject();
	return query;
    }
    
    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
