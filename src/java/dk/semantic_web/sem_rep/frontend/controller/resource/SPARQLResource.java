/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource;

import dk.semantic_web.diy.view.View;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import dk.semantic_web.sem_rep.frontend.view.SPARQLResultView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

/**
 *
 * @author Pumba
 */
public class SPARQLResource extends FrontEndResource
{
    private static final String RELATIVE_URI = "sparql";
    private static final SPARQLResource INSTANCE = new SPARQLResource(FrontPageResource.getInstance());

    public SPARQLResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static SPARQLResource getInstance()
    {
	return INSTANCE;
    }

    @Override
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(RELATIVE_URI, "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(SPARQLResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

        return new SPARQLResultView(this);
    }

}
