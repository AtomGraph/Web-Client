package controller;

import frontend.view.NotFoundView;
import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

import model.SDB;

/**
 Central controller, single point of entry to the application.
 Fills {@link controller.ActionForm ActionForms}, performs {@link controller.Action Actions} and displays {@link view.View Views}.
 */

public class Controller extends dk.semantic_web.diy.controller.Controller
{
    @Override
    public void init()
    {
        setMapping(new controller.ResourceMapping());

	System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        SDB.init(getServletContext());
    }
    
    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        //System.out.println("Character encoding: " + request.getCharacterEncoding());
        request.setCharacterEncoding("UTF-8");
	setHost(request, response);

        if (request.getMethod().equalsIgnoreCase("post")) request = new PostRequestWrapper(request); // IMPORTANT! otherwise one can only use request.getParameter() OR request.getInputStream()

        try
        {
            super.process(request, response);

            //if (request.getAttribute("uri").equals("")) response.sendRedirect("/reports/"); // make reports the default view

            if (getResource() == null) setView(new NotFoundView(this));
            if (getView() != null) getView().display(request, response);
        }
        catch (Exception ex)
        {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);

            ex.printStackTrace(response.getWriter());
            //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());

            /*
            System.out.println(ex.getMessage());
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            String stackTrace = writer.toString();

            view = new ErrorView(this);
            request.setAttribute("error-message", ex.getMessage());
            request.setAttribute("stack-trace", stackTrace);
            */
        }
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        process(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        process(request, response);
    }

    private void setHost(HttpServletRequest request, HttpServletResponse response)
    {
	String host = "http://" + request.getServerName();
	if (request.getServerPort() != 80) host += ":" + request.getServerPort();
	//host += request.getContextPath();
	host += "/";	
	getMapping().setHost(host);
    }
}
