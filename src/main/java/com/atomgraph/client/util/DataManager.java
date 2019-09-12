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
import com.sun.jersey.api.client.Client;
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
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class DataManager extends com.atomgraph.core.util.jena.DataManager implements URIResolver, UnparsedTextURIResolver // net.sf.saxon.NonDelegatingURIResolver ?
{

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    private final javax.ws.rs.core.MediaType[] acceptedTypes;
    private final MediaType[] acceptedXMLMediaTypes;
    private final boolean resolvingUncached;
    private final boolean resolvingMapped = true;
            
    public DataManager(LocationMapper mapper, Client client, MediaTypes mediaTypes,
            boolean preemptiveAuth, boolean resolvingUncached)
    {
        super(mapper, client, mediaTypes, preemptiveAuth);
        this.resolvingUncached = resolvingUncached;
        
        List<MediaType> acceptedTypeList = new ArrayList();
        acceptedTypeList.addAll(mediaTypes.getReadable(Model.class));
        acceptedTypeList.addAll(mediaTypes.getReadable(ResultSet.class));
        acceptedTypes = acceptedTypeList.toArray(new MediaType[acceptedTypeList.size()]);

        List<javax.ws.rs.core.MediaType> acceptableXMLMediaTypeList = new ArrayList();
        Map<String, String> q1 = new HashMap<>();
        q1.put("q", "1.0");
        acceptableXMLMediaTypeList.add(new MediaType(com.atomgraph.client.MediaType.TEXT_XSL_TYPE.getType(),
                com.atomgraph.client.MediaType.TEXT_XSL_TYPE.getSubtype(), q1));
        acceptableXMLMediaTypeList.add(new MediaType(com.atomgraph.core.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE.getType(),
                com.atomgraph.core.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE.getSubtype(), q1));
        Map<String, String> q09 = new HashMap<>();
        q09.put("q", "0.9");
        acceptableXMLMediaTypeList.add(new MediaType(com.atomgraph.core.MediaType.APPLICATION_RDF_XML_TYPE.getType(),
                com.atomgraph.core.MediaType.APPLICATION_RDF_XML_TYPE.getSubtype(), q09));
        Map<String, String> q05 = new HashMap<>();
        q05.put("q", "0.5");
        acceptableXMLMediaTypeList.add(new MediaType(MediaType.APPLICATION_XML_TYPE.getType(), MediaType.APPLICATION_XML_TYPE.getSubtype(), q05));
        Map<String, String> q04 = new HashMap<>();
        q04.put("q", "0.4");
        acceptableXMLMediaTypeList.add(new MediaType(MediaType.TEXT_XML_TYPE.getType(), MediaType.TEXT_XML_TYPE.getSubtype(), q04));

        acceptedXMLMediaTypes = acceptableXMLMediaTypeList.toArray(new MediaType[acceptableXMLMediaTypeList.size()]);
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
        
        if (hasCachedModel(uri.toString()) || (isResolvingMapped() && isMapped(uri.toString()))) // read mapped URIs (such as system ontologies) from a file
        {
            try
            {
                if (log.isDebugEnabled()) log.debug("hasCachedModel({}): {} isMapped({}): {}", uri, hasCachedModel(uri.toString()), uri, isMapped(uri.toString()));
                return getSource(loadModel(uri.toString()), uri.toString());
            }
            catch (IOException ex)
            {
                if (log.isWarnEnabled()) log.warn("Could not read Model from mapped URI: {}", uri);
                throw new TransformerException(ex);
            }
        }
                
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
        {
            if (log.isDebugEnabled()) log.debug("Resolving URI: {} against base URI: {}", href, base);
            
            ClientResponse cr = null;
            try
            {
                if (!resolvingUncached(uri.toString()))
                    throw new IOException("Dereferencing uncached URIs is disabled");

                cr = get(uri.toString(), getAcceptedXMLMediaTypes());

                if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                    throw new IOException("XML document could not be successfully loaded over HTTP. Status code: " + cr.getStatus());
                if (!isAcceptedMediaType(cr.getType(), getAcceptedXMLMediaTypes())) // response content type is an acceptable XML format
                    throw new IOException("MediaType '" + cr.getType() +"' is not accepted");

                // buffer the stream so we can close ClientResponse
                try (InputStream is = cr.getEntityInputStream())
                {
                    byte[] bytes = IOUtils.toByteArray(is);
                    return new StreamSource(new ByteArrayInputStream(bytes), uri.toString());
                }
            }
            catch (IOException ex)
            {
                if (log.isWarnEnabled()) log.warn("Could not read XML document from URI: {}", uri);
                throw new TransformerException(ex);
            }
            finally
            {
                if (cr != null) cr.close();
            }
        }
        
        return null;
    }

    @Override
    public Reader resolve(URI uri, String encoding, Configuration config) throws XPathException
    {
        ClientResponse cr = null;
        try
        {
            cr = getClient().resource(uri).get(ClientResponse.class);

            if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                throw new IOException("Unparsed text could not be successfully loaded over HTTP");

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
 
    public boolean isAcceptedMediaType(MediaType mediaType, MediaType[] mediaTypes)
    {
        for (MediaType accepted : mediaTypes)
            if (accepted.isCompatible(mediaType)) return true;
        
        return false;
    }
    
    public javax.ws.rs.core.MediaType[] getAcceptedMediaTypes()
    {
        return acceptedTypes;
    }
    
    public boolean resolvingUncached(String filenameOrURI)
    {
        return resolvingUncached;
    }
    
    public boolean isResolvingMapped()
    {
        return resolvingMapped;
    }
    
    public MediaType[] getAcceptedXMLMediaTypes()
    {
        return acceptedXMLMediaTypes;
    }

}