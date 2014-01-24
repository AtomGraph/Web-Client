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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

/**
 *
 * @author Martynas
 */
public class QueryProvider extends PerRequestTypeInjectableProvider<Context, Query> implements ContextResolver<Query>
{
    private static final Logger log = LoggerFactory.getLogger(QueryProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;

    public QueryProvider()
    {
        super(Query.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

    public OntModel getOntModel()
    {
	ContextResolver<OntModel> cr = getProviders().getContextResolver(OntModel.class, null);
	return cr.getContext(OntModel.class);
    }

    public OntClass getOntClass()
    {
	ContextResolver<OntClass> cr = getProviders().getContextResolver(OntClass.class, null);
	return cr.getContext(OntClass.class);
    }

    @Override
    public Injectable<Query> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<Query>()
	{
	    @Override
	    public Query getValue()
	    {
                return getQuery(getOntClass(), getUriInfo().getAbsolutePath());
	    }

	};
    }

    @Override
    public Query getContext(Class<?> type)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Given an RDF resource and an ontology class that it belongs to, returns a SPARQL query that can be used
     * to retrieve its description.
     * The ontology class must have a SPIN template call attached (using <code>spin:constraint</code>).
     * 
     * @param ontClass ontology class of the resource
     * @param uri URI of the resource
     * @return query object
     * @see org.topbraid.spin.model.TemplateCall
     */
    public Query getQuery(OntClass ontClass, URI uri)
    {
	return getQuery(getQueryCall(ontClass), getOntModel().createResource(uri.toString()));
    }
    
    /**
     * Given an ontology class, returns the SPIN template call attached to it.
     * The class must have a <code>spin:query</code> property with the template call resource as object.
     * 
     * @param ontClass ontology class
     * @return SPIN template call resource
     * @see org.topbraid.spin.model.TemplateCall
     */
    public TemplateCall getQueryCall(OntClass ontClass)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (!ontClass.hasProperty(SPIN.query))
	    throw new IllegalArgumentException("Resource OntClass must have a SPIN query Template");	    

	//RDFNode constraint = getModel().getResource(ontClass.getURI()).getProperty(SPIN.query).getObject();
        RDFNode constraint = ontClass.getProperty(SPIN.query).getObject();
	return SPINFactory.asTemplateCall(constraint);
    }
    
    /**
     * Given a SPIN template call and an RDF resource, returns a SPARQL query that can be used to retrieve
     * resource's description.
     * Following the convention of SPIN API, variable name <code>?this</code> has a special meaning and
     * is assigned to the value of the resource (which is usually request resource).
     * 
     * @param call SPIN template call resource
     * @param resource RDF resource
     * @return query object
     */
    public Query getQuery(TemplateCall call, Resource resource)
    {
	if (call == null) throw new IllegalArgumentException("TemplateCall cannot be null");
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	QuerySolutionMap qsm = new QuerySolutionMap();
	qsm.add("this", resource);
	ParameterizedSparqlString queryString = new ParameterizedSparqlString(call.getQueryString(), qsm);
	return queryString.asQuery();
    }

}