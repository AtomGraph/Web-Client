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
package org.graphity.browser.resource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.graphity.browser.Resource;
import org.graphity.util.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/search")
public class SearchResource extends Resource
{
    private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

    private @QueryParam("query") String query = null;
    private @QueryParam("stylesheet") String stylesheet = null;
	
    /*
    @GET
    @Produces("text/html; charset=UTF-8")
    @Override
    public Response getResponse()
    {
	log.debug("Search query: {}", query);
	return Response.ok(query).build();
    }
    */
    
    public Model getSPINModel()
    {
	return ResourceUtils.reachableClosure(getSPINResource());
    }

/*
    @Override
    public Query getSPINQuery()
    {
	OntModel queryModel = ModelFactory.createOntologyModel();
	queryModel.add(getSPINModel());
	
	// TO-DO: this will break if there are several regex() queries in the ontology!

	com.hp.hpl.jena.rdf.model.Resource regex = null;
	ResIterator it = queryModel.listResourcesWithProperty(RDF.type, SP.regex);
	if (it.hasNext()) regex = it.nextResource();

	regex.removeAll(SP.arg2).addProperty(SP.arg2, query);
	log.debug("Regex match string: {}", regex.getRequiredProperty(SP.arg2));

	//OntResource queryRes = queryModel.getOntResource(getSPINResource());
	com.hp.hpl.jena.rdf.model.Resource queryRes = queryModel.getResource(getSPINResource().getURI());
	
	log.trace("Explicit query resource {} for URI {}", queryRes, getURI());
	if (queryRes == null) return null;
	
	return QueryBuilder.fromResource(queryRes).
	    build();
    }
*/

}
