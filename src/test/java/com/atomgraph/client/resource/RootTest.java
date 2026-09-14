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

import java.net.URI;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class RootTest
{

    private Root root;
    private UriInfo uriInfo;

    @BeforeEach
    public void setUp()
    {
        root = new Root();
        uriInfo = new ContainerRequest(URI.create("http://localhost:8080/"), URI.create("http://localhost:8080/"),
            "GET", null, new MapPropertiesDelegate(), null).getUriInfo();
    }

    @Test
    public void testEndpointQueryRedirect()
    {
        Response response = root.get(URI.create("https://remote.example/sparql"), "DESCRIBE-abc", uriInfo);

        assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
        // the redirect proxies the SPARQL Protocol URL through ?uri=
        assertEquals("http://localhost:8080/?uri=https%3A%2F%2Fremote.example%2Fsparql%3Fquery%3DDESCRIBE-abc",
            response.getLocation().toString());
    }

    @Test
    public void testNoTargetNotFound()
    {
        assertThrows(NotFoundException.class, () -> root.get(null, null, uriInfo));
    }

}
