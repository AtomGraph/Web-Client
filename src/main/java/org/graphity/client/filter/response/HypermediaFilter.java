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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.graphity.client.vocabulary.GC;
import org.graphity.client.vocabulary.GP;
import org.graphity.core.exception.ConfigurationException;
import org.graphity.core.util.Link;
import org.graphity.core.util.StateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class HypermediaFilter implements ContainerResponseFilter
{
    private static final Logger log = LoggerFactory.getLogger(HypermediaFilter.class);
   
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (response == null) throw new IllegalArgumentException("ContainerResponse cannot be null");

        if (response.getMediaType() == null ||
                !(response.getMediaType().isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE) ||
                response.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)))
            return response;
            
        Model model = getModel(response.getEntity());
        if (model == null) return response;
        Resource resource = getResource(request, model);
        if (resource == null) return response;

        try
        {
            MultivaluedMap<String, Object> headerMap = response.getHttpHeaders();
            URI ontologyHref = getOntologyURI(headerMap);
            URI typeHref = getTypeURI(headerMap);
            if (ontologyHref == null || typeHref == null) return response;

            OntModelSpec ontModelSpec = getOntModelSpec(getRules(headerMap, "Rules"));            
            OntModel ontModel = getOntModel(ontologyHref.toString(), ontModelSpec);
            OntClass template = ontModel.getOntClass(typeHref.toString());            

            if (template != null &&
                    response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL))
            {
                // transition to a URI of another application state (HATEOAS)
                Resource state = getStateBuilder(resource, request.getQueryParameters(), template).
                        build();
                if (!state.getURI().equals(request.getRequestUri().toString()))
                {
                    if (log.isDebugEnabled()) log.debug("Redirecting to a state transition URI: {}", state.getURI());
                    response.setResponse(Response.seeOther(URI.create(state.getURI())).build());
                    return response;
                }                    

                long oldCount = model.size();
                Resource doc = getStateBuilder(resource, request.getQueryParameters(), template).
                        replaceProperty(GC.mode, null). // remove mode to get back to page URI
                        build();
                model = addLayouts(doc, request.getQueryParameters(), template);
                if (log.isDebugEnabled()) log.debug("Added HATEOAS transitions to the response RDF Model for resource: {} # of statements: {}", resource.getURI(), model.size() - oldCount);
            }

            String forClassURI = request.getQueryParameters().getFirst(GC.forClass.getLocalName());
            if (forClassURI != null)
            {
                OntClass forClass = ontModel.createClass(forClassURI);

                if (response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                    addInstance(model, forClass);
            }
            
            response.setEntity(model);
        }
        catch (URISyntaxException ex)
        {
            return response;
        }

        return response;
    }
    
    public Model getModel(Object entity)
    {
        if (entity instanceof Model) return (Model)entity;
        
        return null;
    }
    
    public StateBuilder getStateBuilder(Resource resource, MultivaluedMap<String, String> queryParams, OntClass template)
    {
        return StateBuilder.fromUri(resource.getURI().toString(), resource.getModel());
    }
    
    public Model addLayouts(Resource resource, MultivaluedMap<String, String> params, OntClass template)
    {
        if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
        if (params == null) throw new IllegalArgumentException("MultivaluedMap cannot be null");
        if (template == null) throw new IllegalArgumentException("OntClass cannot be null");
        
        Model model = resource.getModel();
        NodeIterator it = template.listPropertyValues(GC.supportedMode);
        try
        {
            while (it.hasNext())
            {
                RDFNode supportedMode = it.next();
                if (!supportedMode.isURIResource())
                {
                    if (log.isErrorEnabled()) log.error("Invalid Mode defined for template '{}' (gc:supportedMode)", template.getURI());
                    throw new ConfigurationException("Invalid Mode defined for template '" + template.getURI() +"'");
                }

                if (model.contains(null, GP.pageOf, resource))
                {
                    if (supportedMode.asResource().hasProperty(RDF.type, GC.PageMode))                    
                    {
                        // container pages
                        ResIterator resIt = model.listSubjectsWithProperty(GP.pageOf, resource);
                        try
                        {
                            while (resIt.hasNext())
                            {
                                Resource page = resIt.next();
                                StateBuilder.fromUri(page.getURI(), page.getModel()).
                                    replaceProperty(GC.mode, supportedMode.asResource()).
                                    build().
                                    addProperty(GC.layoutOf, page);
                            }
                        }
                        finally
                        {
                            resIt.close();
                        }
                    }
                }
                else
                {
                    StateBuilder.fromUri(resource.getURI(), resource.getModel()).
                        replaceProperty(GC.mode, supportedMode.asResource()).
                        build().
                        addProperty(GC.layoutOf, resource);
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
    
    public OntModel getOntModel(String ontologyURI, OntModelSpec ontModelSpec)
    {
	if (ontologyURI == null) throw new IllegalArgumentException("String cannot be null");
	if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");
        
        return OntDocumentManager.getInstance().getOntology(ontologyURI, ontModelSpec);
    }
  
    public Resource getResource(ContainerRequest request, Model model)
    {
	if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        
        return model.createResource(request.getQueryParameters().getFirst(GC.uri.getLocalName()));
    }

    public Model addInstance(Model model, OntClass forClass)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");

        Property property = SPIN.constructor;
        if (log.isDebugEnabled()) log.debug("Invoking constructor on class {} using property {}", forClass, property);
        new ConstructorBase().construct(forClass, property, model);
        
        return model;
    }
    
}