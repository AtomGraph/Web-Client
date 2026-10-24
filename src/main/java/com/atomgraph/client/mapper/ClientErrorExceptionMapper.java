/*
 * Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atomgraph.client.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.jena.rdf.model.Resource;

/**
 * Maps client errors to an RDF description of the error.
 * The status and the headers of the exception's response are preserved (for example <code>Allow</code> on 405), with
 * the exception of the representation metadata, which describes the original entity and not the error description.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
@Provider
public class ClientErrorExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<ClientErrorException>
{

    /** Headers describing the representation, which the error description replaces. */
    public static final Set<String> REPRESENTATION_HEADERS;
    static
    {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_LANGUAGE,
            HttpHeaders.CONTENT_ENCODING, HttpHeaders.CONTENT_LOCATION, "Content-Range", HttpHeaders.ETAG,
            HttpHeaders.LAST_MODIFIED, HttpHeaders.VARY));
        REPRESENTATION_HEADERS = headers;
    }

    @Override
    public Response toResponse(ClientErrorException ex)
    {
        Response.StatusType status = ex.getResponse().getStatusInfo();
        Resource exRes = toResource(ex, status, null);
        
        Response.ResponseBuilder builder = getResponseBuilder(exRes.getModel()).
            status(status);

        for (Map.Entry<String, List<Object>> header : ex.getResponse().getHeaders().entrySet())
            if (!REPRESENTATION_HEADERS.contains(header.getKey()))
                for (Object value : header.getValue()) builder.header(header.getKey(), value);

        return builder.build();
    }
    
}
