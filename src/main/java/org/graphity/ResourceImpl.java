/*
 * Resource.java
 *
 * Created on Ketvirtadienis, 2007, Kovo 29, 17.50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.graphity;


import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 * 
 * @author Pumba
 */
abstract public class ResourceImpl implements Resource {
    //@Context private ResourceContext resourceContext;
    @Context private ServletContext servletContext;
    //@Context private Application application;
    //@Context private ResourceConfig resourceConfig;
    @Context private UriInfo uriInfo = null;
    @Context private Request request = null;
    //private Response response = null;

    public ResourceImpl()
    {
	//this.response = Response.ok().build();
    }

    @Override
    public String getURI()
    {
	return uriInfo.getAbsolutePath().toString();
    }

    @Override
    public URI getBaseUri()
    {
	return uriInfo.getBaseUri();
    }

    @Override
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    /*
    @Override
    public Application getApplication()
    {
        return resourceConfig;
    }
     */
    
    @Override
    public ServletContext getServletContext()
    {
        return servletContext;
    }

    @Override
    public boolean authorize()
    {
	return true;
    }

}