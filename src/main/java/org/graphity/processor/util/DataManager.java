/*
 * Copyright (C) 2014 Martynas
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

package org.graphity.processor.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.core.UriBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.graphity.processor.locator.LocatorLinkedData;
import org.graphity.processor.locator.PrefixMapper;
import static org.graphity.client.util.DataManager.parseQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class DataManager extends org.graphity.server.util.DataManager
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    public DataManager(LocationMapper mapper, Context context, ServletContext servletContext)
    {
        super(mapper, context, servletContext);
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
    
}