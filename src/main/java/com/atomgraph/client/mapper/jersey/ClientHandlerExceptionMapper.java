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

package com.atomgraph.client.mapper.jersey;

import com.sun.jersey.api.client.ClientHandlerException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.atomgraph.client.mapper.ExceptionMapperBase;
import org.apache.jena.query.DatasetFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
@Provider
public class ClientHandlerExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<ClientHandlerException>
{

    @Override
    public Response toResponse(ClientHandlerException ex)
    {
        return com.atomgraph.core.model.impl.Response.fromRequest(getRequest()).
            getResponseBuilder(DatasetFactory.create(toResource(ex, Response.Status.INTERNAL_SERVER_ERROR, null).
                getModel()), getVariants()).
            status(Response.Status.INTERNAL_SERVER_ERROR).
            build();
    }
    
}
