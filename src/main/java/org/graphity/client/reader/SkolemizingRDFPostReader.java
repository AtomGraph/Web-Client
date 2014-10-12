/*
 * Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.client.reader;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.uri.UriTemplateParser;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.provider.MatchedOntClassProvider;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SIOC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class SkolemizingRDFPostReader extends RDFPostReader
{
    private static final Logger log = LoggerFactory.getLogger(SkolemizingRDFPostReader.class);

    @Context private UriInfo uriInfo;
    @Context Providers providers;
    
    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        return skolemize(super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream));
    }
    
    public Model skolemize(Model model)
    {
    	if (model == null) throw new IllegalArgumentException("Model cannot be null");

	Map<Resource, String> resourceURIMap = new HashMap<>();
	ResIterator resIt = model.listSubjects();
	try
	{
	    while (resIt.hasNext())
	    {
		Resource resource = resIt.next();
                if (resource.isAnon())
                {
                    URI uri = skolemize(resource);
                    if (uri != null) resourceURIMap.put(resource, uri.toString());
                }
	    }
	}
	finally
	{
	    resIt.close();
	}
	
	Iterator<Map.Entry<Resource, String>> entryIt = resourceURIMap.entrySet().iterator();
	while (entryIt.hasNext())
	{
	    Map.Entry<Resource, String> entry = entryIt.next();
	    ResourceUtils.renameResource(entry.getKey(), entry.getValue());
	}

	return model;
    }

    public URI skolemize(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

        OntClass ontClass = matchOntClass(resource);
        if (ontClass != null)
        {
            if (log.isDebugEnabled()) log.debug("Skolemizing resource {} using ontology class {}", resource, ontClass);
            return skolemize(resource, ontClass);
        }        
        
        // as a fallback (for real-world resources), try to skolemize using the document class
        if (resource.hasProperty(FOAF.isPrimaryTopicOf))
        {
            Resource doc = resource.getPropertyResourceValue(FOAF.isPrimaryTopicOf);
            if (doc != null)
            {
                OntClass docClass = matchOntClass(doc);
                if (docClass != null)
                {
                    Resource allValuesFrom = getRestrictionAllValuesFrom(docClass, FOAF.primaryTopic);
                    if (allValuesFrom != null && allValuesFrom.canAs(OntClass.class))
                    {
                        if (log.isDebugEnabled()) log.debug("Skolemizing resource {} using ontology class of its document {}", resource, ontClass);
                        return skolemize(resource, allValuesFrom.as(OntClass.class));
                    }
                }
            }
        }
        
        return null;
    }

    public URI skolemize(Resource resource, OntClass ontClass)
    {
        return skolemize(resource, getItemTemplate(ontClass, GP.uriTemplate)); // ontClass has URI template at this point
    }
    
    public URI skolemize(Resource resource, String itemTemplate)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	if (itemTemplate == null) throw new IllegalArgumentException("URI template cannot be null");
        if (log.isDebugEnabled()) log.debug("Building URI for resource {} with template: {}", resource, itemTemplate);
        UriBuilder builder = getUriInfo().getBaseUriBuilder().path(itemTemplate);
        // add fragment identifier for non-information resources
        if (!resource.hasProperty(RDF.type, FOAF.Document)) builder.fragment("this");

        try
        {
            return skolemize(resource, new UriTemplateParser(itemTemplate), builder);
        }
        catch (IllegalArgumentException ex)
        {
            if (log.isDebugEnabled()) log.debug("Building URI from resource {} failed", resource);
            throw new IllegalArgumentException("POSTed Resources '" + resource + "' is missing properties required by its URI template '" + itemTemplate + "'");
            // map to WebApplicationException
        }
    }
    
    public URI skolemize(Resource resource, UriTemplateParser parser, UriBuilder builder)
    {
	if (parser == null) throw new IllegalArgumentException("UriTemplateParser cannot be null");
	if (builder == null) throw new IllegalArgumentException("UriBuilder cannot be null");

	Map<String, String> nameValueMap = new HashMap<>();
	List<String> names = parser.getNames();
	for (String name : names)
	{
	    Literal literal = getLiteral(resource, name);
	    if (literal != null)
	    {
		if (log.isDebugEnabled()) log.debug("UriTemplate variable name: {} has value: {}", name, literal.toString());
		nameValueMap.put(name, literal.getString());
	    }
	}

	return builder.buildFromMap(nameValueMap);
    }

    public Literal getLiteral(Resource resource, String namePath)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

	if (namePath.contains("."))
	{
	    String name = namePath.substring(0, namePath.indexOf("."));
	    String nameSubPath = namePath.substring(namePath.indexOf(".") + 1);
	    Resource subResource = getResource(resource, name);
	    if (subResource != null) return getLiteral(subResource, nameSubPath);
	}
	
	StmtIterator it = resource.listProperties();
	try
	{
	    while (it.hasNext())
	    {
		Statement stmt = it.next();
		if (stmt.getObject().isLiteral() && stmt.getPredicate().getLocalName().equals(namePath))
		{
		    if (log.isTraceEnabled()) log.trace("Found Literal {} for property name: {} ", stmt.getLiteral(), namePath);
		    return stmt.getLiteral();
		}
	    }
	}
	finally
	{
	    it.close();
	}
	
	return null;
    }

    public Resource getResource(Resource resource, String name)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	StmtIterator it = resource.listProperties();
	try
	{
	    while (it.hasNext())
	    {
		Statement stmt = it.next();
		if (stmt.getObject().isResource() && stmt.getPredicate().getLocalName().equals(name))
		{
		    if (log.isTraceEnabled()) log.trace("Found Resource {} for property name: {} ", stmt.getResource(), name);
		    return stmt.getResource();
		}
	    }
	}
	finally
	{
	    it.close();
	}
	
	return null;
    }

    public String getItemTemplate(OntClass ontClass, Property property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");

        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getString();
        
        return null;
    }

    public Resource getRestrictionAllValuesFrom(OntClass ontClass, Property property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("OntProperty cannot be null");
	
	ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
	
	try
	{
	    while (it.hasNext())
	    {
		OntClass superClass = it.next();
		if (superClass.canAs(AllValuesFromRestriction.class))
		{
		    AllValuesFromRestriction restriction = superClass.asRestriction().asAllValuesFromRestriction();
		    if (restriction.getOnProperty().equals(property))
			return restriction.getAllValuesFrom();
		}
	    }
	    
	    return null;
	}
	finally
	{
	    it.close();
	}
    }

    public OntClass matchOntClass(Resource resource)
    {
        MatchedOntClassProvider ontClassProvider = new MatchedOntClassProvider();
        
        if (resource.hasProperty(SIOC.HAS_CONTAINER))
        {
            Resource container = resource.getPropertyResourceValue(SIOC.HAS_CONTAINER);
            if (log.isDebugEnabled()) log.debug("Resource {} will be stored as a child of specified container {}", resource, container);
            URI containerURI = URI.create(container.getURI());

            OntClass containerClass = ontClassProvider.matchOntClass(getOntModel(), containerURI, getUriInfo().getBaseUri());
            return matchOntClass(SIOC.HAS_CONTAINER, containerClass);
        }
        if (resource.hasProperty(SIOC.HAS_SPACE))
        {
            Resource space = resource.getPropertyResourceValue(SIOC.HAS_SPACE);
            if (log.isDebugEnabled()) log.debug("Container {} will be stored as a child of specified space {}", resource, space);
            URI containerURI = URI.create(space.getURI());
            OntClass spaceClass = ontClassProvider.matchOntClass(getOntModel(), containerURI, getUriInfo().getBaseUri());
            return matchOntClass(SIOC.HAS_SPACE, spaceClass);
        }

        if (resource.hasProperty(RDF.type, SIOC.CONTAINER))
        {
            if (log.isDebugEnabled()) log.debug("Container {} will be stored as a child of requested container {}", resource, this);
            return matchOntClass(SIOC.HAS_SPACE, getOntClass());
        }
        if (resource.hasProperty(RDF.type, FOAF.Document))
        {
            if (log.isDebugEnabled()) log.debug("Document {} will be stored as a child of requested container {}", resource, this);
            return matchOntClass(SIOC.HAS_CONTAINER, getOntClass());
        }

        return null;
    }

    public OntClass matchOntClass(Property property, OntClass containerClass)
    {
	if (containerClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");
        
        ExtendedIterator<Restriction> it = getOntModel().listRestrictions();        
        try
        {
            while (it.hasNext())
            {
                Restriction restriction = it.next();	    
                if (restriction.canAs(AllValuesFromRestriction.class))
                {
                    AllValuesFromRestriction avfr = restriction.asAllValuesFromRestriction();
                    if (avfr.getOnProperty().equals(property) && avfr.hasAllValuesFrom(containerClass))
                    {
                        OntClass ontClass = avfr.listSubClasses(true).next();
                        if (log.isDebugEnabled()) log.debug("Value {} matched endpoint OntClass {}", containerClass, ontClass);
                        return ontClass;
                    }
                }
            }

            if (log.isWarnEnabled()) log.warn("Container {} has no OntClass match in this OntModel", containerClass);
            return null;
        }
        finally
        {
            it.close();
        }	
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
    
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
        return providers;
    }

}