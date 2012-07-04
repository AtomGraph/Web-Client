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

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
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

    /*
     * JAX-RS implementation. Not used because ModelProvider cannot simply access base URI from Client
     * 

    public static final Map<javax.ws.rs.core.MediaType, Double> QUALIFIED_TYPES;    
    static
    {
	Map<javax.ws.rs.core.MediaType, Double> typeMap = new HashMap<javax.ws.rs.core.MediaType, Double>();
	
	typeMap.put(MediaType.APPLICATION_RDF_XML_TYPE, null);
	typeMap.put(MediaType.TEXT_TURTLE_TYPE, 0.9);
	typeMap.put(MediaType.TEXT_PLAIN_TYPE, 0.7);
	typeMap.put(MediaType.APPLICATION_XML_TYPE, 0.5);
	
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }    

    private ClientConfig config = new DefaultClientConfig();
    
    public DataManager(LocationMapper _mapper)
    {
	super(_mapper);
	config.getClasses().add(ModelProvider.class);
    }

    public DataManager(FileManager filemanager)
    {
	super(filemanager);
	config.getClasses().add(ModelProvider.class);
    }

    public DataManager()
    {
	config.getClasses().add(ModelProvider.class);
    }
    
    @Override
    public Model loadModel(String filenameOrURI)
    {
	// http://blogs.oracle.com/enterprisetechtips/entry/consuming_restful_web_services_with#regp
	log.debug("Loading models using cache? getCachingModels: {}", getCachingModels());
	if (hasCachedModel(filenameOrURI))
	{
	    log.debug("Model cache hit: " + filenameOrURI);
	    return getFromCache(filenameOrURI);
	}
	
	// Make sure we load from HTTP as the last resort, otherwise handle with FileManager
	String mappedURI = mapURI(filenameOrURI);
	if (!mappedURI.startsWith("http:"))
	{
	    log.debug("loadModel({}) mapped to {} handled by FileManager", filenameOrURI, mapURI(filenameOrURI));
	    return super.loadModel(filenameOrURI);
	}
	else
	{
	    log.trace("Loading Model from URI: {} with Accept header: {}", filenameOrURI, getAcceptHeader());
	    Model m = Client.create(config).
		    resource(filenameOrURI).
		    header("Accept", getAcceptHeader()).
		    get(Model.class);

	    addCacheModel(filenameOrURI, m);

	    return m;
	}
    }
    */

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

	Model m = null;
	if (isSPARQLService(filenameOrURI))
	{
	    SPARQLService service = findSPARQLService(filenameOrURI);
	    log.debug("URI {} is a SPARQL service, executing Query on SparqlService: {}", service.getURI());
	    
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

    public Model loadModel(String serviceURI, Query query)
    {
	log.debug("Remote service {} Query: {} ", serviceURI, query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    return null;

	if (isSPARQLService(serviceURI))
	    return loadModel(findSPARQLService(serviceURI), query);
	else
	{
	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(serviceURI, query);

	    if (query.isConstructType()) return request.execConstruct();
	    if (query.isDescribeType()) return request.execDescribe();

	    return null;
	}
    }
	
    public Model loadModel(SPARQLService service, Query query)
    {
	log.debug("Remote service {} Query: {} ", service.getURI(), query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    return null;
		
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(service.getURI(), query);

	if (service.getUser() != null && service.getPassword() != null)
	{
	    log.debug("HTTP Basic authentication with username: {}", service.getUser());
	    request.setBasicAuthentication(service.getUser(), service.getPassword());
	}
	if (service.getApiKey() != null)
	{
	    log.debug("Authentication with API key: {}", service.getApiKey());
	    ((QueryEngineHTTP) request).addParam("apikey", service.getApiKey());
	}
	
	if (query.isConstructType()) return request.execConstruct();
	if (query.isDescribeType()) return request.execDescribe();
	
	return null;
    }
    
    public Model loadModel(Model model, Query query)
    {
	log.debug("Local Model Query: {}", query);
	
	if (!(query.isConstructType() || query.isDescribeType()))
	    return null;
		
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
	    if (filenameOrURI.startsWith(service.getURI()))
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

    public void addSPARQLService(String serviceURI, String user, char[] password)
    {
	services.add(new SPARQLService(serviceURI, user, password));
    }

    public void addSPARQLService(String serviceURI, String apiKey)
    {
	services.add(new SPARQLService(serviceURI, apiKey));
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

	    String baseURI = null;
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

    public ResultSet loadResultSet(String filenameOrURI)
    {
	if (isSPARQLService(filenameOrURI))
	    return loadResultSet(findSPARQLService(filenameOrURI), parseQuery(filenameOrURI));
	else
	{
	    Query query = parseQuery(filenameOrURI);
	    if (!(query.isSelectType()))
		return null;

	    String serviceURI = UriBuilder.fromUri(filenameOrURI).
		    queryParam("query", (String)null).
		    build().toString();
	    
	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(serviceURI, query);
	    return request.execSelect();
	}
    }

    public ResultSet loadResultSet(String serviceURI, Query query)
    {
	if (isSPARQLService(serviceURI))
	    return loadResultSet(findSPARQLService(serviceURI), query);
	else
	{
	    log.debug("Remote service {} Query execution: {} ", serviceURI, query);

	    if (!query.isSelectType())
		return null;

	    QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(serviceURI, query);

	    return request.execSelect();
	}
    }

    public ResultSet loadResultSet(SPARQLService service, Query query)
    {
	log.debug("Remote service {} Query execution: {} ", service.getURI(), query);

	if (!query.isSelectType())
	    return null;
	
	QueryEngineHTTP request = QueryExecutionFactory.createServiceRequest(service.getURI(), query);
	if (service.getUser() != null && service.getPassword() != null)
	{
	    log.debug("HTTP Basic authentication with username: {}", service.getUser());
	    request.setBasicAuthentication(service.getUser(), service.getPassword());
	}
	if (service.getApiKey() != null)
	{
	    log.debug("Authentication with API key param: {}", service.getApiKey());
	    ((QueryEngineHTTP) request).addParam("apikey", service.getApiKey());
	}

	return request.execSelect();
    }
    
    public ResultSet loadResultSet(Model model, Query query)
    {
	log.debug("Local Model Query: {}", query);

	if (!query.isSelectType())
	    return null;
	
	QueryExecution qex = QueryExecutionFactory.create(query, model);
	
	return qex.execSelect();
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
		    if (query != null && (query.isSelectType() || query.isAskType()))
		    {
			log.debug("Loading ResultSet for URI: {}", uri);
			return getSource(loadResultSet(uri));
		    }

		    log.debug("Loading Model for URI: {}", uri);
		    return getSource(loadModel(uri));
		}
		catch (Exception ex)
		{
		    log.debug("Could not read Model or ResultSet from URI (not found or syntax error)", ex);
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

    public Query parseQuery(String uri)
    {
	//String queryString = UriBuilder.fromUri(uri).build().getQuery();
	String queryString = uri.substring(uri.indexOf("?") + 1); // TO-DO: query might be missing

	if (queryString != null)
	{
	    String[] params = queryString.split("&");
	    for (String param : params)
	    {
		String[] array = param.split("=");
		if (array[0].equals("query"))
		{
		    try
		    {
			String sparqlString = URLDecoder.decode(array[1], "UTF-8");
			log.debug("Query string: {} from URI: {}", sparqlString, uri);

			return QueryFactory.create(sparqlString);
		    } catch (UnsupportedEncodingException ex)
		    {
			log.warn("UTF-8 encoding unsupported", ex);
		    }

		}
	    }
	}
	
	return null;
    }

    // uses graph store protocol - expects /sparql service!
    public void putModel(String serviceURI, String graphURI, Model model)
    {
	log.debug("PUTting Model to service {} with GRAPH URI {}", serviceURI, graphURI);
	
	DatasetGraphAccessorHTTP http = null;
		
	if (isSPARQLService(serviceURI)) // service registered with credentials
	{
	    SPARQLService service = findSPARQLService(serviceURI);
	    serviceURI = serviceURI.replace("/sparql", "/service"); // TO-DO: better to avoid this and make generic
	    log.debug("URI {} is a SPARQL service, sending PUT with credentials: {}", serviceURI, service.getUser());
	    http = new DatasetGraphAccessorHTTP(serviceURI, service.getUser(), service.getPassword());
	}
	else // no credentials
	{
	    serviceURI.replace("/sparql", "/service");
	    log.debug("URI {} is *not* a SPARQL service, sending PUT without credentials", serviceURI);
	    http = new DatasetGraphAccessorHTTP(serviceURI);
	}
	
	DatasetAccessor accessor = new DatasetAdapter(http);
	accessor.putModel(graphURI, model);
    }

    public void putModel(String serviceURI, Model model)
    {
	
    }

}
