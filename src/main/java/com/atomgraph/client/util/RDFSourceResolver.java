/**
 *  Copyright 2026 Martynas Jusevičius <martynas@atomgraph.com>
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

import com.atomgraph.core.MediaTypes;
import com.atomgraph.core.client.GraphStoreClient;
import com.atomgraph.core.util.jena.PrefixGraphRepository;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves URIs to XML {@link Source}s for XSLT {@code document()} and to {@link Reader}s for
 * {@code unparsed-text()}, backed by a {@link PrefixGraphRepository} (bundled/cached graphs) and a
 * {@link GraphStoreClient} (HTTP). Replaces the XSLT-resolver role of the legacy {@code DataManagerImpl}
 * (the {@code FileManager}/{@code LocationMapper} stack is gone — mapping now lives in the repository).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class RDFSourceResolver implements URIResolver, UnparsedTextURIResolver
{

    private static final Logger log = LoggerFactory.getLogger(RDFSourceResolver.class);

    private final PrefixGraphRepository repository;
    private final GraphStoreClient gsc;
    private final boolean resolvingUncached;
    private final MediaType[] acceptedXMLMediaTypes;

    /**
     * Constructs the resolver.
     *
     * @param repository graph repository for bundled/cached graphs and URI→location mapping
     * @param gsc Graph Store client for HTTP retrieval
     * @param resolvingUncached if true, uncached HTTP URIs are dereferenced; otherwise an empty document is returned
     */
    public RDFSourceResolver(PrefixGraphRepository repository, GraphStoreClient gsc, boolean resolvingUncached)
    {
        this.repository = repository;
        this.gsc = gsc;
        this.resolvingUncached = resolvingUncached;

        List<MediaType> xmlTypes = new ArrayList<>();
        Map<String, String> q1 = new HashMap<>(); q1.put("q", "1.0");
        xmlTypes.add(new MediaType(com.atomgraph.core.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE.getType(), com.atomgraph.core.MediaType.APPLICATION_SPARQL_RESULTS_XML_TYPE.getSubtype(), q1));
        Map<String, String> q09 = new HashMap<>(); q09.put("q", "0.9");
        xmlTypes.add(new MediaType(com.atomgraph.core.MediaType.APPLICATION_RDF_XML_TYPE.getType(), com.atomgraph.core.MediaType.APPLICATION_RDF_XML_TYPE.getSubtype(), q09));
        Map<String, String> q05 = new HashMap<>(); q05.put("q", "0.5");
        xmlTypes.add(new MediaType(MediaType.APPLICATION_XML_TYPE.getType(), MediaType.APPLICATION_XML_TYPE.getSubtype(), q05));
        Map<String, String> q04 = new HashMap<>(); q04.put("q", "0.4");
        xmlTypes.add(new MediaType(MediaType.TEXT_XML_TYPE.getType(), MediaType.TEXT_XML_TYPE.getSubtype(), q04));
        this.acceptedXMLMediaTypes = xmlTypes.toArray(MediaType[]::new);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
        URI baseURI = URI.create(base);
        URI uri = href.isEmpty() ? baseURI : baseURI.resolve(href);

        // bundled/cached graphs (e.g. system ontologies) are read locally and serialized to RDF/XML
        if (getRepository().isCached(uri.toString()) || getRepository().isMapped(uri.toString()))
        {
            try
            {
                return getSource(ModelFactory.createModelForGraph(getRepository().get(uri.toString())), uri.toString());
            }
            catch (IOException ex)
            {
                if (log.isWarnEnabled()) log.warn("Could not read Model from mapped URI: {}", uri);
                throw new TransformerException(ex);
            }
        }

        if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https")))
        {
            if (log.isDebugEnabled()) log.debug("Resolving URI: {} against base URI: {}", href, base);

            try
            {
                if (!isResolvingUncached())
                {
                    if (log.isDebugEnabled()) log.debug("Dereferencing uncached URIs is disabled - returning empty document for URI: {}", uri);
                    return getSource(ModelFactory.createDefaultModel(), uri.toString());
                }

                try (Response cr = getGraphStoreClient().get(uri, getAcceptedXMLMediaTypes()))
                {
                    if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                        throw new IOException("XML document could not be successfully loaded over HTTP. Status code: " + cr.getStatus());
                    if (!isAcceptedMediaType(cr.getMediaType(), getAcceptedXMLMediaTypes()))
                        throw new IOException("MediaType '" + cr.getMediaType() + "' is not accepted");

                    try (InputStream is = cr.readEntity(InputStream.class))
                    {
                        byte[] bytes = IOUtils.toByteArray(is); // buffer so we can close the Response
                        return new StreamSource(new ByteArrayInputStream(bytes), uri.toString());
                    }
                }
            }
            catch (IOException ex)
            {
                if (log.isWarnEnabled()) log.warn("Could not read XML document from URI: {}", uri);
                throw new TransformerException(ex);
            }
        }

        return null;
    }

    @Override
    public Reader resolve(URI uri, String encoding, Configuration config) throws XPathException
    {
        try (Response cr = getGraphStoreClient().getClient().target(uri).request().get())
        {
            if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                throw new IOException("Unparsed text could not be successfully loaded over HTTP");

            try (InputStream is = cr.readEntity(InputStream.class))
            {
                byte[] bytes = IOUtils.toByteArray(is); // buffer so we can close the Response
                if (cr.getMediaType() != null && cr.getMediaType().getParameters().containsKey(MediaType.CHARSET_PARAMETER))
                    return new InputStreamReader(new ByteArrayInputStream(bytes), cr.getMediaType().getParameters().get(MediaType.CHARSET_PARAMETER));
                else
                    return new InputStreamReader(new ByteArrayInputStream(bytes));
            }
        }
        catch (IOException ex)
        {
            throw new WebApplicationException(ex);
        }
    }

    /**
     * Serializes an RDF model to an RDF/XML source.
     *
     * @param model RDF model
     * @param systemId system ID (usually the origin URI)
     * @return XML source
     * @throws IOException if serialization fails
     */
    public Source getSource(Model model, String systemId) throws IOException
    {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            model.write(stream);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
        }
    }

    /**
     * Serializes SPARQL XML results to an XML source.
     *
     * @param results SPARQL result set
     * @param systemId system ID (usually the origin URI)
     * @return XML source
     * @throws IOException if serialization fails
     */
    public Source getSource(ResultSet results, String systemId) throws IOException
    {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ResultSetFormatter.outputAsXML(stream, results);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()), systemId);
        }
    }

    /**
     * Checks whether a media type is among the accepted XML types.
     *
     * @param mediaType candidate media type
     * @param mediaTypes accepted types
     * @return true if accepted
     */
    public boolean isAcceptedMediaType(MediaType mediaType, MediaType[] mediaTypes)
    {
        for (MediaType accepted : mediaTypes)
            if (accepted.isCompatible(mediaType)) return true;
        return false;
    }

    /**
     * Returns the graph repository.
     *
     * @return repository
     */
    public PrefixGraphRepository getRepository()
    {
        return repository;
    }

    /**
     * Returns the Graph Store client.
     *
     * @return client
     */
    public GraphStoreClient getGraphStoreClient()
    {
        return gsc;
    }

    /**
     * Whether uncached HTTP URIs are dereferenced.
     *
     * @return true if resolving uncached URIs
     */
    public boolean isResolvingUncached()
    {
        return resolvingUncached;
    }

    /**
     * Returns the accepted XML media types for HTTP document resolution.
     *
     * @return accepted XML media types
     */
    public MediaType[] getAcceptedXMLMediaTypes()
    {
        return acceptedXMLMediaTypes;
    }

}
