package controller;

import frontend.view.NotFoundView;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import model.SDB;
import view.*;

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
	
        SDB.init(getServletContext());
    }
    
    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        //System.out.println("Character encoding: " + request.getCharacterEncoding());
        request.setCharacterEncoding("UTF-8");
	setHost(request, response);
	
        super.process(request, response);

        //if (request.getAttribute("uri").equals("")) response.sendRedirect("s%C3%B8gning"); // make groups the default view

        if (getResource() == null) setView(new NotFoundView());

        if (getView() != null)
        try
        {
            getView().setController(this);
            getView().display(request, response);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            /*
            System.out.println(ex.getMessage());
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            String stackTrace = writer.toString();

            view = new ErrorView(resource);
            view.setServlet(this);
            request.setAttribute("error-message", ex.getMessage());
            request.setAttribute("stack-trace", stackTrace);

            try
            {
            view.display(request, response);
            } catch (TransformerConfigurationException ex1)
            {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (TransformerException ex1)
            {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (ParserConfigurationException ex1)
            {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex1);
            }
             */
        }
    }
    
   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

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
	host += request.getContextPath();
	host += "/";	
	getMapping().setHost(host);
    }
    
    // </editor-fold>
}
