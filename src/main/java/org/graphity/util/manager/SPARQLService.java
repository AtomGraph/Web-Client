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
package org.graphity.util.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLService
{
    private static Logger log = LoggerFactory.getLogger(SPARQLService.class);
    
    private String endpointUri = null;
    private String user = null;
    private char[] password = null;
    private String apiKey = null;
    
    private SPARQLService(String endpointUri)
    {
	this.endpointUri = endpointUri;	
    }
    
    public SPARQLService(String endpointUri, String user, char[] password)
    {
	this(endpointUri);
	this.user = user;
	this.password = password;
    }

    public SPARQLService(String endpointUri, String apiKey)
    {
	this(endpointUri);
	this.apiKey = apiKey;
    }

    public String getEndpointURI()
    {
	return endpointUri;
    }

    public String getName()
    {
	return "SparqlService(" + endpointUri + ")";
    }

    public char[] getPassword()
    {
	return password;
    }

    public String getUser()
    {
	return user;
    }

    /*
    public void setBasicAuthentication(String user, char[] password)
    {
        this.user = user ;
        this.password = password ;
    }
    */

    public String getApiKey()
    {
	return apiKey;
    }

}
