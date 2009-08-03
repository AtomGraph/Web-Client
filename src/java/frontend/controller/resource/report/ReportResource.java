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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import frontend.controller.resource.FrontPageResource;
import java.net.Authenticator;
import java.net.URL;
import org.apache.commons.codec.digest.DigestUtils;
import util.DigestAuthenticator;

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
	    
	    if (request.getParameter("action") != null && request.getParameter("action").equals("save")) save(request, response);
	}

	return view;
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
    {
	String queryString = request.getParameter("query-string");
	String username = "mjusevicius";
	String password = "n7dx2grc";
	
	try
	{
	    URL itemsUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/items");

	    Authenticator.setDefault(new DigestAuthenticator());
	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(itemsUrl.getHost());
	    remoteRequest.setPathInfo(itemsUrl.getPath());
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    private void save1(HttpServletRequest request, HttpServletResponse response)
    {
	String queryString = request.getParameter("query-string");
	String username = "mjusevicius";
	String password = "n7dx2grc";
	
	try
	{
	    URL itemsUrl = new URL("http://api.talis.com/stores/mjusevicius-dev1/items");

	    dk.semantic_web.diy.http.HttpRequest remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(itemsUrl.getHost());
	    remoteRequest.setPathInfo(itemsUrl.getPath());
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	    
	    String authHeader = remoteResponse.getHeader("WWW-Authenticate");
	    authHeader = authHeader.substring("Digest ".length());
	    System.out.println("STATUS: " + remoteResponse.getStatus());
	    System.out.println("WWW-Authenticate: " + authHeader);
	    
	    String[] fields = authHeader.split(", ");
	    String nonce = fields[2];
	    nonce = nonce.substring("nonce=\"".length(), nonce.length() - 1);
	    System.out.println("nonce: " + nonce);
	    
	    String realm = "bigfoot";
	    String qop = "auth";
	    String nc = "";
	    String cnonce = "whatever";

	    String ha1 = DigestUtils.md5Hex(username + ":" + realm + ":" + password);
	    String ha2 = DigestUtils.md5Hex("POST:" + itemsUrl.getPath());
	    String responseDigest = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);

	    /*
	    remoteRequest = new dk.semantic_web.diy.http.HttpRequest();
	    remoteRequest.setMethod("post");
	    remoteRequest.setServerName(itemsUrl.getHost());
	    remoteRequest.setPathInfo(itemsUrl.getPath());
	    remoteRequest.setQueryString(itemsUrl.getQuery());
	    remoteRequest.setHeader("Content-Type", "text/plain");
	    remoteRequest.setHeader("Authorization", "Digest ");
	    String md5 = DigestUtils.md5Hex("mjusevicius:n7dx2grc");
		    
	    HttpResponse remoteResponse = HttpClient.send(remoteRequest);
	     */
	    
	    /*
	    InputStream stream = remoteResponse.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) sb.append(line + "\n");
	    stream.close();
	    String responseString = sb.toString();
	    System.out.println("QUERY: " + itemsUrl.getQuery());
	    System.out.println("RESPONSE: " + responseString);
	     */
	    
	} catch (IOException ex)
	{
	    Logger.getLogger(ReportResource.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
}
