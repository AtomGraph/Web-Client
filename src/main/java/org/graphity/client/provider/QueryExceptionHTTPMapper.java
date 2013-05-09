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

import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps (tunnels) Jena's remote query execution exception.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class QueryExceptionHTTPMapper implements ExceptionMapper<QueryExceptionHTTP>
{

    @Override
    public Response toResponse(QueryExceptionHTTP qe)
    {
	return Response.
		status(qe.getResponseCode()).
		//entity(qe.getResponseMessage()). // setting entity disables default Tomcat error page
		build();
    }

}