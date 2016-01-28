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
import com.hp.hpl.jena.util.LocationMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.core.MediaType;
import org.graphity.core.MediaTypes;
import org.graphity.core.exception.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data manager subclass that resolves URI to RDF/XML.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class DataManager extends org.graphity.core.util.jena.DataManager implements URIResolver
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    private final boolean resolvingUncached;
    protected boolean resolvingMapped = true;
    protected boolean resolvingSPARQL = true;
            
    public DataManager(LocationMapper mapper, MediaTypes mediaTypes,
            boolean cacheModelLoads, boolean preemptiveAuth, boolean resolvingUncached)
    {
	super(mapper, mediaTypes, cacheModelLoads, preemptiveAuth);
        this.resolvingUncached = resolvingUncached;
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

    public ClientResponse load(String filenameOrURI)
    {
        List<javax.ws.rs.core.MediaType> acceptedTypeList = new ArrayList();
        acceptedTypeList.addAll(getMediaTypes().getReadable(Model.class));
        acceptedTypeList.addAll(getMediaTypes().getReadable(ResultSet.class));
        javax.ws.rs.core.MediaType[] acceptedTypes = acceptedTypeList.toArray(new javax.ws.rs.core.MediaType[acceptedTypeList.size()]);
        
        return get(filenameOrURI, acceptedTypes);
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

        Model model = ModelFactory.createDefaultModel();
        readModel(model, filenameOrURI);
        
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

        model.add(super.loadModel(filenameOrURI));
        return model;
    }
    
    /**
     * Resolves relative URI to XML source.
     * @param href relative URI
     * @param base base URI
     * @return XML source
     * @throws javax.xml.transform.TransformerException 
     */
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	if (!href.isEmpty() && URI.create(href).isAbsolute())
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

        Model model = getFromCache(uri.toString());
        if (model == null) // URI not cached, 
        {
            if (log.isDebugEnabled())
            {
                log.debug("No cached Model for URI: {}", uri);
                log.debug("isMapped({}): {}", uri, isMapped(uri.toString()));
            }

            if (resolvingUncached(uri.toString()) ||
                    (isResolvingMapped() && isMapped(uri.toString())))
                try
                {
                    if (log.isTraceEnabled()) log.trace("Loading data for URI: {}", uri);
                    ClientResponse cr = load(uri.toString());
                    
                    if (cr.hasEntity())
                    {
                        if (cr.getType().isCompatible(MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE) || 
                                cr.getType().isCompatible(MediaType.APPLICATION_SPARQL_RESULTS_JSON_TYPE))
                            return getSource(cr.getEntity(ResultSetRewindable.class), uri.toString());

                        // by default, try to read Model
                        return getSource(cr.getEntity(Model.class), uri.toString());
                    }
                    
                    return getDefaultSource(); // return empty Model                    
                }
                catch (IllegalArgumentException | UriBuilderException | ClientException ex)
                {
                    if (log.isWarnEnabled()) log.warn("Could not read Model or ResultSet from URI: {}", uri);
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
     
    public boolean resolvingUncached(String filenameOrURI)
    {
	return resolvingUncached;
    }
    
    public boolean isResolvingSPARQL()
    {
        return resolvingSPARQL;
    }
    
    public boolean isResolvingMapped()
    {
        return resolvingMapped;
    }
    
}