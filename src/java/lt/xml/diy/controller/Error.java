/*
 * Error.java
 *
 * Created on Tre�iadienis, 2007, Sausio 31, 13.15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

/**
 * Represents an error in the form data, usually a failure to meet a data type or constraint.
 * @author Pumba
 */
public class Error
{
    private String URI = null;
    /**
	 * Creates a new instance of Error
	 */
    public Error(String URI)
    {
	this.URI = URI;
    }
    
    public String getURI()
    {
	return URI;
    }
    
}
