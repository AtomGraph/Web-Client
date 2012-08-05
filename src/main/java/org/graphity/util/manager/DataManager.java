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

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.adapter.DatasetGraphAccessorHTTP;
import org.graphity.util.locator.LocatorLinkedData;
import org.graphity.util.locator.PrefixMapper;
import org.openjena.fuseki.DatasetAccessor;
import org.openjena.fuseki.http.DatasetAdapter;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Uses portions of Jena code
* (c) Copyright 2010 Epimorphics Ltd.
* All rights reserved.
*
* @see org.openjena.fuseki.FusekiLib
* {@link http://openjena.org}
*
 * @author Martynas Jusevičius <martynas@graphity.org>
*/

public class DataManager extends FileManager implements URIResolver
{
    private static DataManager s_instance = null;

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    private List<SPARQLService> services = new ArrayList<SPARQLService>() ;
    protected boolean resolvingUncached = false;
    protected boolean resolvingMapped = true;
    protected boolean resolvingSPARQL = true;

    public static DataManager get() {
        if (s_instance == null) {
            s_instance = new DataManager(FileManager.get());
	    log.debug("new DataManager({}): {}", FileManager.get(), s_instance);
        }
        return s_instance;
    }

   public static final Map<String, String> LANGS = new HashMap<String, String>() ;
    static
    {
        LANGS.put(WebContent.contentTypeRDFXML, WebContent.langRDFXML);
        LANGS.put(WebContent.contentTypeTurtle1, WebContent.langTurtle);
        LANGS.put(WebContent.contentTypeTurtle2, WebContent.langTurtle);
        LANGS.put(WebContent.contentTypeTurtle3, WebContent.langTurtle);
        LANGS.put(WebContent.contentTypeNTriples, WebContent.langNTriple); // text/plain
        LANGS.put(WebContent.contentTypeNTriplesAlt, WebContent.langNTriple) ;
    }
    
    public static final List<String> IGNORED_EXT = new ArrayList<String>();
    static
    {
	IGNORED_EXT.add("html"); IGNORED_EXT.add("htm"); // GRDDL or <link> inspection could be used to analyzed HTML
	IGNORED_EXT.add("jpg");	IGNORED_EXT.add("gif");	IGNORED_EXT.add("png"); // binary image formats
	IGNORED_EXT.add("avi"); IGNORED_EXT.add("mpg"); IGNORED_EXT.add("wmv"); // binary video formats
	IGNORED_EXT.add("mp3"); IGNORED_EXT.add("wav"); // binary sound files
	IGNORED_EXT.add("zip"); IGNORED_EXT.add("rar"); // binary archives
	IGNORED_EXT.add("pdf"); IGNORED_EXT.add("ps"); IGNORED_EXT.add("doc"); // binary documents
	IGNORED_EXT.add("exe"); // binary executables
    }

    public DataManager(FileManager fMgr)
    {
	super(fMgr);
	addLocatorLinkedData();
	removeLocatorURL();
    }
    
    @Override
    public Model loadModel(String filenameOrURI)
    {
	log.debug("loadModel({})", filenameOrURI);
	filenameOrURI = UriBuilder.fromUri(filenameOrURI).fragment(null).build().toString(); // remove document fragments
	
        if (hasCachedModel(filenameOrURI)) return getFromCache(filenameOrURI) ;  

	Model m;
	if (isSPARQLService(filenameOrURI))
	{
	    SPARQLService service = findSPARQLService(filenameOrURI);
	    log.debug("URI {} is a SPARQL service, executing Query on SparqlService: {}", service.getEndpointURI());
	    
	    m = loadModel(service, parseQuery(filenameOrURI));
	}
	else
	{
	    log.debug("URI {} is *not* a SPARQL service, reading Model from TypedStream", filenameOrURI);

	    m = ModelFactory.createDefaultModel();
	    readModel(m, filenameOrURI);
	}
	
	addCacheModel(filenameOrURI, m);
	
        return m;
    }

    public Model loadModel(String endpointURI, Query query, MultivaluedMap<String, String> params)
    {
	log.debug("Remote service {} Query: {} ", endpointURI, query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    throw new QueryExecException("Query to load Model must be CONSTRUCT or DESCRIBE"); // return null;

	if (isSPARQLService(endpointURI))
	    return loadModel(findSPARQLService(endpointURI), query);
	else
	{
	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(endpointURI, query);
	    if (params != null)
		for (Entry<String, List<String>> entry : params.entrySet())
		    if (!entry.getKey().equals("query")) // query param is handled separately
			for (String value : entry.getValue())
			{
			    log.trace("Adding param to SPARQL request with name: {} and value: {}", entry.getKey(), value);
			    request.addParam(entry.getKey(), value);
			}
	    if (query.isConstructType()) return request.execConstruct();
	    if (query.isDescribeType()) return request.execDescribe();

	    return null;
	}
    }
    
    public Model loadModel(String endpointURI, Query query)
    {
	return loadModel(endpointURI, query, null);
    }
	
    public Model loadModel(SPARQLService service, Query query)
    {
	log.debug("Remote service {} Query: {} ", service.getEndpointURI(), query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    throw new QueryExecException("Query to load Model must be CONSTRUCT or DESCRIBE"); // return null;
		
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(service.getEndpointURI(), query);

	if (service.getUser() != null && service.getPassword() != null)
	{
	    log.debug("HTTP Basic authentication with username: {}", service.getUser());
	    request.setBasicAuthentication(service.getUser(), service.getPassword());
	}
	if (service.getApiKey() != null)
	{
	    log.debug("Authentication with API key: {}", service.getApiKey());
	    request.addParam("apikey", service.getApiKey());
	}
	
	if (query.isConstructType()) return request.execConstruct();
	if (query.isDescribeType()) return request.execDescribe();
	
	return null;
    }
    
    public Model loadModel(Model model, Query query)
    {
	log.debug("Local Model Query: {}", query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    throw new QueryExecException("Query to load Model must be CONSTRUCT or DESCRIBE"); // return null;
		
	QueryExecution qex = QueryExecutionFactory.create(query, model);

	if (query.isConstructType()) return qex.execConstruct();
	if (query.isDescribeType()) return qex.execDescribe();
	
	return null;
    }
    
    public boolean isMapped(String filenameOrURI)
    {
	String mappedURI = mapURI(filenameOrURI);
	return (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:"));
    }
    
    public SPARQLService findSPARQLService(String filenameOrURI)
    {
	Iterator<SPARQLService> it = sparqlServices();
	
	while (it.hasNext())
	{
	    SPARQLService service = it.next();
	    if (filenameOrURI.startsWith(service.getEndpointURI()))
		return service;
	}
	
	return null;
    }

    public boolean isSPARQLService(String filenameOrURI)
    {
	return findSPARQLService(filenameOrURI) != null;
    }
    
    public void addSPARQLService(SPARQLService service)
    {
	log.debug("Adding SPARQLService: {}", service.getName());
	services.add(service);
    }

    public void addSPARQLService(String endpointURI, String user, char[] password)
    {
	services.add(new SPARQLService(endpointURI, user, password));
    }

    public void addSPARQLService(String endpointURI, String apiKey)
    {
	services.add(new SPARQLService(endpointURI, apiKey));
    }

    public Iterator<SPARQLService> sparqlServices()
    {
	return services.listIterator();
    }    
    
    @Override
    public Model readModel(Model model, String filenameOrURI) // does not use SparqlServices!!!
    {
	String mappedURI = mapURI(filenameOrURI);
	if (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:")) // if URI is mapped and local
	{
	    log.debug("URI {} is mapped to {}, letting FileManager.readModel() handle it", filenameOrURI, mappedURI);

	    String baseURI;
	    if (getLocationMapper() instanceof PrefixMapper)
		baseURI = ((PrefixMapper)getLocationMapper()).getPrefix(filenameOrURI);
	    else
		baseURI = filenameOrURI;
	    log.debug("FileManager.readModel() URI: {} Base URI: {}", filenameOrURI, baseURI);

	    return super.readModel(model, mappedURI, baseURI, null); // let FileManager handle
	}

	TypedStream in = openNoMapOrNull(filenameOrURI);
	if (in != null)
	{
	    log.debug("Opened filename or URI {} with TypedStream {}", filenameOrURI, in);

	    String syntax = langFromContentType(in.getMimeType());

	    if (syntax != null) // do not read if MimeType/syntax are not known
	    {
		log.debug("URI {} syntax is {}, reading it", filenameOrURI, syntax);

		model.read(in.getInput(), filenameOrURI, syntax) ;
		try { in.getInput().close(); } catch (IOException ex) {}
	    }
	    else
		log.debug("Syntax for URI {} unknown, ignoring", filenameOrURI);
	}
	else
	{
	    if ( log.isDebugEnabled() )
		log.debug("Failed to locate '"+filenameOrURI+"'") ;
	    throw new NotFoundException("Not found: "+filenameOrURI) ;
	}

	return model;
    }
    
    /** Add a Linked Data locator */
    public final void addLocatorLinkedData()
    {
        Locator loc = new LocatorLinkedData() ;
        addLocator(loc) ;
    }

    private void removeLocatorURL()
    {
	Locator locURL = null;
	Iterator<Locator> it = locators();
	while (it.hasNext())
	{
	    Locator loc = it.next();
	    if (loc.getName().equals("LocatorURL")) locURL = loc;
	}
	// remove() needs to be called outside the iterator
	if (locURL != null)
	{
	    log.debug("Removing Locator: {}", locURL);
	    remove(locURL);
	}
    }
    
    // ---- To riot.WebContent
    public static String langFromContentType(String mimeType)
    {
        if ( mimeType == null )
            return null ;
        return LANGS.get(mimeType.toLowerCase()) ;
    }

    public ResultSet loadResultSet(String endpointURI, Query query, MultivaluedMap<String, String> params)
    {
	if (isSPARQLService(endpointURI))
	    return loadResultSet(findSPARQLService(endpointURI), query);
	else
	{
	    log.debug("Remote service {} Query execution: {} ", endpointURI, query);

	    if (!query.isSelectType())
		throw new QueryExecException("Query to load ResultSet must be SELECT or ASK"); // return null

	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(endpointURI, query);
	    if (params != null)
		for (Entry<String, List<String>> entry : params.entrySet())
		    if (!entry.getKey().equals("query")) // query param is handled separately
			for (String value : entry.getValue())
			{
			    log.trace("Adding param to SPARQL request with name: {} and value: {}", entry.getKey(), value);
			    request.addParam(entry.getKey(), value);
			}
	    return request.execSelect();
	}	
    }
    
    public ResultSet loadResultSet(String endpointURI, Query query)
    {
	return loadResultSet(endpointURI, query, null);
    }

    public ResultSet loadResultSet(SPARQLService service, Query query)
    {
	log.debug("Remote service {} Query execution: {} ", service.getEndpointURI(), query);

	if (!query.isSelectType())
	    throw new QueryExecException("Query to load ResultSet must be SELECT or ASK"); // return null
	
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(service.getEndpointURI(), query);
	if (service.getUser() != null && service.getPassword() != null)
	{
	    log.debug("HTTP Basic authentication with username: {}", service.getUser());
	    request.setBasicAuthentication(service.getUser(), service.getPassword());
	}
	if (service.getApiKey() != null)
	{
	    log.debug("Authentication with API key param: {}", service.getApiKey());
	    request.addParam("apikey", service.getApiKey());
	}

	return request.execSelect();
    }
    
    public ResultSet loadResultSet(Model model, Query query)
    {
	log.debug("Local Model Query: {}", query);

	if (!query.isSelectType())
	    throw new QueryExecException("Query to load ResultSet must be SELECT or ASK"); // return null
	
	QueryExecution qex = QueryExecutionFactory.create(query, model);
	
	return qex.execSelect();
    }

    // uses graph store protocol - expects /sparql service!
    public void putModel(String endpointURI, String graphURI, Model model)
    {
	log.debug("PUTting Model to service {} with GRAPH URI {}", endpointURI, graphURI);
	
	DatasetGraphAccessorHTTP http;
		
	if (isSPARQLService(endpointURI)) // service registered with credentials
	{
	    SPARQLService service = findSPARQLService(endpointURI);
	    endpointURI = endpointURI.replace("/sparql", "/service"); // TO-DO: better to avoid this and make generic
	    log.debug("URI {} is a SPARQL service, sending PUT with credentials: {}", endpointURI, service.getUser());
	    http = new DatasetGraphAccessorHTTP(endpointURI, service.getUser(), service.getPassword());
	}
	else // no credentials
	{
	    endpointURI.replace("/sparql", "/service");
	    log.debug("URI {} is *not* a SPARQL service, sending PUT without credentials", endpointURI);
	    http = new DatasetGraphAccessorHTTP(endpointURI);
	}
	
	DatasetAccessor accessor = new DatasetAdapter(http);
	accessor.putModel(graphURI, model);
    }

    public void putModel(String endpointURI, Model model)
    {
	
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);
	String uri = URI.create(base).resolve(href).toString();

	if (isIgnored(uri))
	{
	    log.debug("URI ignored by file extension: {}", uri);
	    return getDefaultSource();
	}

	Model model = getFromCache(uri);
	if (model == null) // URI not cached, 
	{
	    log.debug("No cached Model for URI: {}", uri);
	    log.debug("isSPARQLService({}): {}", uri, isSPARQLService(uri));
	    log.debug("isMapped({}): {}", uri, isMapped(uri));

	    if (resolvingUncached ||
		    (resolvingSPARQL && isSPARQLService(uri)) ||
		    (resolvingMapped && isMapped(uri)))
		try
		{
		    Query query = parseQuery(uri);
		    if (query != null)
		    {
			if (query.isSelectType() || query.isAskType())
			{
			    log.trace("Loading ResultSet for URI: {} using Query: {}", uri, query);
			    return getSource(loadResultSet(UriBuilder.fromUri(uri).
				    replaceQuery(null).
				    build().toString(),
				query, parseParamMap(uri)));
			}
			if (query.isConstructType() || query.isDescribeType())
			{
			    log.trace("Loading Model for URI: {} using Query: {}", uri, query);
			    return getSource(loadModel(UriBuilder.fromUri(uri).
				    replaceQuery(null).
				    build().toString(),
				query, parseParamMap(uri)));
			}
		    }

		    log.trace("Loading Model for URI: {}", uri);
		    return getSource(loadModel(uri));
		}
		catch (Exception ex)
		{
		    log.warn("Could not read Model or ResultSet from URI (not found or syntax error)", ex);
		    return getDefaultSource(); // return empty Model
		}
	    else
	    {
		log.debug("Defaulting to empty Model for URI: {}", uri);
		return getDefaultSource(); // return empty Model
	    }
	}
	else
	{
	    log.debug("Cached Model for URI: {}", uri);
	    return getSource(model);
	}
    }

    protected Source getDefaultSource()
    {
	return getSource(ModelFactory.createDefaultModel());
    }
    
    protected Source getSource(Model model)
    {
	log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	model.write(stream);

	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }

    protected Source getSource(ResultSet results)
    {
	log.debug("ResultVars: {}", results.getResultVars());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ResultSetFormatter.outputAsXML(stream, results);
	
	log.debug("SPARQL XML result bytes written: {}", stream.toByteArray().length);
	
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
    }
    
    public boolean isIgnored(String filenameOrURI)
    {
	return IGNORED_EXT.contains(FileUtils.getFilenameExt(filenameOrURI));
    }

    public boolean isResolvingUncached()
    {
	return resolvingUncached;
    }

    public void setResolvingUncached(boolean resolvingUncached)
    {
	this.resolvingUncached = resolvingUncached;
    }

    public static MultivaluedMap<String, String> parseParamMap(String uri)
    {
	if (uri.indexOf("?") > 0)
	{
	    String queryString = uri.substring(uri.indexOf("?") + 1);
	    return getParamMap(queryString);
	}
	
	return null;
    }
    
    public static MultivaluedMap<String, String> getParamMap(String query)  
    {  
	String[] params = query.split("&");
	MultivaluedMap<String, String> map = new MultivaluedMapImpl();
	
	for (String param : params)  
	{
	    try
	    {
		String name = URLDecoder.decode(param.split("=")[0], "UTF-8");
		String value = URLDecoder.decode(param.split("=")[1], "UTF-8");
		map.add(name, value);
	    }
	    catch (UnsupportedEncodingException ex)
	    {
		log.warn("Could not URL-decode query string component", ex);
	    }
	}

	return map;
    }

    public static Query parseQuery(String uri)
    {
	if (uri.indexOf("?") > 0)
	{
	    //String queryString = UriBuilder.fromUri(uri).build().getQuery();
	    String queryString = uri.substring(uri.indexOf("?") + 1);
	    MultivaluedMap<String, String> paramMap = getParamMap(queryString);
	    if (paramMap.containsKey("query"))
	    {
		String sparqlString = paramMap.getFirst("query");
		log.debug("Query string: {} from URI: {}", sparqlString, uri);

		return QueryFactory.create(sparqlString);
	    }
	}
	
	return null;
    }

}
