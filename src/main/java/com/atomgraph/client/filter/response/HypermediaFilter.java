/*
 * Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atomgraph.client.filter.response;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.RDF;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.vocabulary.RDFS;
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.core.util.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
@Provider
public class HypermediaFilter implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger(HypermediaFilter.class);
   
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        /*
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (response == null) throw new IllegalArgumentException("ContainerResponse cannot be null");

        if (!response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL)) return response;
        
        Model model = getModel(response.getEntity());
        if (model == null) return response;

        Resource requestUri = model.createResource(request.getRequestUri().toString());

        if (request.getQueryParameters().getFirst(AC.forClass.getLocalName()) != null)
        {        
            try
            {
                MultivaluedMap<String, Object> headerMap = response.getHttpHeaders();
                URI ontologyHref = getOntologyURI(headerMap);
                URI typeHref = getTypeURI(headerMap);
                if (ontologyHref == null || typeHref == null) return response;

                OntModelSpec ontModelSpec = getOntModelSpec(getRules(headerMap, "Rules"));            
                OntModel ontModel = getOntModel(ontologyHref.toString(), ontModelSpec);

                long oldCount = model.size();

                String forClassURI = requestUri.getPropertyResourceValue(AC.forClass).getURI();
                OntClass forClass = ontModel.getOntClass(forClassURI);
                if (forClass == null) throw new OntClassNotFoundException("OntClass '" + forClassURI + "' not found in sitemap");

                // TO-DO: check if Rules still necessary or does SPIN handle spin:constructor inheritance
                requestUri.addProperty(AC.constructor, addInstance(model, forClass)); // connects constructor state to CONSTRUCTed template

                if (log.isDebugEnabled()) log.debug("Added HATEOAS transitions to the response RDF Model for resource: {} # of statements: {}", requestUri.getURI(), model.size() - oldCount);
                response.setEntity(model);
            }
            catch (URISyntaxException ex)
            {
                return response;
            }
        }
*/
        
        return response;
    }
    
    public Model getModel(Object entity)
    {
        if (entity instanceof Model) return (Model)entity;
        
        return null;
    }

    public final Resource getResource(OntClass ontClass, AnnotationProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isResource())
            return ontClass.getPropertyValue(property).asResource();
        
        return null;
    }
    
    public URI getTypeURI(MultivaluedMap<String, Object> headerMap) throws URISyntaxException
    {
        return getLinkHref(headerMap, "Link", RDF.type.getLocalName());
    }

    public URI getOntologyURI(MultivaluedMap<String, Object> headerMap) throws URISyntaxException
    {
        return getLinkHref(headerMap, "Link", LDT.ontology.getURI());
    }
    
    public URI getLinkHref(MultivaluedMap<String, Object> headerMap, String headerName, String rel) throws URISyntaxException
    {
	if (headerMap == null) throw new IllegalArgumentException("Header Map cannot be null");
	if (headerName == null) throw new IllegalArgumentException("String header name cannot be null");
        if (rel == null) throw new IllegalArgumentException("Property Map cannot be null");
        
        List<Object> links = headerMap.get(headerName);
        if (links != null)
        {
            Iterator<Object> it = links.iterator();
            while (it.hasNext())
            {
                String linkHeader = it.next().toString();
                Link link = Link.valueOf(linkHeader);
                if (link.getRel().equals(rel)) return link.getHref();
            }
        }
        
        return null;
    }
    
    public OntModelSpec getOntModelSpec(List<Rule> rules)
    {
        OntModelSpec ontModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
        
        if (rules != null)
        {
            Reasoner reasoner = new GenericRuleReasoner(rules);
            //reasoner.setDerivationLogging(true);
            //reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
            ontModelSpec.setReasoner(reasoner);
        }
        
        return ontModelSpec;
    }
    
    public final List<Rule> getRules(MultivaluedMap<String, Object> headerMap, String headerName)
    {
        String rules = getRulesString(headerMap, headerName);
        if (rules == null) return null;
        
        return Rule.parseRules(rules);
    }
    
    public String getRulesString(MultivaluedMap<String, Object> headerMap, String headerName)
    {
	if (headerMap == null) throw new IllegalArgumentException("Header Map cannot be null");
	if (headerName == null) throw new IllegalArgumentException("String header name cannot be null");

        Object rules = headerMap.getFirst(headerName);
        if (rules != null) return rules.toString();
        
        return null;
    }
    
    /*
    public OntModel getOntModel(String ontologyURI, OntModelSpec ontModelSpec)
    {
        return new OntologyProvider().getOntModel(ontologyURI, ontModelSpec);
    }
      
    public Resource addInstance(Model targetModel, OntClass forClass)
    {
        if (log.isDebugEnabled()) log.debug("Invoking constructor on class: {}", forClass);
        addClass(forClass, targetModel); // TO-DO: remove when classes and constraints are cached/dereferencable
        return new ConstructorBase().construct(forClass, targetModel);
    }
    */
    
    // TO-DO: this method should not be necessary when system ontologies/classes are dereferencable! -->
    public void addClass(OntClass forClass, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");    

        String queryString = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
"PREFIX  spin: <http://spinrdf.org/spin#>\n" +
"\n" +
"DESCRIBE ?Class ?Constraint\n" +
"WHERE\n" +
"  { ?Class rdfs:isDefinedBy ?Ontology\n" +
"    OPTIONAL\n" +
"      { ?Class spin:constraint ?Constraint }\n" +
"  }";
        
        // the client needs at least labels and constraints
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add(RDFS.Class.getLocalName(), forClass);
        Query query = new ParameterizedSparqlString(queryString, qsm).asQuery();
        try (QueryExecution qex = QueryExecutionFactory.create(query, forClass.getOntModel()))
        {
            targetModel.add(qex.execDescribe());
        }
    }
    
}