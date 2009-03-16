/*
 * Form.java
 *
 * Created on Pirmadienis, 2007, Sausio 29, 13.34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

import javax.servlet.http.*;
import java.util.List;
import javax.servlet.Servlet;

/**
 * Represents form data submitted with a HTTP request.
 * @author Pumba
 */
public abstract class Form
{
    
    public Form(HttpServletRequest request)
    {
		
    }
    
    protected Servlet servlet = null;
    
    /** Sets the instance of the servlet this form is related to. */

    public void setServlet(Servlet servlet)
    {
	this.servlet = servlet;
    }    
    
    /** Validates the form data (submitted with the request) against constraints
    @return The list of errors (constraints not met)
    */
    public abstract List<Error> validate();
}
