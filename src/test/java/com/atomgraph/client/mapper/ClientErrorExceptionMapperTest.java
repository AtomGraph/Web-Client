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
package com.atomgraph.client.mapper;

import com.atomgraph.client.MediaTypes;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that client errors keep their status and headers and are answered even when no RDF variant is acceptable.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ClientErrorExceptionMapperTest
{

    private static final MediaTypes MEDIA_TYPES = new MediaTypes();

    protected ClientErrorExceptionMapper getMapper(String method, String accept)
    {
        ContainerRequest request = new ContainerRequest(URI.create("http://localhost:8080/"), URI.create("http://localhost:8080/sparql"),
            method, null, new MapPropertiesDelegate(), null);
        if (accept != null) request.header(HttpHeaders.ACCEPT, accept);

        return new ClientErrorExceptionMapper()
        {
            @Override
            public Request getRequest()
            {
                return request;
            }

            @Override
            public HttpHeaders getHttpHeaders()
            {
                return request; // Jersey's container request is also the request's HttpHeaders
            }

            @Override
            public MediaTypes getMediaTypes()
            {
                return MEDIA_TYPES;
            }
        };
    }

    @Test
    public void testAllowIsPreserved()
    {
        Response response = getMapper("DELETE", "text/turtle").toResponse(new NotAllowedException("Method not allowed", "GET", new String[]{ "POST" }));

        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
        assertEquals(Set.of("GET", "POST"), response.getAllowedMethods());
    }

    @Test
    public void testArbitraryHeaderIsPreserved()
    {
        ClientErrorException ex = new ClientErrorException(Response.status(Response.Status.CONFLICT).header("Accept-Patch", "application/sparql-update").build());
        Response response = getMapper("PATCH", "text/turtle").toResponse(ex);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertEquals("application/sparql-update", response.getHeaderString("Accept-Patch"));
    }

    @Test
    public void testRepresentationHeadersAreReplaced()
    {
        ClientErrorException ex = new ClientErrorException(Response.status(Response.Status.BAD_REQUEST).
            header(HttpHeaders.CONTENT_TYPE, "application/json").
            header(HttpHeaders.CONTENT_LANGUAGE, "fr").
            build());
        Response response = getMapper("GET", "text/turtle").toResponse(ex);

        assertTrue(com.atomgraph.core.MediaType.TEXT_TURTLE_TYPE.isCompatible(response.getMediaType()));
        assertNull(response.getHeaderString(HttpHeaders.CONTENT_LANGUAGE));
    }

    @Test
    public void testUnacceptableVariantFallsBackToTurtle()
    {
        Response response = getMapper("GET", "application/sparql-results+json").toResponse(new BadRequestException("No query"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(com.atomgraph.core.MediaType.TEXT_TURTLE_TYPE.isCompatible(response.getMediaType()));
        assertTrue(response.getEntity() instanceof Model);
    }

    @Test
    public void testNotAcceptableIsAnswered()
    {
        Response response = getMapper("GET", "whatever/whatever").toResponse(new NotAcceptableException());

        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertTrue(com.atomgraph.core.MediaType.TEXT_TURTLE_TYPE.isCompatible(response.getMediaType()));
        assertTrue(response.getEntity() instanceof Model);
    }

    @Test
    public void testStatusOutsideEnum()
    {
        Response response = getMapper("POST", "text/turtle").toResponse(new ClientErrorException(422));

        assertEquals(422, response.getStatus());
    }

}
