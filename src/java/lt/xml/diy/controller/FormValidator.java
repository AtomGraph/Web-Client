/*
 * FormValidator.java
 *
 * Created on Šestadienis, 2007, Kovo 10, 15.14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

/**
 *
 * @author Pumba
 */
public final class FormValidator
{
    
    public static final boolean isEmpty(String string)
    {
	return (string.length() == 0);
    }
    
    public static final boolean isFloat(String string)
    {
        try
	{ 
            Float.parseFloat(string); 
            return true;
        }
        catch (NumberFormatException e)
	{
            return false;
        }	
    }
}
