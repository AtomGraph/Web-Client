/*
 * NotFoundView.java
 *
 * Created on Trečiadienis, 2007, Sausio 31, 22.26
 *
 * To change this template, choose Tools | Template Manager
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
public class NotFoundView extends FrontEndView
{
    
    public NotFoundView()
    {
	this(null);
    }
    
    public NotFoundView(FrontEndResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException
    {
	setStyleSheet(new File(getServlet().getServletConfig().getServletContext().getRealPath("/xslt/NotFoundView.xsl")));

	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");
	
	super.display(request, response);
	
	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    
}
