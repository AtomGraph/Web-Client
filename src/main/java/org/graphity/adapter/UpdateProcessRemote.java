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

package org.graphity.adapter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class UpdateProcessRemote implements UpdateProcessor
{
    private static final Logger log = LoggerFactory.getLogger(UpdateProcessRemote.class);
    
    private final UpdateRequest request ;
    private final String endpoint ;
    private String user = null ;
    private char[] password = null ;

    public UpdateProcessRemote(UpdateRequest request , String endpoint)
    {
        this.request = request ;
        this.endpoint = endpoint ;
    }

    @Override
    public void setInitialBinding(QuerySolution binding)
    {
        throw new ARQException("Initial bindings for a remote update execution request not supported") ;
    }

    @Override
    public GraphStore getGraphStore()
    {
        return null ;
    }

    @Override
    public void execute()
    {
	Client client = Client.create();
	WebResource wr = client.resource(endpoint).queryParam("auth_token", "2hwmvJSgCSV3fa3xGIb9");
	client.addFilter(new LoggingFilter(System.out));

	/*
	if (user != null || password != null)
	    try
	    {
		StringBuilder x = new StringBuilder() ;
		byte b[] = x.append(user).append(":").append(password).toString().getBytes("UTF-8") ;
		String y = Base64.encodeBytes(b) ;
		
		log.debug("Authorization: Basic {}", y);
		wr.header("Authorization", "Basic "+y);
	    } catch (UnsupportedEncodingException ex)
	    {
		log.warn("Unsupported encoding", ex);
	    }
	*/
	
	String reqStr = request.toString().replaceFirst("CREATE <", "CREATE GRAPH <"); // Jena bug fixed in 2.7.0

	log.debug("Sending SPARQL request {} to endpoint {}", reqStr, endpoint);
	ClientResponse response =
	wr.type(WebContent.contentTypeSPARQLUpdate).
	accept(WebContent.contentTypeResultsXML).
	post(ClientResponse.class, reqStr);
	
	log.debug("SPARQL endpoint response: {}", response);
    }

    public void setBasicAuthentication(String user, char[] password)
    {
        this.user = user ;
        this.password = password ;
    }
}