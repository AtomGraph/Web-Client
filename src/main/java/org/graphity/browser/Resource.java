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
import com.hp.hpl.jena.util.LocationMapper;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.graphity.ldp.model.impl.ResourceBase;
import org.graphity.model.ResourceFactory;
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
public class Resource extends ResourceBase
{
    private String uri = null;
    private Model model = null;
    private OntModel ontModel = null;
    private String endpointUri = null;
    private Query query = null;
    private QueryBuilder qb = null;
    private com.hp.hpl.jena.rdf.model.Resource spinRes = null;
    private MediaType acceptType = MediaType.APPLICATION_XHTML_XML_TYPE;

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    public Resource(@Context UriInfo uriInfo,
	@Context HttpHeaders headers,
	@QueryParam("uri") String uri,
	@QueryParam("endpoint-uri") String endpointUri,
	@QueryParam("accept") MediaType acceptType,
	@QueryParam("limit") @DefaultValue("10") long limit,
	@QueryParam("offset") @DefaultValue("0") long offset,
	@QueryParam("order-by") String orderBy,
	@QueryParam("desc") @DefaultValue("true") boolean desc)
    {
	super(uriInfo);
	this.uri = uri;
	this.endpointUri = endpointUri;
	log.debug("URI: {} Endpoint URI: {}", uri, endpointUri);
	
	if (acceptType != null) this.acceptType = acceptType;
	else
	    if (headers.getAcceptableMediaTypes().get(0).isCompatible(org.graphity.MediaType.APPLICATION_RDF_XML_TYPE) ||
		headers.getAcceptableMediaTypes().get(0).isCompatible(org.graphity.MediaType.TEXT_TURTLE_TYPE))
		    this.acceptType = headers.getAcceptableMediaTypes().get(0);
	log.debug("AcceptType: {} Acceptable MediaTypes: {}", this.acceptType, headers.getAcceptableMediaTypes());

	// ontology URI is base URI-dependent
	String ontologyUri = uriInfo.getBaseUri().toString();
	log.debug("Adding prefix mapping prefix: {} altName: {} ", ontologyUri, "ontology.ttl");
	((PrefixMapper)LocationMapper.get()).addAltPrefixEntry(ontologyUri, "ontology.ttl");
	log.debug("DataManager.get().getLocationMapper(): {}", DataManager.get().getLocationMapper());
	ontModel = OntDocumentManager.getInstance().
	    getOntology(uriInfo.getBaseUri().toString(), OntModelSpec.OWL_MEM_RDFS_INF);

	com.hp.hpl.jena.rdf.model.Resource resource = ontModel.createResource(getURI());
	log.debug("Resource: {} with URI: {}", resource, getURI());

	if (this.uri == null && resource.hasProperty(Graphity.query)) // only build Query if it's not default DESCRIBE
	{
	    spinRes = resource.getPropertyResourceValue(Graphity.query);
	    log.trace("Explicit query resource {} for URI {}", spinRes, getURI());

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

	    query = qb.build();
	    log.debug("Query generated with QueryBuilder: {}", query);
	}
	
	if (this.endpointUri == null && resource.hasProperty(Graphity.service))
	    this.endpointUri = resource.getPropertyResourceValue(Graphity.service).
		getPropertyResourceValue(ontModel.
		    getProperty("http://www.w3.org/ns/sparql-service-description#endpoint")).getURI();
    }

    @Override
    public Model getModel()
    {
	if (model == null)
	    try
	    {
		model = getResource().getModel();
	    }
	    catch (Exception ex)
	    {
		log.trace("Error while loading Model from URI: {}", uri, ex);
		throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
	    }
	    //if (model == null || model.isEmpty()) model = ResourceFactory.getResource(uri).getModel(); // fallback
	
	    if (model.isEmpty())
	    {
		log.trace("Loaded Model is empty");
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	    }
	
	return model;
    }

    private org.graphity.model.Resource getResource()
    {
	if (uri == null && endpointUri == null) // local URI
	{
	    if (query != null) return ResourceFactory.getResource(getOntModel(), query);
	    else return ResourceFactory.getResource(getOntModel(), getURI());
	}
	else // remote URI or endpoint
	{
	    if (endpointUri != null)
	    {
		if (query != null) return ResourceFactory.getResource(endpointUri, query);
		else return ResourceFactory.getResource(endpointUri, uri);
	    }
	    else return ResourceFactory.getResource(uri);
	}
    }

    protected void setModel(Model model)
    {
	this.model = model;
    }

    protected OntModel getOntModel()
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
    @Override
    public Response post(Model rdfPost)
    {
	log.debug("POSTed Model: {} size: {}", rdfPost, rdfPost.size());

	setModel(rdfPost);

	return getResponse();
    }
    
    @Override
    @GET
    @Produces({MediaType.APPLICATION_XHTML_XML + "; charset=UTF-8",org.graphity.MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", org.graphity.MediaType.TEXT_TURTLE + "; charset=UTF-8"})
    public Response getResponse()
    {
	if (getAcceptType() != null)
	{
	    // uses ModelProvider
	    if (getAcceptType().isCompatible(org.graphity.MediaType.APPLICATION_RDF_XML_TYPE))
	    {
		log.debug("Accept param: {}, writing RDF/XML", getAcceptType());
		return Response.ok(getModel(), org.graphity.MediaType.APPLICATION_RDF_XML_TYPE).
			tag(getEntityTag()).build();
	    }
	    if (getAcceptType().isCompatible(org.graphity.MediaType.TEXT_TURTLE_TYPE))
	    {
		log.debug("Accept param: {}, writing Turtle", getAcceptType());
		return Response.ok(getModel(), org.graphity.MediaType.TEXT_TURTLE_TYPE).
			tag(getEntityTag()).build();
	    }
	}

	return Response.ok(this).tag(getEntityTag()).build(); // uses ResourceXHTMLWriter
    }

    protected MediaType getAcceptType()
    {
	return acceptType;
    }

    @Override
    public Response put(Model model)
    {
	throw new WebApplicationException(405); // method not allowed
    }

    @Override
    public Response delete()
    {
	throw new WebApplicationException(405); // method not allowed
    }
    
}