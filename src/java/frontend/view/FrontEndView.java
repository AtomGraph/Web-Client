/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view;

import controller.Controller;
import frontend.controller.FrontEndResource;
import dk.semantic_web.diy.view.XSLTView;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
abstract public class FrontEndView extends XSLTView
{
  
    public FrontEndView(FrontEndResource resource)
    {
	super(resource);
    }

    public FrontEndView(Controller controller)
    {
	super(controller);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	long startTime = System.currentTimeMillis();

	getTransformer().setParameter("relative-uri", (String)request.getAttribute("uri"));
	getTransformer().setParameter("host-uri", getController().getMapping().getHost());
	//getTransformer().setParameter("host-uri", "http://semanticreports.com/"); // QUIRK!!!
	//getTransformer().setParameter("host-uri", "http://localhost/");
	getTransformer().setParameter("view", getClass().getName());
	getTransformer().setParameter("browser", request.getHeader("User-Agent"));

	response.setCharacterEncoding("UTF-8");
	//response.setContentType("application/xhtml+xml");
	response.setContentType("text/html");
	super.display(request, response);

	long endTime = System.currentTimeMillis();
	System.out.println("XSLT transformation duration: " + (endTime - startTime) + "ms");
    }
    
}
