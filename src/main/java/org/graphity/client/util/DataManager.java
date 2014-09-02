/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.util;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.LocationMapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import javax.servlet.ServletContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class DataManager extends org.graphity.processor.util.DataManager implements URIResolver
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
        
    public DataManager(LocationMapper mapper, Context context, ServletContext servletContext, UriInfo uriInfo)
    {
	super(mapper, context, servletContext);
        this.uriInfo = uriInfo;
    }

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
                                query, parseParamMap(uri.toString())));
                        }
                        if (query.isConstructType() || query.isDescribeType())
                        {
                            if (log.isTraceEnabled()) log.trace("Loading Model for URI: {} using Query: {}", uri, query);
                            return getSource(loadModel(UriBuilder.fromUri(uri).
                                    replaceQuery(null).
                                    build().toString(),
                                query, parseParamMap(uri.toString())));
                        }
                    }

                    if (log.isTraceEnabled()) log.trace("Loading Model for URI: {}", uri);
                    return getSource(loadModel(uri.toString()));
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
            return getSource(model);
        }
    }
    
    protected Source getDefaultSource()
    {
	return getSource(ModelFactory.createDefaultModel());
    }
    
    protected Source getSource(Model model)
    {
	if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	model.write(stream);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }

    protected Source getSource(ResultSet results)
    {
	if (log.isDebugEnabled()) log.debug("ResultVars: {}", results.getResultVars());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ResultSetFormatter.outputAsXML(stream, results);
	
	if (log.isDebugEnabled()) log.debug("SPARQL XML result bytes written: {}", stream.toByteArray().length);
	
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
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
		if (log.isWarnEnabled()) log.warn("Could not URL-decode query string component", ex);
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
		if (log.isDebugEnabled()) log.debug("Query string: {} from URI: {}", sparqlString, uri);

		return QueryFactory.create(sparqlString);
	    }
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