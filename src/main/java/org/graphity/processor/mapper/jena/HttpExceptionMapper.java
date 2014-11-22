/*
 * Copyright 2014 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.processor.mapper.jena;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.jena.atlas.web.HttpException;
import org.graphity.processor.mapper.ExceptionMapperBase;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class HttpExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<HttpException>
{
    
    @Override
    public Response toResponse(HttpException ex)
    {
	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(toResource(ex, Response.Status.INTERNAL_SERVER_ERROR,
                        null).
                    getModel()).
		build();
    }

}