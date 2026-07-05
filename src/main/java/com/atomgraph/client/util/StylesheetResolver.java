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

import com.atomgraph.core.client.GraphStoreClient;
import com.atomgraph.client.util.jena.PrefixGraphRepository;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;

/**
 * Resolves XSLT {@code xsl:import}/{@code xsl:include} URIs to raw stylesheet {@link Source}s
 * during compilation. HTTP locations are fetched via the {@link GraphStoreClient}; local
 * ({@code file:}/{@code jar:}/classpath) locations are handed back to Saxon by returning
 * {@code null}, so its default resolver opens them (relative imports against a {@code file:}/{@code jar:}
 * base need no location mapping). Unlike {@link RDFSourceResolver} the bytes are returned verbatim
 * (stylesheets are XML, not RDF). Replaces the legacy {@code XsltResolver} (which extended the
 * FileManager-based {@code DataManagerImpl}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class StylesheetResolver implements URIResolver
{

    private final PrefixGraphRepository repository;
    private final GraphStoreClient gsc;

    /**
     * Constructs the resolver.
     *
     * @param repository graph repository for URI→location mapping
     * @param gsc Graph Store client for HTTP retrieval
     */
    public StylesheetResolver(PrefixGraphRepository repository, GraphStoreClient gsc)
    {
        this.repository = repository;
        this.gsc = gsc;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
        URI baseURI = URI.create(base);
        URI uri = href.isEmpty() ? baseURI : baseURI.resolve(href);
        String location = getRepository().resolve(uri.toString());

        try
        {
            if (location.startsWith("http://") || location.startsWith("https://"))
            {
                try (Response cr = getGraphStoreClient().getClient().target(URI.create(location)).request().get();
                     InputStream is = cr.readEntity(InputStream.class))
                {
                    byte[] bytes = IOUtils.toByteArray(is); // buffer so we can close the Response
                    return new StreamSource(new ByteArrayInputStream(bytes), uri.toString());
                }
            }

            return null; // non-HTTP (file:/jar:/classpath): let Saxon's default resolver open it
        }
        catch (IOException ex)
        {
            throw new TransformerException(ex);
        }
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

}
