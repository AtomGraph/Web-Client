/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Form;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pumba
 */
public class RDFForm extends Form
{
    Model model = ModelFactory.createDefaultModel();
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
	    String key = param.split("/")[0];
	    String value = param.split("/")[1];

	    keys.add(key);
	    values.add(value);
	    //model.createResource(param)
	    //model.set
	}	
    }
    
    // http://www.lsrn.org/semweb/rdfpost.html
    private void initModel()
    {
	for (int i = 0; i < keys.size(); i++)
	{
	    if (keys.get(i).equals("v")) model.setNsPrefix("", values.get(i)); // default namespace
	    if (keys.get(i).equals("n") && keys.get(i + 1).equals("v")) model.setNsPrefix(values.get(i), values.get(i + 1)); // namespace with prefix
	    if (keys.get(i).equals("sb") || keys.get(i).equals("su") || keys.get(i).equals("sv") || keys.get(i).equals("sn"))
	    {
		Resource subject = null;
		if (keys.get(i).equals("sb")) subject = model.createResource(); // blank node
		if (keys.get(i).equals("su")) subject = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("sv")) subject = model.createResource(values.get(i)); // (default namespace) + local name?
		if (keys.get(i).equals("sn") && keys.get(i + 1).equals("sv")) subject = model.createResource(model.getNsPrefixURI(values.get(i) + values.get(i + 1))); // ns prefix + local name
	    }
	    if (keys.get(i).equals("pu") || keys.get(i).equals("pv") || keys.get(i).equals("pn") || keys.get(i).equals("sn"))
	    {
		
	    }

	}
    }
    
    public Model getModel()
    {
	
	return null;
    }
    
    @Override
    public List<Error> validate()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
