/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.util;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.LocationMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import com.atomgraph.core.MediaTypes;
import com.atomgraph.core.exception.ClientException;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.io.ResultSetProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data manager subclass that resolves URI to RDF/XML.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class DataManager extends com.atomgraph.core.util.jena.DataManager implements URIResolver, UnparsedTextURIResolver // net.sf.saxon.NonDelegatingURIResolver ?
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    private final javax.ws.rs.core.MediaType[] acceptedTypes;
    private final boolean resolvingUncached;
    protected boolean resolvingMapped = true;
    protected boolean resolvingSPARQL = true;
            
    public DataManager(LocationMapper mapper, Client client, MediaTypes mediaTypes,
            boolean preemptiveAuth, boolean resolvingUncached)
    {
	super(mapper, client, mediaTypes, preemptiveAuth);
        this.resolvingUncached = resolvingUncached;
        
        List<javax.ws.rs.core.MediaType> acceptedTypeList = new ArrayList();
        acceptedTypeList.addAll(mediaTypes.getReadable(Model.class));
        acceptedTypeList.addAll(mediaTypes.getReadable(ResultSet.class));
        acceptedTypes = acceptedTypeList.toArray(new javax.ws.rs.core.MediaType[acceptedTypeList.size()]);        
    }

    public ClientResponse load(String filenameOrURI)
    {        
        return get(filenameOrURI, getAcceptedMediaTypes());
    }
    
    public boolean isMapped(String filenameOrURI)
    {
	String mappedURI = mapURI(filenameOrURI);
	return (!mappedURI.equals(filenameOrURI) && !mappedURI.startsWith("http:"));
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
        URI baseURI = URI.create(base);
        URI uri = href.isEmpty() ? baseURI : baseURI.resolve(href);
        
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
            {
            // TO-DO: unify both cases
            // requesting RDF
            if (!href.isEmpty() && URI.create(href).isAbsolute())
            {
                if (log.isDebugEnabled()) log.debug("Resolving URI: {} against base URI: {}", href, base);
                //URI uri = baseURI.resolve(href);
                return resolve(uri);
            }
            // requesting XML
            else
            {
                if (log.isDebugEnabled()) log.debug("Resolving URI: {} against base URI: {}", href, base);
                // empty href means stylesheet is referencing itself: document('')
                //URI uri = href.isEmpty() ? baseURI : baseURI.resolve(href);

                ClientResponse cr = null;
                try
                {
                    cr = getClient().resource(uri).
                        accept(MediaType.TEXT_XML_TYPE, MediaType.WILDCARD_TYPE).
                        get(ClientResponse.class);

                    if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                        throw new IOException("XSLT stylesheet could not be successfully loaded over HTTP");

                    // buffer the stylesheet stream so we can close ClientResponse
                    try (InputStream is = cr.getEntityInputStream())
                    {
                        byte[] bytes = IOUtils.toByteArray(is);
                        return new StreamSource(new ByteArrayInputStream(bytes), uri.toString());
                    }
                }
                catch (IOException ex)
                {
                    throw new WebApplicationException(ex);
                }
                finally
                {
                    if (cr != null) cr.close();
                }
            }
        }
        
        return null;
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
     * @throws javax.xml.transform.TransformerException
     */
    public Source resolve(URI uri) throws TransformerException
    {
        if (uri == null) throw new IllegalArgumentException("URI cannot be null");
        if (!uri.isAbsolute()) throw new IllegalArgumentException("URI to be resolved must be absolute");

        Model model = getFromCache(uri.toString());
        try
        {
            if (model == null) // URI not cached, 
            {
                if (log.isDebugEnabled()) log.debug("No cached Model for URI: {}", uri);

                if (isResolvingMapped() && isMapped(uri.toString()))
                {
                    if (log.isDebugEnabled()) log.debug("isMapped({}): {}", uri, isMapped(uri.toString()));
                    return getSource(loadModel(uri.toString()), uri.toString());
                }

                if (resolvingUncached(uri.toString()))
                    try
                    {                    
                        if (log.isTraceEnabled()) log.trace("Loading data for URI: {}", uri);
                        ClientResponse cr = null;

                        try
                        {
                            cr = load(uri.toString());

                            if (cr.hasEntity())
                            {
                                if (ResultSetProvider.isResultSetType(cr.getType()))
                                    return getSource(cr.getEntity(ResultSetRewindable.class), uri.toString());

                                if (ModelProvider.isModelType(cr.getType()))
                                    return getSource(cr.getEntity(Model.class), uri.toString());
                            }
                        }
                        finally
                        {
                            if (cr != null) cr.close();
                        }

                        return getDefaultSource(); // return empty Model                    
                    }
                    catch (IllegalArgumentException | ClientException | ClientHandlerException ex)
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
        catch (IOException ex)
        {
            if (log.isErrorEnabled()) log.error("Error resolving Source for URI: {}", uri);            
            throw new TransformerException(ex);
        }        
    }
    
    protected Source getDefaultSource() throws IOException
    {
	return getSource(ModelFactory.createDefaultModel(), null);
    }
    
    /**
     * Serializes RDF model to XML source.
     * 
     * @param model RDF model
     * @param systemId system ID (usually origin URI) of the source
     * @return XML source
     * @throws java.io.IOException
     */
    public Source getSource(Model model, String systemId) throws IOException
    {
	if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());
	try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            model.write(stream);
            if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
        }
    }

    /**
     * Serializes SPARQL XML results to XML source.
     * 
     * @param results SPARQL XML results
     * @param systemId system ID (usually origin URI) of the source
     * @return XML source
     * @throws java.io.IOException
     */
    public Source getSource(ResultSet results, String systemId) throws IOException
    {
	if (log.isDebugEnabled()) log.debug("ResultVars: {}", results.getResultVars());
	try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ResultSetFormatter.outputAsXML(stream, results);
            if (log.isDebugEnabled()) log.debug("SPARQL XML result bytes written: {}", stream.toByteArray().length);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
        }
    }
 
    public javax.ws.rs.core.MediaType[] getAcceptedMediaTypes()
    {
        return acceptedTypes;
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
   
    @Override
    public Reader resolve(URI uri, String encoding, Configuration config) throws XPathException
    {
        ClientResponse cr = null;
        try
        {
            cr = getClient().resource(uri).get(ClientResponse.class);

            if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                throw new IOException("XSLT stylesheet could not be successfully loaded over HTTP");

            InputStream is = cr.getEntityInputStream();
            byte[] bytes = IOUtils.toByteArray(is); // buffer the input stream so we can close ClientResponse
            if (cr.getType() != null && cr.getType().getParameters().containsKey("charset"))
                return new InputStreamReader(new ByteArrayInputStream(bytes), cr.getType().getParameters().get("charset")); // extract response content charset
            else
                return new InputStreamReader(new ByteArrayInputStream(bytes));
        }
        catch (IOException ex)
        {
            throw new WebApplicationException(ex);
        }
        finally
        {
            if (cr != null) cr.close();
        }
    }
    
}