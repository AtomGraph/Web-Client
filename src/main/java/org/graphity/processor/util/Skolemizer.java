/*
 * Copyright 2014 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.processor.util;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.uri.UriTemplateParser;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.processor.provider.MatchedOntClassProvider;
import org.graphity.processor.vocabulary.GP;
import org.graphity.processor.vocabulary.SIOC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class that can build URIs from templates for RDF resources as well as models.
 * Needs to be initialized with sitemap ontology, ontology class matching request URI, and request URI information.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Skolemizer
{
    private static final Logger log = LoggerFactory.getLogger(Skolemizer.class);

    private UriInfo uriInfo;
    private OntClass ontClass;
    private OntModel ontModel;
    
    protected Skolemizer()
    {
    }
    
    protected static Skolemizer newInstance()
    {
	return new Skolemizer();
    }

    public Skolemizer uriInfo(UriInfo uriInfo)
    {
	if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        this.uriInfo = uriInfo;
        return this;
    }

    public Skolemizer ontClass(OntClass ontClass)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        this.ontClass = ontClass;
        return this;
    }

    public Skolemizer ontModel(OntModel ontModel)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
        this.ontModel = ontModel;
        return this;
    }

    public static Skolemizer fromOntModel(OntModel ontModel)
    {
        return newInstance().ontModel(ontModel);
    }
    
    public Model build(Model model)
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
                    URI uri = build(resource);
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

    public Resource getBaseResource(Resource resource)
    {
        if (resource.hasProperty(SIOC.HAS_CONTAINER)) return resource.getPropertyResourceValue(SIOC.HAS_CONTAINER);
        if (resource.hasProperty(SIOC.HAS_SPACE)) return resource.getPropertyResourceValue(SIOC.HAS_SPACE);
        
        return ResourceFactory.createResource(getUriInfo().getAbsolutePath().toString());
    }
    
    public URI build(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");

        OntClass matchingClass = matchOntClass(resource);
        if (matchingClass != null)
        {
            if (log.isDebugEnabled()) log.debug("Skolemizing resource {} using ontology class {}", resource, matchingClass);
            return build(resource, UriBuilder.fromUri(getBaseResource(resource).getURI()), matchingClass);
        }        
        
        // as a fallback (for real-world resources), try to skolemize using the document class
        if (resource.hasProperty(FOAF.isPrimaryTopicOf))
        {
            Resource doc = null;
            
            StmtIterator it = resource.listProperties(FOAF.isPrimaryTopicOf);
            try
            {
                // document resource has to be a blank node as well
                while (it.hasNext() && doc == null)
                {
                    Statement stmt = it.next();
                    if (stmt.getObject().isAnon()) doc = stmt.getObject().asResource();
                }
            }
            finally
            {
                it.close();
            }

            if (doc != null)
            {
                OntClass docClass = matchOntClass(doc);
                if (docClass != null)
                {
                    OntClass topicClass = matchOntClass(FOAF.isPrimaryTopicOf, docClass);
                    return build(resource, UriBuilder.fromUri(getBaseResource(doc).getURI()), topicClass);
                }
            }
        }
        
        return null;
    }

    public URI build(Resource resource, UriBuilder baseBuilder, OntClass ontClass)
    {
        // build URI relative to absolute path
        return build(resource, baseBuilder, getSkolemTemplate(ontClass, GP.skolemTemplate));
    }
    
    public URI build(Resource resource, UriBuilder baseBuilder, String itemTemplate)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	if (baseBuilder == null) throw new IllegalArgumentException("UriBuilder cannot be null");
        if (itemTemplate == null) throw new IllegalArgumentException("URI template cannot be null");
        
        if (log.isDebugEnabled()) log.debug("Building URI for resource {} with template: {}", resource, itemTemplate);
        UriBuilder builder = baseBuilder.path(itemTemplate);
        // add fragment identifier for non-information resources
        if (!resource.hasProperty(RDF.type, FOAF.Document)) builder.fragment("this");

        try
        {
            return build(resource, new UriTemplateParser(itemTemplate), builder);
        }
        catch (IllegalArgumentException ex)
        {
            if (log.isDebugEnabled()) log.debug("Building URI from resource {} failed", resource);
            throw new IllegalArgumentException("POSTed Resources '" + resource + "' is missing properties required by its URI template '" + itemTemplate + "'");
            // map to WebApplicationException
        }
    }
    
    protected URI build(Resource resource, UriTemplateParser parser, UriBuilder builder)
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

    protected Literal getLiteral(Resource resource, String namePath)
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

    protected Resource getResource(Resource resource, String name)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	StmtIterator it = resource.listProperties();
	try
	{
	    while (it.hasNext())
	    {
		Statement stmt = it.next();
		if (stmt.getObject().isAnon() && stmt.getPredicate().getLocalName().equals(name))
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

    protected String getSkolemTemplate(OntClass ontClass, Property property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");

        if (ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
            return ontClass.getPropertyValue(property).asLiteral().getString();
        
        return null;
    }
    
    protected OntClass matchOntClass(Resource resource)
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
            if (log.isDebugEnabled()) log.debug("Container {} will be stored as a child of requested container {}", resource, getUriInfo().getAbsolutePath());
            return matchOntClass(SIOC.HAS_SPACE, getOntClass());
        }
        if (resource.hasProperty(RDF.type, FOAF.Document))
        {
            if (log.isDebugEnabled()) log.debug("Document {} will be stored as a child of requested container {}", resource, getUriInfo().getAbsolutePath());
            return matchOntClass(SIOC.HAS_CONTAINER, getOntClass());
        }

        return null;
    }

    protected OntClass matchOntClass(Property property, OntClass ontClass)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
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
                    if (avfr.getOnProperty().equals(property) && avfr.hasAllValuesFrom(ontClass))
                    {
                        ExtendedIterator<OntClass> classIt = avfr.listSubClasses(true);
                        try
                        {
                            if (classIt.hasNext())
                            {
                                OntClass matchingClass = classIt.next();
                                if (log.isDebugEnabled()) log.debug("Value {} matched endpoint OntClass {}", ontClass, matchingClass);
                                return matchingClass;
                            }
                        }
                        finally
                        {
                            classIt.close();
                        }
                    }
                }
            }

            if (log.isWarnEnabled()) log.warn("Container {} has no OntClass match in this OntModel", ontClass);
            return null;
        }
        finally
        {
            it.close();
        }	
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
    public OntClass getOntClass()
    {
        return ontClass;
    }
    
    public OntModel getOntModel()
    {
        return ontModel;
    }

}
