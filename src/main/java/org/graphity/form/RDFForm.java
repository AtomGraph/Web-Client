/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.form;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import org.graphity.Form;

/**
 *
 * @author Pumba
 */
public class RDFForm implements Request, Form
{
    private Model model = ModelFactory.createDefaultModel();
    private List<String> keys = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();
    private List<Exception> errors = new ArrayList<Exception>();

    public RDFForm(HttpServletRequest request)
    {
	super(request);

	initParamMap(request);
	initModel();
    }

    private void initParamMap(HttpServletRequest request)
    {
	// using getQueryString() even with POST method because request.getParameterNames() does not guarantee order
	String queryString = request.getQueryString();
	String[] params = queryString.split("&");

	for (String param : params)
	{
	    System.out.println(param);
	    String[] array = param.split("=");
	    String key = null;
	    String value = null;

	    try
	    {
		key = URLDecoder.decode(array[0], "UTF-8");
		if (array.length > 1) value = URLDecoder.decode(array[1], "UTF-8");
	    } catch (UnsupportedEncodingException ex)
	    {
		Logger.getLogger(RDFForm.class.getName()).log(Level.SEVERE, null, ex);
	    }

            if (value != null) // && key != null
            {
                keys.add(key);
                values.add(value);
            }
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
	    if (keys.get(i).equals("n") && keys.get(i + 1).equals("v"))
            {
                model.setNsPrefix(values.get(i), values.get(i + 1));
                i++;
            } // namespace with prefix
	    if (keys.get(i).equals("sb") || keys.get(i).equals("su") || keys.get(i).equals("sv") || keys.get(i).equals("sn"))
	    {
		property = null; object = null;
		if (keys.get(i).equals("sb")) subject = model.createResource(new AnonId(values.get(i))); // blank node
		if (keys.get(i).equals("su")) subject = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("sv")) subject = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("sn") && keys.get(i + 1).equals("sv"))
                {
                    subject = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
	    }
	    if (keys.get(i).equals("pu") || keys.get(i).equals("pv") || keys.get(i).equals("pn") || keys.get(i).equals("sn"))
	    {
		object = null;
		if (keys.get(i).equals("pu")) property = model.createProperty(values.get(i));
		if (keys.get(i).equals("pv")) property = model.createProperty(model.getNsPrefixURI(""), values.get(i));
		if (keys.get(i).equals("pn") && keys.get(i + 1).equals("pv"))
                {
                    property = model.createProperty(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
	    }
	    if (keys.get(i).equals("ob") || keys.get(i).equals("ou") || keys.get(i).equals("ov") || keys.get(i).equals("on") || keys.get(i).equals("ol"))
	    {
		if (keys.get(i).equals("ob")) object = model.createResource(new AnonId(values.get(i))); // blank node
		if (keys.get(i).equals("ou")) object = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("ov")) object = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("on") && keys.get(i + 1).equals("ov"))
                {
                    object = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
		if (keys.get(i).equals("ol")) object = model.createTypedLiteral(values.get(i)); // literal

	    }

	    if (subject != null && property != null && object != null)
	    {
		stmt = model.createStatement(subject, property, object);
		model.add(stmt);
	    }
	}
	//model.write(System.out);
    }

    public Model getModel()
    {
	return model;
    }

    @Override
    public List<Exception> validate()
    {
        return errors;
    }

    @Override
    public String getMethod()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Variant selectVariant(List<Variant> variants) throws IllegalArgumentException
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResponseBuilder evaluatePreconditions(EntityTag eTag)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResponseBuilder evaluatePreconditions()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
