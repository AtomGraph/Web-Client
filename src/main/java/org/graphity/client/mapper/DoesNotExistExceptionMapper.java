/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.mapper;

import com.hp.hpl.jena.shared.DoesNotExistException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps (tunnels) one of Jena's remote loading 404 Not Found exceptions.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class DoesNotExistExceptionMapper implements ExceptionMapper<DoesNotExistException>
{

    @Override
    public Response toResponse(DoesNotExistException ae)
    {
	return Response.
		status(Response.Status.NOT_FOUND).
		build();
    }

}
