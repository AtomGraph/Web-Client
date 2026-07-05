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

import com.atomgraph.client.MediaType;
import jakarta.ws.rs.client.Client;
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
 * during compilation. HTTP(S) locations are fetched with the SSL-configured JAX-RS {@link Client}
 * (app stylesheets served over HTTPS need its trust store / client certificate), requesting
 * {@code text/xsl}; local ({@code file:}/{@code jar:}/classpath) locations are handed back to Saxon
 * by returning {@code null}, so its default resolver opens them. Bytes are returned verbatim
 * (stylesheets are XML, not RDF).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class StylesheetResolver implements URIResolver
{

    private final Client client;

    /**
     * Constructs the resolver.
     *
     * @param client SSL-configured JAX-RS client for HTTP(S) stylesheet retrieval
     */
    public StylesheetResolver(Client client)
    {
        this.client = client;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
        URI baseURI = URI.create(base);
        URI uri = href.isEmpty() ? baseURI : baseURI.resolve(href);

        if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
            return null; // non-HTTP (file:/jar:/classpath): let Saxon's default resolver open it

        try (Response cr = getClient().target(uri).request().accept(MediaType.TEXT_XSL_TYPE).get())
        {
            if (!cr.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                throw new IOException("XSLT stylesheet could not be loaded over HTTP. Status: " + cr.getStatus() + ", URI: " + uri);

            // buffer the stylesheet stream so we can close the Response
            try (InputStream is = cr.readEntity(InputStream.class))
            {
                byte[] bytes = IOUtils.toByteArray(is);
                return new StreamSource(new ByteArrayInputStream(bytes), uri.toString());
            }
        }
        catch (IOException ex)
        {
            throw new TransformerException(ex);
        }
    }

    /**
     * Returns the JAX-RS client used for HTTP(S) retrieval.
     *
     * @return client
     */
    public Client getClient()
    {
        return client;
    }

}
