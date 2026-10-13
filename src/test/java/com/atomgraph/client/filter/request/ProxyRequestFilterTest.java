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
package com.atomgraph.client.filter.request;

import java.net.URI;
import java.util.List;
import com.atomgraph.client.MediaTypes;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ProxyRequestFilterTest
{

    private ProxyRequestFilter filter;

    @BeforeEach
    public void setUp()
    {
        filter = new ProxyRequestFilter();
    }

    protected ContainerRequest getRequest(String requestUri)
    {
        return new ContainerRequest(URI.create("http://localhost:8080/"), URI.create(requestUri),
            "GET", null, new MapPropertiesDelegate(), null);
    }

    @Test
    public void testResolveTargetURIWithoutParam()
    {
        assertNull(filter.resolveTargetURI(getRequest("http://localhost:8080/")));
    }

    @Test
    public void testResolveTargetURI()
    {
        assertEquals(URI.create("https://remote.example/doc"),
            filter.resolveTargetURI(getRequest("http://localhost:8080/?uri=https%3A%2F%2Fremote.example%2Fdoc")));
    }

    @Test
    public void testResolveTargetURIStripsFragment()
    {
        assertEquals(URI.create("https://remote.example/doc"),
            filter.resolveTargetURI(getRequest("http://localhost:8080/?uri=https%3A%2F%2Fremote.example%2Fdoc%23this")));
    }

    @Test
    public void testOverlayHeadersSplitsCombinedLink()
    {
        // upstream sends one comma-joined Link line; each link-value must come out as its own header
        // value, because Link.valueOf() (the writer's parser) only reads a single link-value
        Response upstream = Response.ok().
            header(HttpHeaders.LINK, "<https://remote.example/ns#>; rel=https://example.org/ns#ontology, <https://remote.example/>; rel=https://example.org/ns#base").
            build();

        Response response = filter.overlayHeaders(Response.ok().build(), upstream, true);
        List<Object> linkValues = response.getHeaders().get(HttpHeaders.LINK);

        assertEquals(2, linkValues.size());
        assertEquals("<https://remote.example/ns#>; rel=https://example.org/ns#ontology", linkValues.get(0).toString());
        assertEquals("<https://remote.example/>; rel=https://example.org/ns#base", linkValues.get(1).toString());
    }

    @Test
    public void testOverlayHeadersForwardsEndToEndHeaders()
    {
        Response upstream = Response.ok().
            header(HttpHeaders.ETAG, "\"123\"").
            header(HttpHeaders.CACHE_CONTROL, "max-age=60").
            header("X-Custom", "not-forwarded").
            build();

        Response response = filter.overlayHeaders(Response.ok().build(), upstream, true);

        assertEquals("\"123\"", response.getHeaderString(HttpHeaders.ETAG));
        assertEquals("max-age=60", response.getHeaderString(HttpHeaders.CACHE_CONTROL));
        assertNull(response.getHeaderString("X-Custom"));
    }

    @Test
    public void testOverlayHeadersSkipsValidators()
    {
        Response upstream = Response.ok().
            header(HttpHeaders.ETAG, "\"123\"").
            header(HttpHeaders.LAST_MODIFIED, "Tue, 09 Sep 2026 12:00:00 GMT").
            header(HttpHeaders.CACHE_CONTROL, "max-age=60").
            build();

        Response response = filter.overlayHeaders(Response.ok().build(), upstream, false);

        assertNull(response.getHeaderString(HttpHeaders.ETAG));
        assertNull(response.getHeaderString(HttpHeaders.LAST_MODIFIED));
        assertEquals("max-age=60", response.getHeaderString(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    public void testOverlayHeadersReplacesLocallyStampedValue()
    {
        Response upstream = Response.ok().header(HttpHeaders.ETAG, "\"origin\"").build();
        Response local = Response.ok().header(HttpHeaders.ETAG, "\"local\"").build();

        Response response = filter.overlayHeaders(local, upstream, true);

        assertEquals(1, response.getHeaders().get(HttpHeaders.ETAG).size());
        assertEquals("\"origin\"", response.getHeaderString(HttpHeaders.ETAG));
    }

    /** A filter answering a request that accepts the given media type, as the proxy does once the upstream has answered */
    protected ProxyRequestFilter accepting(String accept)
    {
        ContainerRequest request = getRequest("http://localhost:8080/?uri=https%3A%2F%2Fremote.example%2Fsparql");
        request.header(HttpHeaders.ACCEPT, accept);
        filter.mediaTypes = new MediaTypes();
        filter.request = request;
        return filter;
    }

    @Test
    public void testBooleanResultIsServedAsBoolean()
    {
        try (Response response = accepting("application/sparql-results+json").getResponse(new SPARQLResult(true), Response.Status.OK))
        {
            assertEquals(200, response.getStatus());
            assertTrue(response.getMediaType().isCompatible(MediaType.valueOf("application/sparql-results+json")), response.getMediaType().toString());
            SPARQLResult entity = (SPARQLResult)response.getEntity();
            assertTrue(entity.isBoolean() && entity.getBooleanResult());
        }
    }

    @Test
    public void testBooleanResultTagsTellTrueFromFalse()
    {
        String trueTag, falseTag;
        try (Response response = accepting("application/sparql-results+xml").getResponse(new SPARQLResult(true), Response.Status.OK)) { trueTag = response.getHeaderString(HttpHeaders.ETAG); }
        try (Response response = accepting("application/sparql-results+xml").getResponse(new SPARQLResult(false), Response.Status.OK)) { falseTag = response.getHeaderString(HttpHeaders.ETAG); }
        assertNotEquals(trueTag, falseTag);
    }

    /** Jena has no boolean encoding in Protobuf, and HTML has no writer for a boolean: neither is offered */
    @Test
    public void testBooleanResultNotOfferedInFormatsWithoutABooleanWriter()
    {
        assertThrows(NotAcceptableException.class, () -> accepting("application/x-protobuf+sparql-results").getResponse(new SPARQLResult(true), Response.Status.OK));
        assertThrows(NotAcceptableException.class, () -> accepting("text/html").getResponse(new SPARQLResult(true), Response.Status.OK));
    }

    @Test
    public void testResultSetResultIsServedAsResultSet()
    {
        ResultSetRewindable rows = ResultSetFactory.copyResults(QueryExecutionFactory.create("SELECT ?x WHERE { VALUES ?x { 1 2 } }", ModelFactory.createDefaultModel()).execSelect());
        try (Response response = accepting("application/sparql-results+json").getResponse(new SPARQLResult(rows), Response.Status.OK))
        {
            assertEquals(200, response.getStatus());
            assertTrue(response.getEntity() instanceof ResultSetRewindable, "the result set arm keeps its own response");
        }
    }

    @Test
    public void testProxyRequestsAbort() throws Exception
    {
        ContainerRequest request = getRequest("http://localhost:8080/?uri=https%3A%2F%2Fremote.example%2Fdoc");
        assertTrue(filter.resolveTargetURI(request) != null);
        // the non-proxy request passes through without aborting
        ContainerRequest passThrough = getRequest("http://localhost:8080/?endpoint=https%3A%2F%2Fremote.example%2Fsparql&query=SELECT");
        filter.filter(passThrough);
        assertNull(passThrough.getAbortResponse());
    }

}
