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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.util.locator.LocatorLinkedData;
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

    protected boolean resolvingUncached = false;

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
    
    public DataManager(FileManager fMgr)
    {
	super(fMgr);
	addLocatorLinkedData();
	removeLocatorURL();
    }
    
    @Override
    public Model loadModel(String filenameOrURI)
    {
	return readModel(ModelFactory.createDefaultModel(), filenameOrURI);
    }

    @Override
    public Model readModel(Model model, String filenameOrURI)
    {
	String mappedURI = mapURI(filenameOrURI);
	if (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:")) // if URI is mapped and local
	{
	    log.debug("URI {} is mapped to {}, letting FileManager.readModel() handle it", filenameOrURI, mappedURI);
	    return super.readModel(model, mappedURI); // let FileManager handle
	}

	TypedStream stream = openNoMapOrNull(filenameOrURI);
	if (stream != null)
	{
	    log.debug("Opened filename or URI {} with TypedStream {}", filenameOrURI, stream);

	    String syntax = langFromContentType(stream.getMimeType());
	    log.debug("Syntax used to load Model: {}", syntax);

	    if (syntax != null) // do not read if MimeType/syntax are not known
	    {
		log.debug("URI {} syntax is {}, letting FileManager.readModel() handle it", filenameOrURI, syntax);
		return super.readModel(model, filenameOrURI, null, syntax); // let FileManager handle syntax
	    }
	}
	else
	    log.debug("Could not open stream for filename or URI: {}", filenameOrURI);
	
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
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);
	String uri = URI.create(base).resolve(href).toString();
	
	Model model = getFromCache(uri);
	if (model == null) // URI not cached, 
	{
	    log.debug("No cached Model for URI: {}", uri);

	    if (resolvingUncached)
		try
		{
		    log.debug("Loading Model for URI: {}", uri);
		    model = loadModel(uri);
		}
		catch (Exception ex)
		{
		    log.debug("Syntax error reading Model from URI", ex);
		    model = ModelFactory.createDefaultModel(); // return empty Model
		    //return null;
		}
	    else
	    {
		log.debug("Defaulting to empty Model for URI: {}", uri);
		model = ModelFactory.createDefaultModel(); // return empty Model
	    }
	}
	else
	    log.debug("Cached Model for URI: {}", uri);

	log.debug("Number of Model stmts read: {} from URI: {}", model.size(), uri);
	log.debug("Model for URI: {} is cached? {}", uri, hasCachedModel(uri));
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	model.write(stream);
	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
    }

    public boolean isResolvingUncached()
    {
	return resolvingUncached;
    }

    public void setResolvingUncached(boolean resolvingUncached)
    {
	this.resolvingUncached = resolvingUncached;
    }

    /*
    @Override
    public Model getFromCache(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return null; 
        return super.getFromCache(filenameOrURI) ;
    }
    
    @Override
    public boolean hasCachedModel(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return false ; 
        return super.hasCachedModel(filenameOrURI) ;
    }
    
    @Override
    // http://linuxsoftwareblog.com/?p=843
    public void addCacheModel(String uri, Model m)
    { 
        if ( getCachingModels() )
            super.addCacheModel(uri, m) ;
	
	
	Dataset ds = DatasetFactory.create();
	//ds.getNamedModel(uri).wr
		
	GraphStore graphStore = GraphStoreFactory.create(ds);
	//DatasetFactory.
	//UpdateFactory.
	//ds.getNamedModel(uri)
	//graphStore.
	//graphStore.ad
	//UpdateFactory.create().
	//Update data = new UpdateDataInsert();
	//ds.getNamedModel(uri)
    }
    */
    
}
