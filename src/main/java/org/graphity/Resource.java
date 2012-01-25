/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity;

import java.net.URI;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Pumba
 */
public interface Resource
{
	public String getURI(); // full URI starting with http://; URL-encoded
	public URI getBaseUri();
	public UriInfo getUriInfo();
	//public Application getApplication();
	public ServletContext getServletContext();

	//public boolean exists() throws Exception;
	public boolean authorize() throws Exception;
	
	public Date getLastModified();

}
