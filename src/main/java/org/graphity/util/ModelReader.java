/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.graphity.form.RDFForm;

/**
 *
 * @author Pumba
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class ModelReader implements MessageBodyReader<Model>
{
    @Context private HttpServletRequest request;
    private Model model = ModelFactory.createDefaultModel();
    private List<String> keys = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();
    private List<Exception> errors = new ArrayList<Exception>();

    public ModelReader()
    {
	initParamMap();
	initModel();
    }

    private void initParamMap()
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

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	return model;
    }
    
}
