/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.query;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.vocabulary.SP;

/**
 * SPARQL SELECT query builder based on SPIN RDF syntax
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see QueryBuilder
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/">SPIN API</a>
 */
public class SelectBuilder extends QueryBuilder implements Select
{
    private static final Logger log = LoggerFactory.getLogger(SelectBuilder.class);

    private Select select = null;

    /**
     * Constructs builder from SPIN query
     * 
     * @param select SPIN SELECT resource
     */
    protected SelectBuilder(Select select)
    {
	super(select);
	this.select = select;
    }

    public static SelectBuilder fromSelect(Select select)
    {
	return new SelectBuilder(select);
    }

    public static SelectBuilder fromQuery(org.topbraid.spin.model.Query query)
    {
	return fromSelect((Select)query);
    }

    public static SelectBuilder fromResource(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Select Resource cannot be null");
	
	Query query = SPINFactory.asQuery(resource);
	if (query == null || !(query instanceof Select))
	    throw new IllegalArgumentException("SelectBuilder Resource must be a SPIN SELECT Query");

	return fromSelect((Select)query);
    }

    public static SelectBuilder fromQuery(com.hp.hpl.jena.query.Query query, String uri, Model model)
    {
	if (query == null) throw new IllegalArgumentException("Query cannot be null");
	
	ARQ2SPIN arq2spin = new ARQ2SPIN(model);
	return fromQuery(arq2spin.createQuery(query, uri));
    }

    public static SelectBuilder fromQuery(com.hp.hpl.jena.query.Query query, Model model)
    {
	return fromQuery(query, null, model);
    }

    public static SelectBuilder fromQueryString(String queryString, Model model)
    {
	return fromQuery(QueryFactory.create(queryString), model);
    }

    /**
     * SPIN SELECT resource
     * 
     * @return the query resource of this builder
     */
    @Override
    protected Select getQuery()
    {
	return select;
    }

    public SelectBuilder replaceLimit(Long limit)
    {
	if (log.isTraceEnabled()) log.trace("Removing LIMIT modifier");
	removeAll(SP.limit);
	
	if (limit != null)
	{
	    if (log.isTraceEnabled()) log.trace("Setting LIMIT modifier: {}", limit);
	    addLiteral(SP.limit, limit);
	}
	
	return this;
    }

    public SelectBuilder replaceOffset(Long offset)
    {
	if (log.isTraceEnabled()) log.trace("Removing OFFSET modifier");
	removeAll(SP.offset);
		
	if (offset != null)
	{
	    if (log.isTraceEnabled()) log.trace("Setting OFFSET modifier: {}", offset);
	    addLiteral(SP.offset, offset);
	}
	
	return this;
    }

    public SelectBuilder orderBy(String varName)
    {	
	return orderBy(varName, false);
    }

    public SelectBuilder orderBy(String varName, Boolean desc)
    {
	if (varName == null) throw new IllegalArgumentException("ORDER BY variable name cannot be null");
	
	return orderBy(SPINFactory.createVariable(getModel(), varName), desc);
    }
    
    public SelectBuilder orderBy(Variable var, Boolean desc)
    {
	if (var == null) throw new IllegalArgumentException("ORDER BY resource cannot be null");

	return orderBy((Resource)var, desc);
    }

    public SelectBuilder orderBy(Variable var)
    {
	return orderBy(var, false);
    }
    
    public SelectBuilder orderBy(Resource expression, Boolean desc)
    {
	if (desc == null) throw new IllegalArgumentException("DESC cannot be null");
	
	if (desc)
	    return orderBy(expression, SP.Desc);
	else
	    return orderBy(expression);
    }

    public SelectBuilder orderBy(Resource expression, Resource type)
    {
	if (expression == null) throw new IllegalArgumentException("ORDER BY expression cannot be null");
	if (type == null) throw new IllegalArgumentException("ORDER BY type cannot be null");
	if (!type.equals(SP.Asc) && !type.equals(SP.Desc)) throw new IllegalArgumentException("ORDER BY type must be sp:Asc or sp:Desc");

	Resource condition = getModel().createResource().addProperty(SP.expression, expression).
		addProperty(RDF.type, type);
	
	return orderBy(condition);
    }

    public SelectBuilder orderBy(Resource condition)
    {
	if (condition == null) throw new IllegalArgumentException("ORDER BY condition cannot be null");
	Variable var = SPINFactory.asVariable(condition);
	if (var != null && !isWhereVariable(var))
	{
	    if (log.isErrorEnabled()) log.error("Variable var: {} not in the WHERE pattern", var);
	    throw new IllegalArgumentException("Cannot ORDER BY variable '" + var + "' that is not specified in the WHERE pattern");
	}
	if (condition.hasProperty(SP.expression))
	{
	    Variable exprVar = SPINFactory.asVariable(condition.getPropertyResourceValue(SP.expression));
	    if (exprVar != null && !isWhereVariable(exprVar)) throw new IllegalArgumentException("Cannot ORDER BY variable that is not specified in the WHERE pattern");
	}
	if (log.isTraceEnabled()) log.trace("Setting ORDER BY condition: {}", condition);
	
	if (hasProperty(SP.orderBy))
	    getPropertyResourceValue(SP.orderBy).as(RDFList.class).add(condition);
	else
	    addProperty(SP.orderBy, getModel().createList(new RDFNode[]{condition}));
	
	return this;
    }
    
    public SelectBuilder replaceOrderBy(RDFList conditions)
    {
	if (log.isTraceEnabled()) log.trace("Removing all ORDER BY conditions");
	removeAll(SP.orderBy);
	
	if (conditions != null)
	{
	    if (log.isTraceEnabled()) log.trace("Setting ORDER BY conditions: {}", conditions);
	    addProperty(SP.orderBy, conditions);
	}
	
	return this;
    }
    
    @Override
    public List<Resource> getResultVariables()
    {
	return getQuery().getResultVariables();
    }

    @Override
    public boolean isDistinct()
    {
	return getQuery().isDistinct();
    }

    @Override
    public boolean isReduced()
    {
	return getQuery().isReduced();
    }

    @Override
    public Long getLimit()
    {
	return getQuery().getLimit();
    }

    @Override
    public Long getOffset()
    {
	return getQuery().getOffset();
    }

    @Override
    public List<String> getResultVariableNames()
    {
        return getQuery().getResultVariableNames();
    }

    public RDFList getOrderBy()
    {
        Resource orderBy = getQuery().getPropertyResourceValue(SP.orderBy);
        if (orderBy != null) return orderBy.as(RDFList.class);
        return null;
    }
    
}