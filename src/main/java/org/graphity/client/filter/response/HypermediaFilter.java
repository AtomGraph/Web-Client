/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.client.filter.response;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.graphity.client.vocabulary.GC;
import org.graphity.client.vocabulary.GP;
import org.graphity.core.exception.ConfigurationException;
import org.graphity.core.util.Link;
import org.graphity.core.util.StateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class HypermediaFilter implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger(HypermediaFilter.class);

    @Context UriInfo uriInfo;
    
    public OntClass getMatchedOntClass(MultivaluedMap<String, Object> headerMap)
    {
        try
        {
            URI ontologyHref = getOntologyURI(headerMap);
            if (ontologyHref != null)                    
            {
                URI typeHref = getTypeURI(headerMap);
                if (typeHref != null)
                {
                    OntModelSpec ontModelSpec = getOntModelSpec(getRules(headerMap, "Rules"));
                    OntModel sitemap = getSitemap(ontologyHref.toString(), ontModelSpec);
                        return sitemap.getOntClass(typeHref.toString());
                }
            }
        }
        catch (URISyntaxException ex)   
        {
            if (log.isErrorEnabled()) log.error("'Link' response header contains invalid URI: {}", headerMap.getFirst("Link"));
        }
        
        return null;
    }
    
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (response == null) throw new IllegalArgumentException("ContainerResponse cannot be null");

        OntClass matchedOntClass = getMatchedOntClass(response.getHttpHeaders());
        if (matchedOntClass != null &&
                response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL) &&
                response.getEntity() != null && response.getEntity() instanceof Model)
        {
            Model model = (Model)response.getEntity();
            long oldCount = model.size();
            Resource resource = model.createResource(request.getAbsolutePath().toString());
            
            model = addStates(resource, matchedOntClass);
            if (log.isDebugEnabled()) log.debug("Added HATEOAS transitions to the response RDF Model for resource: {} # of statements: {}", resource.getURI(), model.size() - oldCount);
            response.setEntity(model);
            return response;
        }
        
        return response;
    }
    
    public Model addStates(Resource resource, OntClass matchedOntClass)
    {
        if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
        if (matchedOntClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        
        Model model = resource.getModel();
        NodeIterator it = matchedOntClass.listPropertyValues(GC.supportedMode);
        try
        {
            while (it.hasNext())
            {
                RDFNode supportedMode = it.next();
                if (!supportedMode.isURIResource())
                {
                    if (log.isErrorEnabled()) log.error("Invalid Mode defined for template '{}' (gc:supportedMode)", matchedOntClass.getURI());
                    throw new ConfigurationException("Invalid Mode defined for template '" + matchedOntClass.getURI() +"'");
                }

                if (!supportedMode.equals(GP.ConstructMode))
                {
                    if (resource.hasProperty(RDF.type, GP.Container) && model.contains(null, GP.pageOf, resource))
                    {
                        // container pages
                        ResIterator resIt = model.listSubjectsWithProperty(GP.pageOf, resource);
                        try
                        {
                            while (resIt.hasNext())
                            {
                                Resource page = resIt.next();
                                Long limit = null, offset = null;
                                if (getUriInfo().getQueryParameters().containsKey(GP.limit.getLocalName()))
                                    limit = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.limit.getLocalName()));
                                if (getUriInfo().getQueryParameters().containsKey(GP.offset.getLocalName()))
                                    offset = Long.parseLong(getUriInfo().getQueryParameters().getFirst(GP.offset.getLocalName()));

                                if (page.hasLiteral(GP.limit, limit) && page.hasLiteral(GP.offset, offset))
                                    StateBuilder.fromUri(page.getURI(), page.getModel()).
                                        replaceProperty(GC.mode, supportedMode.asResource()).
                                        build().
                                        addProperty(GC.layoutOf, page).
                                        addProperty(RDF.type, FOAF.Document);
                            }
                        }
                        finally
                        {
                            resIt.close();
                        }
                    }
                    else
                    {
                        // container without pagination or item
                        StateBuilder.fromUri(resource.getURI(), resource.getModel()).
                            replaceProperty(GC.mode, supportedMode.asResource()).
                            build().
                            addProperty(GC.layoutOf, resource).
                            addProperty(RDF.type, FOAF.Document);                        
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return model;
    }

    public URI getTypeURI(MultivaluedMap<String, Object> headerMap) throws URISyntaxException
    {
        return getLinkHref(headerMap, "Link", RDF.type.getLocalName());
    }

    public URI getOntologyURI(MultivaluedMap<String, Object> headerMap) throws URISyntaxException
    {
        return getLinkHref(headerMap, "Link", GP.ontology.getURI());
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
    
    public OntModel getSitemap(String ontologyURI, OntModelSpec ontModelSpec)
    {
	if (ontologyURI == null) throw new IllegalArgumentException("String cannot be null");
	if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");
        
        return OntDocumentManager.getInstance().getOntology(ontologyURI, ontModelSpec);
    }
 
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
}