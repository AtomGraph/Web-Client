/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view;

import com.hp.hpl.jena.query.QueryException;
import dk.semantic_web.diy.view.View;
import frontend.controller.resource.SPARQLResource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.SDB;
import view.QueryResult;

/**
 *
 * @author Pumba
 */
public class SPARQLResultView extends View
{

    public SPARQLResultView(SPARQLResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
	String queryString = request.getParameter("query");
	String format = request.getParameter("output");

	Logger.getLogger(SPARQLResultView.class.getName()).log(Level.INFO, queryString);

	try
	{
	    // add content negotiation with Accept headers

	    response.setCharacterEncoding("UTF-8");

	    if (format == null || format.equals("xml") || format.equals("sparql"))
	    {
		response.setContentType("application/sparql-results+xml");
		QueryResult.selectXMLToStream(SDB.getDataset(), queryString, response.getOutputStream());
	    }
	    if (format != null && format.equals("json"))
	    {
		response.setContentType("application/sparql-results+json");
		QueryResult.selectJSONToStream(SDB.getDataset(), queryString, response.getOutputStream());
	    }
	    
	    //response.setStatus(HttpServletResponse.SC_OK);
	    response.setStatus(123);

	} catch (QueryException ex)
	{
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    Logger.getLogger(SPARQLResultView.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

}
