package lt.xml.diy.view;

import lt.xml.diy.controller.ResourceImpl;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;

public abstract class View
{
    private Servlet servlet = null;
    private ResourceImpl resource = null;
    
    public View(ResourceImpl resource)
    {
	this.resource = resource;
    }

    public ResourceImpl getResource()
    {
	return resource;
    }

    public void setResource(ResourceImpl resource)
    {
	this.resource = resource;
    }

    public Servlet getServlet()
    {
	return servlet;
    }
    
    public void setServlet(Servlet servlet)
    {
	this.servlet = servlet;
    }

    public abstract void display(HttpServletRequest request, HttpServletResponse response)  throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException;
    
}
