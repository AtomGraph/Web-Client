/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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

import org.apache.jena.rdf.model.ResourceFactory;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.atomgraph.core.exception.NotFoundException;
import org.apache.jena.query.DatasetFactory;

/**
 * Maps one of Jena's remote loading 404 Not Found exceptions.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
@Provider
public class NotFoundExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<NotFoundException>
{
    
    @Override
    public Response toResponse(NotFoundException ex)
    {
        return getResponseBuilder(DatasetFactory.create(DatasetFactory.create(toResource(ex, Response.Status.NOT_FOUND,
                        ResourceFactory.createResource("http://www.w3.org/2011/http-statusCodes#NotFound")).
                    getModel()))).
                status(Response.Status.NOT_FOUND).
                build();
    }
    
}