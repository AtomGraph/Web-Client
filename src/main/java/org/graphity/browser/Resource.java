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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.LocationMapper;
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
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/{path: .*}")
public class Resource extends LinkedDataResourceImpl
{
    private UriInfo uriInfo = null;
    private OntModel ontModel = null;
    private QueryBuilder qb = null;
    private com.hp.hpl.jena.rdf.model.Resource resource, spinRes = null;

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    public Resource(@Context UriInfo uriInfo,
	@QueryParam("uri") String uri,
	@QueryParam("endpoint-uri") String serviceUri,
	@QueryParam("accept") String accept,
	@QueryParam("limit") @DefaultValue("10") long limit,
	@QueryParam("offset") @DefaultValue("0") long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") @DefaultValue("true") boolean desc)
    {
	super(uri == null ? uriInfo.getAbsolutePath().toString() : uri, serviceUri, accept);
	this.uriInfo = uriInfo;

	// ontology URI is base URI-dependent
	String ontologyUri = uriInfo.getBaseUri().toString();
	log.debug("Adding prefix mapping prefix: {} altName: {} ", ontologyUri, "ontology.ttl");
	((PrefixMapper)LocationMapper.get()).addAltPrefixEntry(ontologyUri, "ontology.ttl");
	log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	ontModel = OntDocumentManager.getInstance().
	    getOntology(uriInfo.getBaseUri().toString(), OntModelSpec.OWL_MEM_RDFS_INF);

	resource = ontModel.createResource(getURI());
	log.debug("Resource: {} with URI: {}", resource, getURI());

	if (resource.hasProperty(Graphity.query))
	{
	    spinRes = resource.getPropertyResourceValue(Graphity.query);
	    log.trace("Explicit query resource {} for URI {}", spinRes, getURI());

spinRes.canAs(Select.class);

	    if (SPINFactory.asQuery(spinRes) instanceof Select) // wrap SELECT into CONSTRUCT { ?s ?p ?o }
	    {
		log.trace("Explicit query is SELECT, wrapping into CONSTRUCT");

		QueryBuilder selectBuilder = QueryBuilder.fromResource(spinRes).
		    limit(limit).
		    offset(offset);
		if (orderBy != null) selectBuilder.orderBy(orderBy, desc);

		qb = QueryBuilder.fromConstructTemplate(ontModel.getResource(Graphity.SubjectVar.getURI()),
			ontModel.getResource(Graphity.PredicateVar.getURI()), 
			ontModel.getResource(Graphity.ObjectVar.getURI())).
		    subQuery(selectBuilder).
		    optional(ontModel.getResource(Graphity.SPOOptional.getURI()));
	    }
	    else
		qb = QueryBuilder.fromResource(spinRes); // CONSTRUCT
	    
	    setQuery(qb.build());
	    log.debug("Query generated with QueryBuilder: {}", getQuery());
	}
	//else
	//    qb = QueryBuilder.fromQuery(super.getQuery()); // default DESCRIBE
	
	if (getEndpointURI() == null && resource.hasProperty(Graphity.service))
	    setEndpointURI(resource.getPropertyResourceValue(Graphity.service).
		getPropertyResourceValue(ontModel.
		    getProperty("http://www.w3.org/ns/sparql-service-description#endpoint")).getURI());
    }

    @Override
    public Model getModel()
    {
	// local URI
	if (getEndpointURI() == null && uriInfo.getAbsolutePath().toString().equals(getURI()))
	{
	    log.debug("Querying local OntModel: {} with Query: {}", ontModel, getQuery());
	    return DataManager.get().loadModel(ontModel, getQuery());
	}
	
	return super.getModel();
    }

    public OntModel getOntModel()
    {
	return ontModel;
    }

    public com.hp.hpl.jena.rdf.model.Resource getSPINResource()
    {
	return spinRes;
    }

    public QueryBuilder getQueryBuilder()
    {
	return qb;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addModel(Model rdfPost)
    {
	log.debug("POSTed Model: {} size: {}", rdfPost, rdfPost.size());

	//getOntModel().add(rdfPost);
	// we add the RDF submitted with the RDF/POST form in this case
	// the posted Model could be saved using DataManager.putModel() for example
	
	return getResponse();
    }

}