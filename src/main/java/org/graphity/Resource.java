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
import java.util.Date;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
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
