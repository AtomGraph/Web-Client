/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client.provider;

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
