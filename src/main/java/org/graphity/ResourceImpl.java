/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graphity;


import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
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