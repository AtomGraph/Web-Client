/*
 * Resource.java
 *
 * Created on Ketvirtadienis, 2007, Kovo 29, 17.50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lt.xml.diy.view.View;

/**
 *
 * @author Pumba
 */
abstract public class ResourceImpl implements Resource
{
    private Resource parent = null;
    private Controller controller = null;
    private View view = null;
    
    /** Creates a new instance of Resource */
    public ResourceImpl(Resource parent)
    {
	this.parent = parent;
    }
    
    public View doGet(HttpServletRequest request, HttpServletResponse response)
    {
	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	return view;
    }

    public View doPost(HttpServletRequest request, HttpServletResponse response)
    {
	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	return view;
	
    }
    
    public View doPut(HttpServletRequest request, HttpServletResponse response)
    {
	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	return view;
    }

    public View doDelete(HttpServletRequest request, HttpServletResponse response)
    {
	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	return view;	
    }

    public boolean hasParent()
    {
	return getParent() != null;
    }

    public Resource getParent()
    {
	return parent;
    }

    public void setParent(Resource parent)
    {
	this.parent = parent;
    }
    
    public View getView()
    {
	return view;
    }
    
    public void setView(View view)
    {
	this.view = view;
    }
    
    public Controller getController()
    {
	return controller;
    }
    
    public void setController(Controller controller)
    {
	this.controller = controller;
    }
}
