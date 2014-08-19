/*
 * Copyright (C) 2014 Martynas
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

package org.graphity.processor.provider;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class QueryBuilderProvider extends PerRequestTypeInjectableProvider<Context, QueryBuilder> implements ContextResolver<QueryBuilder>
{
    private static final Logger log = LoggerFactory.getLogger(QueryBuilderProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;
    
    public QueryBuilderProvider()
    {
        super(QueryBuilder.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public Query getQuery()
    {
	//return getProviders().getContextResolver(Query.class, null).getContext(Query.class);
	return QueryFactory.create("DESCRIBE <" + getUriInfo().getAbsolutePath().toString() + ">");
    }

    public OntModel getOntModel() // needs QueryProvider registered as well!
    {
	return getProviders().getContextResolver(OntModel.class, null).getContext(OntModel.class);
    }
    
    public QueryBuilder getQueryBuilder()
    {
        return QueryBuilder.fromQuery(getQuery(), getOntModel());
    }

    @Override
    public Injectable<QueryBuilder> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<QueryBuilder>()
	{
	    @Override
	    public QueryBuilder getValue()
	    {
                return getQueryBuilder();
	    }
	};
    }

    @Override
    public QueryBuilder getContext(Class<?> type)
    {
        return getQueryBuilder();        
    }
    
}