/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.util;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.LocationMapper;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.graphity.client.locator.LocatorLinkedData;
import org.graphity.client.locator.PrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data manager subclass that resolves URI to RDF/XML.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class DataManager extends org.graphity.core.util.DataManager implements URIResolver
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    public static final List<String> IGNORED_EXT = new ArrayList<>();
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

    protected boolean resolvingUncached = false;
    protected boolean resolvingMapped = true;
    protected boolean resolvingSPARQL = true;
    
    private final UriInfo uriInfo;
        
    public DataManager(LocationMapper mapper, Context context, boolean preemptiveAuth, UriInfo uriInfo)
    {
	super(mapper, context, preemptiveAuth);
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        this.uriInfo = uriInfo;
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
    
    /**
     * Resolves relative URI to XML source.
     * @param href relative URI
     * @param base base URI
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	if (!href.equals("") && URI.create(href).isAbsolute())
	{
	    if (log.isDebugEnabled()) log.debug("Resolving URI: {} against base URI: {}", href, base);
	    URI uri = URI.create(base).resolve(href);
            return resolve(uri);
	}
	else
	{
	    if (log.isDebugEnabled()) log.debug("Stylesheet self-referencing its doc - let the processor handle resolving");
	    return null;
	}
    }

    /**
     * Resolves URI to RDF/XML.
     * Ignored extensions are rejected. Cached copy is returned, if it exists.
     * Further processing tries to decode a SPARQL query from the query string, and executes it on the endpoint.
     * If there is no query, attempting to load RDF model from URI.
     * Finally the model is serialized as RDF/XML.
     * 
     * @param uri document URI
     * @return XML source
     */
    public Source resolve(URI uri)
    {
        if (uri == null) throw new IllegalArgumentException("URI cannot be null");
        if (!uri.isAbsolute()) throw new IllegalArgumentException("URI to be resolved must be absolute");
        
        if (isIgnored(uri.toString()))
        {
            if (log.isDebugEnabled()) log.debug("URI ignored by file extension: {}", uri);
            return getDefaultSource();
        }

        Model model = getFromCache(uri.toString());
        if (model == null) // URI not cached, 
        {
            if (log.isDebugEnabled())
            {
                log.debug("No cached Model for URI: {}", uri);
                log.debug("isMapped({}): {}", uri, isMapped(uri.toString()));
            }

            URI relative = null; // indicates whether the URI being resolved is relative to the base URI
            if (getUriInfo() != null) // DataManager neeeds to be registered as @Provider
            {
                relative = getUriInfo().getBaseUri().relativize(uri);
                if (relative.isAbsolute()) relative = null;
            }

            Map.Entry<String, Context> endpoint = findEndpoint(uri.toString());
            if (endpoint != null)
                if (log.isDebugEnabled()) log.debug("URI {} has SPARQL endpoint: {}", uri, endpoint.getKey());
            else
                if (log.isDebugEnabled()) log.debug("URI {} has no SPARQL endpoint", uri);
            
            if (isResolvingUncached() || relative != null ||
                    (isResolvingSPARQL() && endpoint != null) ||
                    (isResolvingMapped() && isMapped(uri.toString())))
                try
                {
                    Query query = parseQuery(uri.toString());
                    if (query != null)
                    {
                        if (query.isSelectType() || query.isAskType())
                        {
                            if (log.isTraceEnabled()) log.trace("Loading ResultSet for URI: {} using Query: {}", uri, query);
                            return getSource(loadResultSet(UriBuilder.fromUri(uri).
                                    replaceQuery(null).
                                    build().toString(),
                                query, parseParamMap(uri.toString())), uri.toString());
                        }
                        if (query.isConstructType() || query.isDescribeType())
                        {
                            if (log.isTraceEnabled()) log.trace("Loading Model for URI: {} using Query: {}", uri, query);
                            return getSource(loadModel(UriBuilder.fromUri(uri).
                                    replaceQuery(null).
                                    build().toString(),
                                query, parseParamMap(uri.toString())), uri.toString());
                        }
                    }

                    if (log.isTraceEnabled()) log.trace("Loading Model for URI: {}", uri);
                    return getSource(loadModel(uri.toString()), uri.toString());
                }
                catch (IllegalArgumentException | UriBuilderException | NotFoundException ex)
                {
                    if (log.isWarnEnabled()) log.warn("Could not read Model or ResultSet from URI (not found or syntax error)", ex);
                    return getDefaultSource(); // return empty Model
                }
            else
            {
                if (log.isDebugEnabled()) log.debug("Defaulting to empty Model for URI: {}", uri);
                return getDefaultSource(); // return empty Model
            }
        }
        else
        {
            if (log.isDebugEnabled()) log.debug("Cached Model for URI: {}", uri);
            return getSource(model, uri.toString());
        }
    }
    
    protected Source getDefaultSource()
    {
	return getSource(ModelFactory.createDefaultModel(), null);
    }
    
    /**
     * Serializes RDF model to XML source.
     * 
     * @param model RDF model
     * @param systemId system ID (usually origin URI) of the source
     * @return XML source
     */
    public Source getSource(Model model, String systemId)
    {
	if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	model.write(stream);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
    }

    /**
     * Serializes SPARQL XML results to XML source.
     * 
     * @param results SPARQL XML results
     * @param systemId system ID (usually origin URI) of the source
     * @return XML source
     */
    public Source getSource(ResultSet results, String systemId)
    {
	if (log.isDebugEnabled()) log.debug("ResultVars: {}", results.getResultVars());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ResultSetFormatter.outputAsXML(stream, results);
	
	if (log.isDebugEnabled()) log.debug("SPARQL XML result bytes written: {}", stream.toByteArray().length);
	
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
    }
 
    /**
     * Parses parameter map from URI (if query string is present).
     * 
     * @param uri URI with optional query string
     * @return parameter map
     */
    public MultivaluedMap<String, String> parseParamMap(String uri)
    {
	if (uri.indexOf("?") > 0)
	{
	    String queryString = uri.substring(uri.indexOf("?") + 1);
	    return getParamMap(queryString);
	}
	
	return null;
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

    public boolean isResolvingSPARQL()
    {
        return resolvingSPARQL;
    }

    public void setResolvingSPARQL(boolean resolvingSPARQL)
    {
	this.resolvingSPARQL = resolvingSPARQL;
    }

    public boolean isResolvingMapped()
    {
        return resolvingMapped;
    }
    
    public void setResolvingMapped(boolean resolvingMapped)
    {
        this.resolvingMapped = resolvingMapped;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
}