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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.graphity.processor.model.SPARQLEndpointBase;
import org.graphity.processor.query.QueryBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.processor.vocabulary.VoID;
import org.graphity.processor.vocabulary.XHV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class DescriptionProvider extends org.graphity.server.provider.DescriptionProvider
{
    private static final Logger log = LoggerFactory.getLogger(DescriptionProvider.class);

    private final OntClass matchedOntClass;
    private final Resource dataset;
    private final SPARQLEndpoint endpoint;
    private final QueryBuilder queryBuilder;

    public DescriptionProvider()
    {
        super();
        
	matchedOntClass = matchOntClass(getRealURI(), uriInfo.getBaseUri());
	if (matchedOntClass == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with matched OntClass: {}", matchedOntClass);
	
        queryBuilder = setSelectModifiers(QueryBuilder.fromQuery(getQuery(matchedOntClass, ontResource), getModel()));
        if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with QueryBuilder: {}", queryBuilder);

	dataset = getDataset(matchedOntClass);
	if (dataset != null && dataset.hasProperty(VoID.sparqlEndpoint))
	    this.endpoint = new SPARQLEndpointBase(dataset.getPropertyResourceValue(VoID.sparqlEndpoint),
		    uriInfo, request, resourceConfig);
	else this.endpoint = endpoint;	    
	if (log.isDebugEnabled()) log.debug("Constructing ResourceBase with Dataset: {} and SPARQL endpoint: {}", dataset, this.endpoint);	

    }

    /**
     * Returns query builder, which is used to build SPARQL query to retrieve RDF description of this resource.
     * 
     * @return query builder
     */
    public QueryBuilder getQueryBuilder()
    {
	return queryBuilder;
    }

    /**
     * Returns query used to retrieve RDF description of this resource
     * 
     * @return query object
     */
    @Override
    public Query getQuery()
    {
	return getQueryBuilder().build();
    }

    @Override
    public Model getDescription()
    {
        return super.getDescription(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns RDF description of this resource.
     * 
     * @return RDF description
     * @see getQuery()
     */
    //@Override
    public Model getDescription()
    {	
	Model description = ModelFactory.createDefaultModel(); // super.describe();
	if (log.isDebugEnabled()) log.debug("Generating Response from description Model with {} triples", description.size());

	if (hasRDFType(LDP.Container)) // && !description.isEmpty()
	{
	    if (log.isDebugEnabled()) log.debug("Adding PageResource metadata: ldp:pageOf {}", this);
	    Resource page = description.createResource(getPageUriBuilder().build().toString()).
		addProperty(RDF.type, LDP.Page).
                addProperty(LDP.pageOf, this);

	    if (getOffset() >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:previous {}", getURI(), getPreviousUriBuilder().build().toString());
		page.addProperty(XHV.prev, description.createResource(getPreviousUriBuilder().build().toString()));
	    }

	    // no way to know if there's a next page without counting results (either total or in current page)
	    //int subjectCount = describe().listSubjects().toList().size();
	    //log.debug("describe().listSubjects().toList().size(): {}", subjectCount);
	    //if (subjectCount >= getLimit())
	    {
		if (log.isDebugEnabled()) log.debug("Adding page metadata: {} xhv:next {}", getURI(), getNextUriBuilder().build().toString());
		page.addProperty(XHV.next, description.createResource(getNextUriBuilder().build().toString()));
	    }
	}

	return description;
    }

    /**
     * Returns dataset resource, specified in an ontology class restriction.
     * 
     * @param ontClass the ontology class with the restriction
     * @return dataset resource or null, if no dataset restriction was found
     */
    public final Resource getDataset(OntClass ontClass)
    {
	RDFNode hasValue = getRestrictionHasValue(ontClass, VoID.inDataset);
	if (hasValue != null && hasValue.isResource()) return hasValue.asResource();

	return null;
    }

}
