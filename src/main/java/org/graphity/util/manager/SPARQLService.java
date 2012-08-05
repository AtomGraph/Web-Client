/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
