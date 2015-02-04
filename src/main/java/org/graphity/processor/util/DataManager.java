/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.graphity.processor.locator.LocatorLinkedData;
import org.graphity.processor.locator.PrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data manager subclass that supports Linked Data locator and parses SPARQL queries from URIs.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.locator.LocatorLinkedData
 */
public class DataManager extends org.graphity.core.util.DataManager
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    public DataManager(LocationMapper mapper, Context context, boolean preemptiveAuth)
    {
        super(mapper, context, preemptiveAuth);
    }

    @Override
    public void addCacheModel(String uri, Model m)
    {
	if (log.isTraceEnabled()) log.trace("Adding Model to cache with URI: ({})", uri);
	super.addCacheModel(uri, m);
    }

    @Override
    public boolean hasCachedModel(String filenameOrURI)
    {
	boolean cached = super.hasCachedModel(filenameOrURI);
	if (log.isTraceEnabled()) log.trace("Is Model with URI {} cached: {}", filenameOrURI, cached);
	return cached;
    }

    public boolean isPrefixMapped(String filenameOrURI)
    {
	return !getPrefix(filenameOrURI).equals(filenameOrURI);
    }

    public String getPrefix(String filenameOrURI)
    {
	if (getLocationMapper() instanceof PrefixMapper)
	{
	    String baseURI = ((PrefixMapper)getLocationMapper()).getPrefix(filenameOrURI);
	    if (baseURI != null) return baseURI;
	}
	
	return filenameOrURI;
    }
    
    /**
     * Loads RDF model from URI.
     * If URI is prefix-mapped, local file is returned.
     * If the URI is SPARQL Protocol URI (contains endpoint and encoded query), the query is decoded and executed on the endpoint.
     * Otherwise, attempting to reading RDF from Linked Data URI.
     * 
     * @param filenameOrURI target location
     * @return RDF model
     */
    @Override
    public Model loadModel(String filenameOrURI)
    {
	if (isPrefixMapped(filenameOrURI))
	{
	    String prefix = getPrefix(filenameOrURI);
	    if (log.isDebugEnabled()) log.debug("URI {} is prefix mapped, loading prefix URI: {}", filenameOrURI, prefix);
	    return loadModel(prefix);
	}
	
	if (log.isDebugEnabled()) log.debug("loadModel({})", filenameOrURI);
	filenameOrURI = UriBuilder.fromUri(filenameOrURI).fragment(null).build().toString(); // remove document fragments
	
        if (hasCachedModel(filenameOrURI))
	{
	    if (log.isDebugEnabled()) log.debug("Returning cached Model for URI: {}", filenameOrURI);
	    return getFromCache(filenameOrURI) ;
	}  

	Model model;
	Map.Entry<String, Context> endpoint = findEndpoint(filenameOrURI);
	if (endpoint != null)
	{
	    if (log.isDebugEnabled()) log.debug("URI {} is a SPARQL service, executing Query on SPARQL endpoint: {}", filenameOrURI);

	    model = ModelFactory.createDefaultModel();
	    Query query = parseQuery(filenameOrURI);
	    if (query != null) model = loadModel(endpoint.getKey(), query);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("URI {} is *not* a SPARQL service, reading Model from TypedStream", filenameOrURI);

	    model = ModelFactory.createDefaultModel();
	    readModel(model, filenameOrURI);
	}

	addCacheModel(filenameOrURI, model);
	
        return model;
    }

    public boolean isMapped(String filenameOrURI)
    {
	String mappedURI = mapURI(filenameOrURI);
	return (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:"));
    }

    /**
     * Reads RDF into model from URI location.
     * If the URI is mapped, local mapped file is read.
     * Otherwise, attempting to select RDF syntax and read into stream from the target location.
     * 
     * @param model RDF model
     * @param filenameOrURI target location
     * @return populated RDF model
     */
    @Override
    public Model readModel(Model model, String filenameOrURI)
    {
	String mappedURI = mapURI(filenameOrURI);
	if (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:")) // if URI is mapped and local
	{
	    if (log.isDebugEnabled()) log.debug("URI {} is mapped to {}, letting FileManager.readModel() handle it", filenameOrURI, mappedURI);
	    if (log.isDebugEnabled()) log.debug("FileManager.readModel() URI: {} Base URI: {}", mappedURI, filenameOrURI);

	    return super.readModel(model, mappedURI, filenameOrURI, null); // let FileManager handle
	}

	TypedStream in = openNoMapOrNull(filenameOrURI);
	if (in != null)
	{
	    if (log.isDebugEnabled()) log.debug("Opened filename or URI {} with TypedStream {}", filenameOrURI, in);

            Lang lang = RDFLanguages.contentTypeToLang(in.getMimeType());
	    if (lang != null) // do not read if MimeType/syntax are not known
	    {
                String syntax = lang.getName();
		if (log.isDebugEnabled()) log.debug("URI {} syntax is {}, reading it", filenameOrURI, syntax);

		model.read(in.getInput(), filenameOrURI, syntax) ;
		try { in.getInput().close(); } catch (IOException ex) {}
	    }
	    else
		if (log.isDebugEnabled()) log.debug("Syntax for URI {} unknown, ignoring", filenameOrURI);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Failed to locate '"+filenameOrURI+"'") ;
	    throw new NotFoundException("Not found: "+filenameOrURI) ;
	}

	return model;
    }
    
    /** Add a Linked Data locator */
    public void addLocatorLinkedData()
    {
        Locator loc = new LocatorLinkedData() ;
        addLocator(loc) ;
    }

    public void removeLocatorURL()
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
	    if (log.isDebugEnabled()) log.debug("Removing Locator: {}", locURL);
	    remove(locURL);
	}
    }
 
    /**
     * Parses query from URI, if it is a SPARQL Protocol URI.
     * 
     * @param uri URI string
     * @return parsed query
     */
    public Query parseQuery(String uri)
    {
	if (uri.indexOf("?") > 0)
	{
	    String queryString = uri.substring(uri.indexOf("?") + 1);
	    MultivaluedMap<String, String> paramMap = getParamMap(queryString);
	    if (paramMap.containsKey("query"))
	    {
		String sparqlString = paramMap.getFirst("query");
		if (log.isDebugEnabled()) log.debug("Query string: {} from URI: {}", sparqlString, uri);

		return QueryFactory.create(sparqlString);
	    }
	}
	
	return null;
    }

    /**
     * Parses parameter key/value map from HTTP query string.
     * 
     * @param query query string
     * @return parameter map
     */
    public MultivaluedMap<String, String> getParamMap(String query)  
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
		if (log.isWarnEnabled()) log.warn("Could not URL-decode query string component", ex);
	    }
	}

	return map;
    }

}