/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.resource.report;

import dk.semantic_web.diy.controller.Resource;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.diy.http.HttpClient;
import dk.semantic_web.diy.http.HttpResponse;
import frontend.controller.FrontEndResource;
import frontend.view.report.ReportView;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.resource.FrontPageResource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import util.Base64Coder;

/**
 *
 * @author Pumba
 */
public class ReportResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "";
    private static final ReportResource INSTANCE = new ReportResource(FrontPageResource.getInstance());
    
    private View view = null;
    
    public ReportResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static Resource getInstance()
    {
	return INSTANCE;
    }
    
    public String getRelativeURI()
    {
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else view = new ReportView(this);

	return view;
    }

    @Override
    public View doPost(HttpServletRequest request, HttpServletResponse response)
    {
	View parent = super.doPost(request, response);
	if (parent != null) view = parent;
	else
	{
	    view = new ReportView(this);
	    
	    if (request.getParameter("action") != null && request.getAttribute("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
    {
	String queryString = request.getParameter("query-string");

	try
	{
	    
	    URL resultUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/items");

	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(resultUrl.getHost());
	    remoteRequest.setPathInfo(resultUrl.getPath());
	    remoteRequest.setQueryString(resultUrl.getQuery());
	    remoteRequest.setHeader("Content-Type", "text/plain");
	    remoteRequest.setHeader("Authorization", "Digest " + Base64Coder.encodeString("mjusevicius:n7dx2grc"));
	    
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	    //InputStreamReader reader = new InputStreamReader(;
	    InputStream stream = remoteResponse.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) sb.append(line + "\n");
	    stream.close();
	    String responseString = sb.toString();
	    System.out.println("QUERY: " + resultUrl.getQuery());
	    System.out.println("RESPONSE: " + responseString);
	} catch (NoSuchAlgorithmException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
}
