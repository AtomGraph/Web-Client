package lt.xml.diy.view;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;
import lt.xml.diy.controller.Resource;

public abstract class View
{
    private Servlet servlet = null;
    private Resource resource = null;
    
    public View(Resource resource)
    {
	this.resource = resource;
    }

    public Resource getResource()
    {
	return resource;
    }

    public void setResource(Resource resource)
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
