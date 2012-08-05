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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.ByteArrayOutputStream;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLAdapter // implements org.openjena.fuseki.DatasetAccessor
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLAdapter.class);
    
    private String endpoint = null;
    
    public SPARQLAdapter(String endpoint)
    {
	this.endpoint = endpoint;
    }

    public String getEndpoint()
    {
	return endpoint;
    }
    
    public void add(Model data)
    {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	data.write(baos, WebContent.langNTriples);

	//UpdateDataInsert;
	// http://www.w3.org/TR/sparql11-update/#insertData
	UpdateRequest request = UpdateFactory.create("INSERT DATA { "
	    + baos.toString() +
	    "}", Syntax.syntaxSPARQL_11);

	UpdateProcessRemote process = new UpdateProcessRemote(request, getEndpoint());
	//process.setBasicAuthentication(getServiceApiKey(), "X".toCharArray());
	process.execute();	
    }
    
    public void add(String graphUri, Model data)
    {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	data.write(baos, WebContent.langNTriples);

	//UpdateDataInsert;
	// http://www.w3.org/TR/sparql11-update/#insertData
	UpdateRequest request = UpdateFactory.create("CREATE GRAPH <" + graphUri + ">", Syntax.syntaxSPARQL_11).
	   add("INSERT DATA { GRAPH <" + graphUri + "> {"
	    + baos.toString() +
	    "} }");

	UpdateProcessRemote process = new UpdateProcessRemote(request, getEndpoint());
	//process.setBasicAuthentication(getServiceApiKey(), "X".toCharArray());
	process.execute();
    }
    
    public boolean containsModel(String graphUri)
    {
	Query query = QueryFactory.create("ASK { <" + graphUri + "> ?p ?o }");
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(getEndpoint(), query);
	return request.execAsk();
    }
    
}
