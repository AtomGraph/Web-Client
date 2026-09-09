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

import com.atomgraph.client.MediaTypes;
import com.atomgraph.client.util.HTMLMediaTypePredicate;
import com.atomgraph.core.exception.BadGatewayException;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.util.ModelUtils;
import com.atomgraph.core.util.ResultSetUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS request filter that intercepts {@code ?uri=} proxy requests and short-circuits the pipeline
 * via {@link ContainerRequestContext#abortWith(Response)}. The proxy is a global transport function,
 * not a document operation, so it is not modelled as a resource class: the filter forwards the
 * request method, entity stream and conditional headers to the target verbatim - no local entity
 * parsing on writes (an RDF/POST form body reaches the origin as
 * {@code application/x-www-form-urlencoded} for the origin to parse) - and converts the target's
 * response for the original caller.
 * <p>
 * External HTTP responses are dispatched on upstream {@code Content-Type} via Jena's live RIOT
 * registry (the same predicate {@code ModelProvider.isReadable} consults, and unlike the
 * {@code MediaTypes} snapshot it includes langs registered after class-loading, such as RDF/POST):
 * RDF langs parse into a {@link Model} and SPARQL results langs into a {@link ResultSet}, both
 * re-served through content negotiation - including (X)HTML via the XSLT writers, which is this
 * application's purpose. Error responses and non-RDF bodies relay verbatim: their bodies are
 * diagnostic or opaque representations, not negotiable content, and the origin's status and
 * validators must reach the client unchanged (a rejected write - 412 on a stale {@code If-Match},
 * 401/403 on an unauthorized delta - must surface as that status).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
@PreMatching
@Priority(Priorities.USER + 50) // after HttpMethodOverrideFilter (Priorities.USER), so ?_method= is already applied
public class ProxyRequestFilter implements ContainerRequestFilter
{

    private static final Logger log = LoggerFactory.getLogger(ProxyRequestFilter.class);
    private static final Pattern LINK_SPLITTER = Pattern.compile(",(?=\\s*<)");
    /**
     * End-to-end response headers forwarded verbatim from the upstream. Excludes hop-by-hop headers
     * (RFC 7230 §6.1), framing headers re-emitted by the container, origin-bound security headers
     * (CSP, HSTS, CORS), cookies, and {@code Content-Type}/{@code Link} which are set explicitly.
     */
    private static final Set<String> FORWARDED_RESPONSE_HEADERS = Set.of(
        HttpHeaders.ETAG,
        HttpHeaders.LAST_MODIFIED,
        HttpHeaders.CACHE_CONTROL,
        HttpHeaders.VARY,
        HttpHeaders.EXPIRES,
        HttpHeaders.CONTENT_LANGUAGE,
        HttpHeaders.CONTENT_DISPOSITION,
        HttpHeaders.CONTENT_LOCATION,
        HttpHeaders.LOCATION,
        HttpHeaders.RETRY_AFTER,
        "Age");
    /**
     * Conditional request headers forwarded verbatim to the upstream so preconditions are evaluated
     * at the origin: {@code If-Match}/{@code If-Unmodified-Since} carry optimistic-concurrency
     * validators on writes, {@code If-None-Match}/{@code If-Modified-Since} carry cache validation on
     * reads. Excludes {@code Authorization}/{@code Cookie} and {@code Range}, whose byte offsets do
     * not survive the Model re-serialization the proxy performs.
     */
    private static final Set<String> FORWARDED_REQUEST_HEADERS = Set.of(
        HttpHeaders.IF_MATCH,
        HttpHeaders.IF_NONE_MATCH,
        HttpHeaders.IF_MODIFIED_SINCE,
        HttpHeaders.IF_UNMODIFIED_SINCE);

    @Inject MediaTypes mediaTypes;
    @Inject Client client;
    @Context Request request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException
    {
        URI targetURI = resolveTargetURI(requestContext);
        if (targetURI == null) return; // not a proxy request - the root resource handles it

        // the ?accept= query param overrides content negotiation (used by the RDF export links)
        String acceptParam = requestContext.getUriInfo().getQueryParameters().getFirst("accept");
        if (acceptParam != null) requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, acceptParam);

        if (log.isDebugEnabled()) log.debug("Proxying {} {} → {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri(), targetURI);
        requestContext.abortWith(proxy(requestContext, getClient().target(targetURI)));
    }

    /**
     * Resolves the proxy target URI from the {@code ?uri=} query parameter, with the
     * {@code #fragment} stripped (servers do not receive fragment identifiers).
     * Returns null if this request should not be proxied.
     *
     * @param requestContext the current request context
     * @return target URI to proxy to, or null
     */
    protected URI resolveTargetURI(ContainerRequestContext requestContext)
    {
        String uriParam = requestContext.getUriInfo().getQueryParameters().getFirst("uri");
        if (uriParam == null) return null;

        URI targetURI = URI.create(uriParam);
        if (targetURI.getFragment() != null)
        {
            try
            {
                targetURI = new URI(targetURI.getScheme(), targetURI.getAuthority(), targetURI.getPath(), targetURI.getQuery(), null);
            }
            catch (URISyntaxException ex)
            {
                // should not happen when only removing the fragment
            }
        }

        return targetURI;
    }

    /**
     * Forwards the current request to the target and converts the target's response.
     *
     * @param requestContext the current request context
     * @param target proxy target
     * @return response for the original caller
     */
    protected Response proxy(ContainerRequestContext requestContext, WebTarget target)
    {
        try
        {
            Invocation.Builder builder = target.request(getReadableMediaTypes());

            // forward conditional request headers so preconditions reach the origin, which owns the
            // validators - without this the origin sees an unconditional request and a proxied If-Match
            // write silently loses its optimistic-concurrency guard
            for (String name : FORWARDED_REQUEST_HEADERS)
            {
                String value = requestContext.getHeaderString(name);
                if (value != null) builder.header(name, value);
            }

            Response clientResponse = requestContext.hasEntity()
                ? builder.method(requestContext.getMethod(),
                    Entity.entity(requestContext.getEntityStream(), requestContext.getMediaType()))
                : builder.method(requestContext.getMethod());

            try (clientResponse)
            {
                // special case for http <-> https 301/303 redirection, which the connector does not follow across schemes
                if (("GET".equalsIgnoreCase(requestContext.getMethod()) || HttpMethod.HEAD.equalsIgnoreCase(requestContext.getMethod())) &&
                    (clientResponse.getStatusInfo().toEnum().equals(Response.Status.SEE_OTHER) || clientResponse.getStatusInfo().toEnum().equals(Response.Status.MOVED_PERMANENTLY)) &&
                    ((target.getUri().getScheme().equals("http") && clientResponse.getLocation().getScheme().equals("https")) ||
                    (target.getUri().getScheme().equals("https") && clientResponse.getLocation().getScheme().equals("http"))))
                        return proxy(requestContext, getClient().target(clientResponse.getLocation()));

                return getResponse(clientResponse, target.getUri(), requestContext.getMethod());
            }
        }
        catch (MessageBodyProviderNotFoundException ex)
        {
            if (log.isWarnEnabled()) log.warn("Proxied URI {} returned non-RDF media type", target.getUri());
            throw new NotAcceptableException(ex);
        }
        catch (RiotException ex)
        {
            if (log.isWarnEnabled()) log.warn("Proxied URI {} returned body typed as RDF but unparseable", target.getUri());
            throw new BadGatewayException(ex);
        }
        catch (ProcessingException ex)
        {
            if (log.isWarnEnabled()) log.warn("Could not dereference proxied URI: {}", target.getUri());
            throw new BadGatewayException(ex);
        }
    }

    /**
     * Converts the proxy target's HTTP response into a JAX-RS response for the original caller.
     * RDF and SPARQL results bodies parse and re-serve through content negotiation (including
     * (X)HTML via the XSLT writers); HEAD, error and non-RDF responses relay verbatim.
     *
     * @param clientResponse response from the proxy target
     * @param targetURI upstream URI (used as the parse base URI hint for {@code ModelProvider})
     * @param method HTTP method
     * @return JAX-RS response to return to the original caller
     */
    protected Response getResponse(Response clientResponse, URI targetURI, String method)
    {
        // HEAD responses have no body by HTTP semantics. Routing them through the typed branches
        // below would parse an empty entity into an empty Model/ResultSet, then re-stamp
        // ETag/Last-Modified off that empty value - producing validators that disagree with the
        // upstream GET. Forward the upstream headers (including ETag) verbatim instead.
        if (HttpMethod.HEAD.equalsIgnoreCase(method))
        {
            Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
            if (clientResponse.getMediaType() != null) rb.type(clientResponse.getMediaType());
            return overlayHeaders(rb.build(), clientResponse, true);
        }

        if (clientResponse.getMediaType() == null)
        {
            Response.ResponseBuilder rb = Response.status(clientResponse.getStatus());
            return overlayHeaders(rb.build(), clientResponse, true);
        }

        // error responses relay verbatim: the body is a diagnostic representation, not negotiable
        // content, so it must not go through the Model/ResultSet re-serialization branches - parsing a
        // non-RDF or empty error body there throws and masks the origin's status as 502/406. A proxied
        // write that the origin rejects (412 on a stale If-Match, 401/403 on an unauthorized delta)
        // must reach the client as that status, with the origin's validators forwarded
        Response.Status.Family family = clientResponse.getStatusInfo().getFamily();
        if (family == Response.Status.Family.CLIENT_ERROR || family == Response.Status.Family.SERVER_ERROR)
        {
            clientResponse.bufferEntity();
            Response.ResponseBuilder rb = Response.status(clientResponse.getStatus()).
                type(clientResponse.getMediaType()).
                entity(clientResponse.readEntity(InputStream.class));
            return overlayHeaders(rb.build(), clientResponse, true);
        }

        // dispatch on the live Jena RIOT registry - the same predicate ModelProvider.isReadable uses,
        // so any RDF lang Jersey can read into a Model routes to the Model branch, including langs
        // registered after the MediaTypes static snapshot was captured (e.g. RDF/POST)
        MediaType upstreamCT = clientResponse.getMediaType();
        MediaType formatType = new MediaType(upstreamCT.getType(), upstreamCT.getSubtype()); // strip charset
        Lang lang = RDFLanguages.contentTypeToLang(formatType.toString());

        if (lang != null && ResultSetReaderRegistry.isRegistered(lang))
        {
            ResultSetRewindable results = clientResponse.readEntity(ResultSetRewindable.class);
            return overlayHeaders(getResponse(results, clientResponse.getStatusInfo()), clientResponse, false);
        }

        if (lang != null)
        {
            // base URI hint so ModelProvider resolves relative IRIs against the upstream URI
            clientResponse.getHeaders().putSingle(ModelProvider.REQUEST_URI_HEADER, targetURI.toString());
            Model model = clientResponse.readEntity(Model.class);
            // forward the origin's validators (replacing the ones the Model builder stamps off the re-serialized
            // bytes): a client editing the proxied document sends If-Match through this proxy to the origin, which
            // compares against its own ETag - a re-serialization validator would 412 every proxied write
            return overlayHeaders(getResponse(model, clientResponse.getStatusInfo()), clientResponse, true);
        }

        // upstream is neither RDF nor SPARQL results - pipe raw bytes
        // buffer so the stream remains readable after try-with-resources closes the client response
        clientResponse.bufferEntity();
        InputStream entity = clientResponse.readEntity(InputStream.class);

        Response.ResponseBuilder rb = Response.status(clientResponse.getStatus()).
            type(upstreamCT).
            entity(entity);

        return overlayHeaders(rb.build(), clientResponse, true);
    }

    /**
     * Copies the upstream {@code Link} and end-to-end cache/content headers onto the given
     * built response, replacing any locally stamped values. {@code ETag}/{@code Last-Modified}
     * are skipped when {@code copyValidators} is {@code false} (the ResultSet branch), where the
     * builder-stamped validators stand.
     *
     * @param response the response built by the typed or raw branch
     * @param clientResponse upstream response to copy headers from
     * @param copyValidators whether to forward {@code ETag} and {@code Last-Modified}
     * @return response with overlaid upstream headers
     */
    protected Response overlayHeaders(Response response, Response clientResponse, boolean copyValidators)
    {
        Response.ResponseBuilder rb = Response.fromResponse(response);

        // forward all Link headers from the external response so the client receives remote hypermedia
        String linkHeader = clientResponse.getHeaderString(HttpHeaders.LINK);
        if (linkHeader != null)
            for (String part : LINK_SPLITTER.split(linkHeader))
                rb.header(HttpHeaders.LINK, part.trim());

        for (String name : FORWARDED_RESPONSE_HEADERS)
        {
            if (!copyValidators && (HttpHeaders.ETAG.equalsIgnoreCase(name) || HttpHeaders.LAST_MODIFIED.equalsIgnoreCase(name))) continue;
            String value = clientResponse.getHeaderString(name);
            if (value != null) rb.header(name, null).header(name, value); // replace, not append - the upstream value overlays any locally stamped one
        }

        return rb.build();
    }

    /**
     * Builds a response for the given RDF model with content negotiation, including (X)HTML.
     *
     * @param model RDF model
     * @param statusType response status
     * @return JAX-RS response
     */
    protected Response getResponse(Model model, Response.StatusType statusType)
    {
        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariants(getMediaTypes().getWritable(Model.class),
                new ArrayList<>(),
                new ArrayList<>());

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                model,
                null,
                new EntityTag(Long.toHexString(ModelUtils.hashModel(model))),
                variants,
                new HTMLMediaTypePredicate()).
            getResponseBuilder().
            status(statusType).
            build();
    }

    /**
     * Builds a response for the given SPARQL result set with content negotiation, including (X)HTML.
     *
     * @param resultSet SPARQL results (rewindable so we can hash without consuming)
     * @param statusType response status
     * @return JAX-RS response
     */
    protected Response getResponse(ResultSetRewindable resultSet, Response.StatusType statusType)
    {
        long hash = ResultSetUtils.hashResultSet(resultSet);
        resultSet.reset();

        List<Variant> variants = com.atomgraph.core.model.impl.Response.getVariants(getMediaTypes().getWritable(ResultSet.class),
                new ArrayList<>(),
                new ArrayList<>());

        return new com.atomgraph.core.model.impl.Response(getRequest(),
                resultSet,
                null,
                new EntityTag(Long.toHexString(hash)),
                variants,
                new HTMLMediaTypePredicate()).
            getResponseBuilder().
            status(statusType).
            build();
    }

    /**
     * Returns the outbound {@code Accept} types: everything readable as a model or SPARQL results.
     *
     * @return readable media types
     */
    protected MediaType[] getReadableMediaTypes()
    {
        List<MediaType> readable = new ArrayList<>();
        readable.addAll(getMediaTypes().getReadable(Model.class));
        readable.addAll(getMediaTypes().getReadable(ResultSet.class));
        return readable.toArray(MediaType[]::new);
    }

    /**
     * Returns the media types registry used for content negotiation and outbound {@code Accept} headers.
     *
     * @return media types
     */
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }

    /**
     * Returns the HTTP client used to reach proxy targets.
     *
     * @return HTTP client
     */
    public Client getClient()
    {
        return client;
    }

    /**
     * Returns the JAX-RS request.
     *
     * @return request
     */
    public Request getRequest()
    {
        return request;
    }

}
