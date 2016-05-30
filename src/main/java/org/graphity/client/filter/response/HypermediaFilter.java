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

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
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
        Resource state = getResource(request, model);
        if (state == null) return response;

        try
        {
            MultivaluedMap<String, Object> headerMap = response.getHttpHeaders();
            URI ontologyHref = getOntologyURI(headerMap);
            URI typeHref = getTypeURI(headerMap);
            if (ontologyHref == null || typeHref == null) return response;

            OntModelSpec ontModelSpec = getOntModelSpec(getRules(headerMap, "Rules"));            
            OntModel ontModel = getOntModel(ontologyHref.toString(), ontModelSpec);
            OntClass template = ontModel.getOntClass(typeHref.toString());            

            if (template != null)
            {
                if (response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL))
                {

                    // transition to a URI of another application state (HATEOAS)
                    Resource defaultState = getStateBuilder(state, getMode(request, template), getForClass(request)).
                            build();
                    if (!defaultState.equals(state))
                    {
                        if (log.isDebugEnabled()) log.debug("Redirecting to a state transition URI: {}", defaultState.getURI());
                        response.setResponse(Response.seeOther(URI.create(defaultState.getURI())).build());
                        return response;
                    }
                }
                
                long oldCount = model.size();
                addLayouts(getStateBuilder(state, null, getForClass(request)).build(), template);
                if (log.isDebugEnabled()) log.debug("Added HATEOAS transitions to the response RDF Model for resource: {} # of statements: {}", state.getURI(), model.size() - oldCount);
            }

            //String forClassURI = request.getQueryParameters().getFirst(GC.forClass.getLocalName());
            if (getForClass(request) != null && response.getStatusType().getFamily().equals(Response.Status.Family.SUCCESSFUL))
            {
                OntClass forClass = ontModel.createClass(getForClass(request).getURI());
                addInstance(model, forClass);
                
                state.addProperty(GC.constructorOf, getStateBuilder(state, getMode(request, template), null).
                    build());
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
    
    public StateBuilder getStateBuilder(Resource resource, Resource mode, Resource forClass)
    {
        if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

        return StateBuilder.fromUri(resource.getURI(), resource.getModel()).
            replaceProperty(GC.mode, mode).
            replaceProperty(GC.forClass, forClass);
    }
    
    public final Resource getResource(OntClass ontClass, AnnotationProperty property)
    {
        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isResource())
            return ontClass.getPropertyValue(property).asResource();
        
        return null;
    }
    
    public void addLayouts(Resource state, OntClass template)
    {
        if (state == null) throw new IllegalArgumentException("Resource cannot be null");
        if (template == null) throw new IllegalArgumentException("OntClass cannot be null");
        
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

                getStateBuilder(state, supportedMode.asResource(), null).
                    build().
                    addProperty(GC.layoutOf, state);
            }
        }
        finally
        {
            it.close();
        }
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
        
        // cannot use request URI directly because for some reason browsers send decoded query param values
        //return model.createResource(request.getRequestUri().toString());
        StateBuilder sb = StateBuilder.fromUri(request.getRequestUri(), model).
            replaceProperty(GC.uri, model.createResource(request.getQueryParameters().getFirst(GC.uri.getLocalName())));
        if (request.getQueryParameters().containsKey(GC.mode.getLocalName()))
            sb.replaceProperty(GC.mode, model.createResource(request.getQueryParameters().getFirst(GC.mode.getLocalName())));
        if (request.getQueryParameters().containsKey(GC.forClass.getLocalName()))
            sb.replaceProperty(GC.forClass, model.createResource(request.getQueryParameters().getFirst(GC.forClass.getLocalName())));

        return sb.build();
    }

    public Resource getMode(ContainerRequest request, OntClass template)
    {
	if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");
        if (template == null) throw new IllegalArgumentException("OntClass cannot be null");

        final Resource mode;
        if (request.getQueryParameters().containsKey(GC.mode.getLocalName()))
            mode = ResourceFactory.createResource(request.getQueryParameters().getFirst(GC.mode.getLocalName()));
        else mode = getResource(template, GC.defaultMode);
        
        return mode;
    }

    public Resource getForClass(ContainerRequest request)
    {
	if (request == null) throw new IllegalArgumentException("ContainerRequest cannot be null");

        if (request.getQueryParameters().containsKey(GC.forClass.getLocalName()))
            return ResourceFactory.createResource(request.getQueryParameters().getFirst(GC.forClass.getLocalName()));
        
        return null;
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