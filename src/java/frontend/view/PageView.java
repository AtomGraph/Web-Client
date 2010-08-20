/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view;

import frontend.controller.resource.PageResource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
public class PageView extends FrontEndView
{

    public PageView(PageResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
        super(resource);
        setStyleSheet(getController().getServletContext().getResourceAsStream(getStyleSheetPath()), getController().getServletContext().getResource(XSLT_PATH).toURI().toString());
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setDocument("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"/>");

        super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

}
