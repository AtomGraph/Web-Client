package lt.xml.diy.controller;

import lt.xml.diy.view.View;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 Central controller, single point of entry to the application.
 Fills {@link controller.ActionForm ActionForms}, performs {@link controller.Action Actions} and displays {@link view.View Views}.
 */

abstract public class Controller extends HttpServlet
{
    private ResourceImpl resource = null;
    private View view = null;
    private ResourceMapping mapping = null;
    
    private String getFullURI(HttpServletRequest request)
    {
        String scheme = request.getScheme();
        String host = request.getHeader("host"); //localhost
        String path = request.getRequestURI();
        String uri = scheme + "://" + host + path;

        return uri;
    }

    private String getPath(HttpServletRequest request)
    {
        String path = request.getRequestURI();
	String context = request.getContextPath() + "/";
	path = path.substring(context.length());

        return path;
    }

    protected void process(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	//String uri = getFullURI(request); // absolute
	String uri = getPath(request); // relative
	
	request.setAttribute("uri", uri);
	resource = mapping.findByURI(uri);

	view = null;
	if (resource != null)
	{
	    resource.setController(this);
	    
	    if (request.getMethod().equalsIgnoreCase("get")) view = resource.doGet(request, response);
	    else if (request.getMethod().equalsIgnoreCase("post")) view = resource.doPost(request, response);
	    else if (request.getMethod().equalsIgnoreCase("put")) view = resource.doPut(request, response);
	    else if (request.getMethod().equalsIgnoreCase("delete")) view = resource.doDelete(request, response);
	    else response.setStatus(response.SC_METHOD_NOT_ALLOWED);
	}
    }
    
    public void setMapping(ResourceMapping mapping)
    {
	this.mapping = mapping;
    }
    
    public ResourceMapping getMapping()
    {
	return mapping;
    }

    public void setResource(ResourceImpl resource)
    {
	this.resource = resource;
    }
    
    public ResourceImpl getResource()
    {
	return resource;
    }

    public View getView()
    {
	return view;
    }

    public void setView(View view)
    {
	this.view = view;
    }
    
}
