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

package org.graphity.browser;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.LocationMapper;
import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.MediaType;
import org.graphity.model.impl.LinkedDataResourceImpl;
import org.graphity.util.QueryBuilder;
import org.graphity.util.locator.PrefixMapper;
import org.graphity.util.manager.DataManager;
import org.graphity.vocabulary.Graphity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/{path: .*}")
public class Resource extends LinkedDataResourceImpl
{
    @Context private UriInfo uriInfo = null;
    private @QueryParam("limit") @DefaultValue("10") long limit;
    private @QueryParam("offset") @DefaultValue("0") long offset;
    private @QueryParam("order-by") String orderBy = null;
    private @QueryParam("desc") @DefaultValue("true") boolean desc;

    private static final Logger log = LoggerFactory.getLogger(Resource.class);
    
    @PostConstruct
    public void init()
    {
	String ontologyUri = uriInfo.getBaseUri().toString();
	log.debug("Adding prefix mapping prefix: {} altName: {} ", ontologyUri, "ontology.ttl");
	((PrefixMapper)LocationMapper.get()).addAltPrefixEntry(ontologyUri, "ontology.ttl");
	log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
    }

    @Override
    public Model getModel()
    {
	Model model = null;

	if (getEndpointURI() == null && getAbsolutePath().equals(getURI())) // local URI
	{
	    log.debug("Querying local OntModel with Query: {}", getQuery());
	    model = DataManager.get().loadModel(getOntModel(), getQuery());
	}
	else
	    model = super.getModel();

	if (model.isEmpty()) // || !model.containsResource(model.createResource(uri)))
	    throw new WebApplicationException(Response.Status.NOT_FOUND);

	return model;
    }

    public OntModel getOntModel()
    {
	return OntDocumentManager.getInstance().
	    getOntology(uriInfo.getBaseUri().toString(), OntModelSpec.OWL_MEM_RDFS_INF);
    }

    protected com.hp.hpl.jena.rdf.model.Resource getResource()
    {
	return getOntModel().createResource(getURI());
    }

    public com.hp.hpl.jena.rdf.model.Resource getSPINResource()
    {
	if (getResource().hasProperty(Graphity.query))
	    return getResource().getPropertyResourceValue(Graphity.query);
	
	ARQ2SPIN arq2SPIN = new ARQ2SPIN(ModelFactory.createDefaultModel());
	return arq2SPIN.createQuery(super.getQuery(), null); // turn default DESCRIBE into SPIN Resource
    }

    public QueryBuilder getQueryBuilder(com.hp.hpl.jena.rdf.model.Resource queryRes)
    {
	if (SPINFactory.asQuery(queryRes) instanceof Select) // wrap SELECT into CONSTRUCT { ?s ?p ?o }
	{
	    QueryBuilder selectBuilder = QueryBuilder.fromResource(queryRes).
		    limit(limit).
		    offset(offset);
	    if (orderBy != null) selectBuilder.orderBy(orderBy, desc);
	    
	    return QueryBuilder.fromConstructTemplate(getOntModel().getResource(Graphity.SubjectVar.getURI()),
		    getOntModel().getResource(Graphity.PredicateVar.getURI()), 
		    getOntModel().getResource(Graphity.ObjectVar.getURI())).
		subQuery(selectBuilder).
		optional(getOntModel().getResource(Graphity.SPOOptional.getURI()));
	}
	
	return QueryBuilder.fromResource(queryRes);
    }
    
    @Override
    public Query getQuery()
    {
	if (getSPINResource() != null)
	{
	    log.trace("Explicit query resource {} for URI {}", getSPINResource(), getURI());
	    Query query = getQueryBuilder(getSPINResource()).build();
	    log.debug("getQueryBuilder().build(): {}", query);
	    return query;
	}

	return super.getQuery();
    }

    public com.hp.hpl.jena.rdf.model.Resource getService()
    {
	return getResource().getPropertyResourceValue(Graphity.service);
    }
    
    @Override
    public String getEndpointURI()
    {
	if (getService() != null && super.getEndpointURI() == null)
	{
	    com.hp.hpl.jena.rdf.model.Resource endpoint = getService().
		getPropertyResourceValue(getOntModel().
		    getProperty("http://www.w3.org/ns/sparql-service-description#endpoint"));
	    if (endpoint == null) return null;

	    return endpoint.getURI();
	}

	return super.getEndpointURI();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addModel(Model rdfPost)
    {
	log.debug("POSTed Model: {} size: {}", rdfPost, rdfPost.size());

	getOntModel().add(rdfPost);
	// we add the RDF submitted with the RDF/POST form in this case
	// the posted Model could be saved using DataManager.putModel() for example
	
	return getResponse();
    }

    private String getAbsolutePath()
    {
	return uriInfo.getAbsolutePath().toString();
    }

}
