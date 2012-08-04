/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.ldp.provider;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class QueryParamProvider extends PerRequestTypeInjectableProvider<QueryParam, Query> // implements InjectableProvider<QueryParam, Parameter>
{
    private static final Logger log = LoggerFactory.getLogger(QueryParamProvider.class);
    
    @Context HttpContext hc = null;

    public QueryParamProvider(Type t)
    {
	super(t);
    }

    @Override
    public Injectable<Query> getInjectable(ComponentContext ic, QueryParam qp)
    {
	final String paramName = qp.value();
	return new Injectable<Query>()
	{
	    @Override
	    public Query getValue()
	    {
		String value = hc.getUriInfo().getQueryParameters().getFirst(paramName);
		if (value == null || value.isEmpty()) return null;
		    
		log.trace("Providing Injectable<Query> with @QueryParam({}) and value: {}", paramName, value);
		return QueryFactory.create(value);
	    }
	};
    }
} 