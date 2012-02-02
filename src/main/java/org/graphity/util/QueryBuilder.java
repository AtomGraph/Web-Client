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

package org.graphity.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class QueryBuilder
{
    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);
    private Model model = ModelFactory.createDefaultModel();
    private org.topbraid.spin.model.Query spinQuery = null;

    protected static QueryBuilder newInstance()
    {
        // Initialize system functions and templates
        SPINModuleRegistry.get().init(); 
	return new QueryBuilder();
    }
    
    public static QueryBuilder fromQuery(Query query, String uri)
    {
	return newInstance().query(query, uri);
    }

    public static QueryBuilder fromQueryString(String queryString)
    {
	return newInstance().query(QueryFactory.create(queryString), null);
    }

    public static QueryBuilder fromResource(Resource resource)
    {
	return newInstance().query(resource);
    }

    protected QueryBuilder query(Resource resource)
    {
	spinQuery = SPINFactory.asQuery(resource);

	return this;
    }
    
    protected QueryBuilder query(Query query, String uri)
    {
	ARQ2SPIN arq2Spin = new ARQ2SPIN(model);
	spinQuery = arq2Spin.createQuery(query, uri);
	
	return this;
    }
    
    public QueryBuilder bind(String name, String value)
    {
	//if (value.isURIResource())
	{
	    Resource var = getVariableByName("uri");
	    if (var != null)
	    {
		var.removeAll(SP.varName);
		ResourceUtils.renameResource(var, value);
	    }
	}
	
	return this;
    }
    
    protected Resource getVariableByName(String name)
    {
	ResIterator it = model.listResourcesWithProperty(SP.varName, name);
	if (it.hasNext()) return it.nextResource();
	else return null;
    }

    public Query build()
    {
	log.trace("Querystring generated from SPIN Model: {}", spinQuery.toString());

	return ARQFactory.get().createQuery(spinQuery);
    }

}
