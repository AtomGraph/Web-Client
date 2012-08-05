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

package org.graphity.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.*;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class QueryBuilder
{
    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);
    private org.topbraid.spin.model.Query spinQuery = null;
    private ARQ2SPIN arq2spin = null; //new ARQ2SPIN(model);
	
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

    public static QueryBuilder fromQuery(Query query)
    {
	return newInstance().query(query);
    }

    public static QueryBuilder fromConstructTemplate(Resource subject, Resource predicate, RDFNode object)
    {
	return newInstance().construct(subject, predicate, object);
    }

    public static QueryBuilder fromConstructTemplate(String subject, String predicate, String object)
    {
	return newInstance().construct(subject, predicate, object);
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
	arq2spin = new ARQ2SPIN(spinQuery.getModel());
	
	return this;
    }

    protected QueryBuilder query(Query query)
    {
	return query(query, null);
    }
    
    protected QueryBuilder query(Query query, String uri)
    {
	arq2spin = new ARQ2SPIN(ModelFactory.createDefaultModel());
	spinQuery = arq2spin.createQuery(query, uri);

	return this;
    }

    public QueryBuilder construct(TripleTemplate template)
    {
	Resource queryRes = ModelFactory.createDefaultModel().createResource(SP.Construct);
		
	((Construct)queryRes).getTemplates().add(template);

	return query(queryRes);
    }
    
    public QueryBuilder construct(String subject, String predicate, String object)
    {
	return construct(spinQuery.getModel().createResource().addProperty(SP.varName, subject),
		spinQuery.getModel().createResource().addProperty(SP.varName, predicate),
		spinQuery.getModel().createResource().addProperty(SP.varName, object));
    }
    
    public QueryBuilder construct(Resource subject, Resource predicate, RDFNode object)
    {
	Resource queryRes = ModelFactory.createDefaultModel().createResource(SP.Construct);
	
	queryRes.addProperty(SP.templates, queryRes.getModel().createList(new RDFNode[]{queryRes.
		getModel().createResource().
		    addProperty(SP.subject, subject).
		    addProperty(SP.predicate, predicate).
		    addProperty(SP.object, object)}));
	
	return query(queryRes);
    }

    public QueryBuilder where(Resource element)
    {
	return where(SPINFactory.asElement(element));
    }
    
    public QueryBuilder where(Element element)
    {
	//spinQuery.getWhereElements().add(element);
	//spinQuery.getWhere().add(element);

	RDFList where = spinQuery.getModel().createList(new RDFNode[]{element});

	if (!spinQuery.hasProperty(SP.where))
	    spinQuery.addProperty(SP.where, where);
	else
	    spinQuery.getPropertyResourceValue(SP.where).
		    as(RDFList.class).
		    add(element);
	
	return this;
    }
    
    public QueryBuilder subQuery(QueryBuilder builder)
    {	
	return subQuery(builder.buildSPIN());
    }

    public QueryBuilder subQuery(Select select)
    //public QueryBuilder subQuery(org.topbraid.spin.model.Query select)
    {
	SubQuery subQuery = SPINFactory.createSubQuery(spinQuery.getModel(), select);
	log.trace("SubQuery: {}", subQuery);
	return where(subQuery);
    }

    public QueryBuilder subQuery(Resource query)
    {
	spinQuery.getModel().add(query.getModel());
	return subQuery((Select)SPINFactory.asQuery(query));  // exception if not SELECT ?
    }

    public QueryBuilder optional(Resource optional)
    {
	return where(optional);
    }

    public QueryBuilder optional(Optional optional)
    {
	return where(optional);
    }
    
    public QueryBuilder optional(ElementOptional optional)
    {
	return optional(SPINFactory.createOptional(spinQuery.getModel(), arq2spin.createElementList(optional)));
    }

    public QueryBuilder limit(long limit)
    {
	spinQuery.removeAll(SP.limit);
	spinQuery.addLiteral(SP.limit, limit);
	
	return this;
    }

    public QueryBuilder offset(long offset)
    {
	spinQuery.removeAll(SP.offset);
	spinQuery.addLiteral(SP.offset, offset);
	
	return this;
    }

    public QueryBuilder orderBy(String varName)
    {	
	return orderBy(SPINFactory.createVariable(spinQuery.getModel(), varName));
    }

    public QueryBuilder orderBy(String varName, boolean desc)
    {	
	return orderBy(SPINFactory.createVariable(spinQuery.getModel(), varName), desc);
    }

    public QueryBuilder orderBy(Variable var)
    {
	return orderBy(var, false);
    }
    
    public QueryBuilder orderBy(Variable var, boolean desc)
    {
	log.debug("SPIN Query hasProperty(SP.orderBy): {}", spinQuery.hasProperty(SP.orderBy));
	log.debug("SPIN Query Model size(): {}", spinQuery.getModel().size());
	log.debug("spinQuery.getRequiredProperty(SP.orderBy): {}", spinQuery.getRequiredProperty(SP.orderBy));

	spinQuery.removeAll(SP.orderBy); // does not have effect??
	//spinQuery.getRequiredProperty(SP.orderBy).remove();
	//spinQuery.getModel().removeAll(spinQuery, SP.orderBy, null);
	
	log.debug("SPIN Query hasProperty(SP.orderBy): {}", spinQuery.hasProperty(SP.orderBy));
	log.debug("SPIN Query Model size(): {}", spinQuery.getModel().size());
	log.debug("spinQuery.getRequiredProperty(SP.orderBy): {}", spinQuery.getRequiredProperty(SP.orderBy));
		
	Resource bnode = spinQuery.getModel().createResource().addProperty(SP.expression, var);
	spinQuery.addProperty(SP.orderBy, spinQuery.getModel().createList(new RDFNode[]{bnode}));
	
	if (desc)
	    bnode.addProperty(RDF.type, SP.Desc);
	else
	    bnode.addProperty(RDF.type, SP.Asc);
	
	log.debug("SPIN Query Model size(): {}", spinQuery.getModel().size());
	
	return this;
    }

    public QueryBuilder bind(String name, String value)
    {
	//if (value.isURIResource())
	{
	    Resource var = getVariableByName(name);
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
	ResIterator it = spinQuery.getModel().listResourcesWithProperty(SP.varName, name);
	if (it.hasNext()) return it.nextResource();
	else return null;
    }

    public Query build()
    {
	log.trace("Querystring generated from SPIN Model: {}", buildSPIN().toString());

	return ARQFactory.get().createQuery(buildSPIN());
    }

    public org.topbraid.spin.model.Query buildSPIN()
    {
	log.trace("Querystring generated from SPIN Model: {}", spinQuery.toString()); // no PREFIXes

	// generate SPARQL query string
	StringBuilder sb = new StringBuilder();
	PrintContext pc = new StringPrintContext(sb);
	pc.setPrintPrefixes(true);
	spinQuery.print(pc);

	spinQuery.removeAll(SP.text);
	spinQuery.addLiteral(SP.text, spinQuery.getModel().createTypedLiteral(sb.toString()));

	return spinQuery;
    }

}
