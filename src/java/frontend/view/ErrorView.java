/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view;

import frontend.controller.FrontEndResource;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
public class ErrorView extends FrontEndView
{

    public ErrorView(FrontEndResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/ErrorView.xsl")));

	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");
	
	getTransformer().setParameter("error-message", request.getAttribute("error-message"));
	getTransformer().setParameter("stack-trace", request.getAttribute("stack-trace"));
	
	super.display(request, response);
	
	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
