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
package org.graphity.browser.adapter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.GregorianCalendar;
import java.util.UUID;
import javax.ws.rs.core.UriBuilder;
import org.graphity.adapter.DatasetGraphAccessorHTTP;
import org.openjena.fuseki.http.DatasetAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARQLCacheAdapter extends DatasetAdapter
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLCacheAdapter.class);
    
    private UriBuilder uriBuilder = null;
    private String endpoint = null;
    private String user = null;
    private char[] password = null;
    private String apiKey = null;
    
    public SPARQLCacheAdapter(UriBuilder uriBuilder, String endpoint)
    {
	super(new DatasetGraphAccessorHTTP(endpoint.replace("/sparql", "/service")));
	this.endpoint = endpoint;
	this.uriBuilder = uriBuilder;
    }
    public SPARQLCacheAdapter(UriBuilder uriBuilder, String endpoint, String user, char[] password)
    {
	super(new DatasetGraphAccessorHTTP(endpoint.replace("/sparql", "/service"), user, password));
	this.endpoint = endpoint;
	this.uriBuilder = uriBuilder;
	this.user = user;
	this.password = password;
    }

    public SPARQLCacheAdapter(UriBuilder uriBuilder, String endpoint, String apiKey)
    {
	super(new DatasetGraphAccessorHTTP(endpoint.replace("/sparql", "/service"), apiKey));
	this.endpoint = endpoint;
	this.uriBuilder = uriBuilder;
	this.apiKey = apiKey;
    }

    public UriBuilder getUriBuilder()
    {
	return uriBuilder.clone();
    }

    public String getEndpoint()
    {
	return endpoint;
    }

    public static String stripFragment(String filenameOrURI)
    {
	return UriBuilder.fromUri(filenameOrURI).
		fragment(null).
		build().
		toString();	
    }
    
    public void add(String filenameOrURI, Model data, String endpoint)
    {
	filenameOrURI = stripFragment(filenameOrURI);
	
	String graphUri = getUriBuilder().
	    path("graphs/{graphId}").
	    build(UUID.randomUUID().toString()).toString();

	putModel(graphUri, data);
	
	putModel(getUriBuilder().
		path("graphs/{graphId}").
		build(UUID.randomUUID().toString()).toString(),
	    createGraphMetaModel(ResourceFactory.createResource(graphUri), filenameOrURI, endpoint));
    }

    @Override
    public boolean containsModel(String filenameOrURI)
    {
	filenameOrURI = stripFragment(filenameOrURI);
	if (log.isDebugEnabled()) log.debug("Checking if endpoint {} contains graph {}", endpoint, filenameOrURI);
	Query query = QueryFactory.create("ASK { GRAPH ?g { ?s <http://purl.org/net/provenance/ns#accessedResource> <" + filenameOrURI + "> } }");
	//Query query = QueryFactory.create("ASK { <" + graphUri + "> ?p ?o }");
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(getEndpoint(), query);
	
	if (user != null && password != null)
	{
	    if (log.isDebugEnabled()) log.debug("HTTP Basic authentication with username: {}", user);
	    request.setBasicAuthentication(user, password);
	}
	
	return request.execAsk();
    }

    public Model createGraphMetaModel(Resource graph, String uri, String endpoint)
    {
	Model metaModel = ModelFactory.createDefaultModel();
	
	metaModel.add(metaModel.createLiteralStatement(graph, DCTerms.created, new GregorianCalendar()));
	//metaModel.add(metaModel.createStatement(graph, DCTerms.creator, user));

	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://www.w3.org/2004/03/trix/rdfg-1/Graph")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://purl.org/net/opmv/ns#Artifact")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://purl.org/net/provenance/ns#DataItem")));
	metaModel.add(metaModel.createStatement(graph, RDF.type, metaModel.createResource("http://www.w3.org/ns/sparql-service-description#NamedGraph")));
	
	metaModel.add(metaModel.createStatement(graph, metaModel.createProperty("http://purl.org/net/opmv/ns#retrievedBy"), metaModel.createResource(AnonId.create("access"))));
	metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), RDF.type, metaModel.createResource("http://purl.org/net/provenance/ns#DataAccess")));
	metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), RDF.type, metaModel.createResource("http://purl.org/net/provenance/types#HTTPBasedDataAccess")));
	//metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), metaModel.createProperty("http://purl.org/net/opmv/ns#wasPerformedBy"), user));
	metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), metaModel.createProperty("http://purl.org/net/provenance/types#exchangedHTTPMessage"), metaModel.createResource(AnonId.create("request"))));
	metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), metaModel.createProperty("http://purl.org/net/provenance/ns#accessedResource"), metaModel.createResource(uri)));
	if (endpoint != null) metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("access")), metaModel.createProperty("http://purl.org/net/provenance/ns#accessedService"), metaModel.createResource(endpoint)));
	
	metaModel.add(metaModel.createStatement(metaModel.createResource(AnonId.create("request")), RDF.type, metaModel.createResource("http://www.w3.org/2011/http#Request")));
	metaModel.add(metaModel.createLiteralStatement(metaModel.createResource(AnonId.create("request")), metaModel.createProperty("http://www.w3.org/2011/http#methodName"), "GET"));
	metaModel.add(metaModel.createLiteralStatement(metaModel.createResource(AnonId.create("request")), metaModel.createProperty("http://www.w3.org/2011/http#absoluteURI"), uri));

	if (log.isDebugEnabled()) log.debug("No of stmts in the metamodel: {} for GRAPH: {}", metaModel.size(), graph.getURI());
	
	return metaModel;
    }

}
