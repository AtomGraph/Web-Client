/**
 *  Copyright 2025 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.resource;

import com.atomgraph.client.vocabulary.AC;
import java.net.URI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The application root. Proxy requests ({@code ?uri=}) never reach it - the
 * {@link com.atomgraph.client.filter.request.ProxyRequestFilter} aborts them pre-matching - so it
 * only handles the non-proxy cases: turning a SPARQL Protocol {@code ?endpoint=}/{@code ?query=}
 * pair into a redirect to the proxied query URL, and 404 when no resource URI is supplied.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
@Path("/")
public class Root
{

    private static final Logger log = LoggerFactory.getLogger(Root.class);

    /**
     * Handles non-proxy GET requests.
     *
     * @param endpoint SPARQL endpoint URI
     * @param query SPARQL query string
     * @param uriInfo URI information
     * @return redirect to the proxied SPARQL Protocol URL
     */
    @GET
    public Response get(@QueryParam("endpoint") URI endpoint, @QueryParam("query") String query, @Context UriInfo uriInfo)
    {
        // if SPARQL endpoint and query are provided, build a SPARQL Protocol URI and then redirect to a URI that proxies it
        if (endpoint != null && query != null)
        {
            if (log.isDebugEnabled()) log.debug("Redirecting from endpoint/query URL to a proxied URL");
            String encodedQuery = UriComponent.encode(query, UriComponent.Type.UNRESERVED); // manually encode query string because UriBuilder::build will complain about {}
            URI sparqlUrl = UriBuilder.fromUri(endpoint).queryParam(AC.query.getLocalName(), encodedQuery).build();
            String encodedSparqlUrl = UriComponent.encode(sparqlUrl.toString(), UriComponent.Type.UNRESERVED); // manually encode URL
            URI uri = uriInfo.getBaseUriBuilder().queryParam(AC.uri.getLocalName(), encodedSparqlUrl).build();

            return Response.seeOther(uri).build();
        }

        throw new NotFoundException("Resource URI not supplied");
    }

}
