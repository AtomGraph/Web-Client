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

package org.graphity.processor.provider;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.vocabulary.GP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for resource template class in the sitemap ontology that matches the current request.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class OntClassMatcher extends PerRequestTypeInjectableProvider<Context, OntClass> implements ContextResolver<OntClass>
{
    private static final Logger log = LoggerFactory.getLogger(OntClassMatcher.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;
    
    public OntClassMatcher()
    {
	super(OntClass.class);
    }

    @Override
    public Injectable<OntClass> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<OntClass>()
	{
	    @Override
	    public OntClass getValue()
	    {
                return getOntClass();
	    }
	};
    }

    @Override
    public OntClass getContext(Class<?> type)
    {
        return getOntClass();
    }

    public OntClass getOntClass()
    {
        return matchOntClass(getOntModel(), getUriInfo().getAbsolutePath(), getUriInfo().getBaseUri());
    }
    
    /**
     * Given an absolute URI and a base URI, returns ontology class with a matching URI template, if any.
     * 
     * @param ontModel sitemap ontology model
     * @param uri absolute URI being matched
     * @param base base URI
     * @return matching ontology class or null, if none
     */
    public OntClass matchOntClass(OntModel ontModel, URI uri, URI base)
    {
	if (uri == null) throw new IllegalArgumentException("URI being matched cannot be null");
	if (base == null) throw new IllegalArgumentException("Base URI cannot be null");
	if (!uri.isAbsolute()) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not absolute");
	if (base.relativize(uri).equals(uri)) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not relative to the base URI \"" + base + "\"");
	    
	StringBuilder path = new StringBuilder();
	// instead of path, include query string by relativizing request URI against base URI
	path.append("/").append(base.relativize(uri));
	return matchOntClass(ontModel, path);
    }

    /**
     * Given a relative URI, returns ontology class with a matching URI template, if any.
     * <code>gp:uriTemplate</code> property is used with URI template string as the object literal.
     * 
     * @param ontModel sitemap ontology model
     * @param path absolute path (relative URI)
     * @return matching ontology class or null, if none
     */
    public OntClass matchOntClass(OntModel ontModel, CharSequence path)
    {
        return matchOntClass(ontModel, path, GP.uriTemplate);
    }

    /**
     * Given a relative URI and URI template property, returns ontology class with a matching URI template, if any.
     * URIs are matched against the URI templates specified in resource templates (sitemap ontology classes).
     * Templates in the base ontology model have priority (are matched first) against templates in imported ontologies.
     * 
     * @param ontModel sitemap ontology model
     * @param path absolute path (relative URI)
     * @param property restriction property holding the URI template value
     * @return matching ontology class or null, if none
     * @see <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec3.html#x3-340003.7">3.7 Matching Requests to Resource Methods (JAX-RS 1.1)</a>
     * @see <a href="https://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/uri/UriTemplate.html">Jersey UriTemplate</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public OntClass matchOntClass(OntModel ontModel, CharSequence path, Property property)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
        
        TreeMap<UriTemplate, OntClass> matchedClasses = new TreeMap<>(UriTemplate.COMPARATOR);

        // the main sitemap has precedence
        matchedClasses.putAll(matchOntClasses(ontModel, path, property, true));
        if (!matchedClasses.isEmpty())
        {
            if (log.isDebugEnabled()) log.debug("Matched UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
            return matchedClasses.firstEntry().getValue();
        }

        // gp:Templates from imported ontologies have lower precedence
        matchedClasses.putAll(matchOntClasses(ontModel, path, property, false));
        if (!matchedClasses.isEmpty())
        {
            if (log.isDebugEnabled()) log.debug("Matched imported UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
            return matchedClasses.firstEntry().getValue();
        }
        
        if (log.isDebugEnabled()) log.debug("Path {} has no OntClass match in this OntModel", path);
        return null;
    }

    /**
     * Matches path (relative URI) against URI templates in sitemap ontology.
     * This method uses Jersey implementation of the JAX-RS URI matching algorithm.
     * 
     * @param ontModel sitemap ontology model
     * @param path URI path
     * @param property property attaching URI templates to ontology class
     * @param inBaseModel whether to only use base model statements
     * @return URI template/ontology class map
     */
    public Map<UriTemplate, OntClass> matchOntClasses(OntModel ontModel, CharSequence path, Property property, boolean inBaseModel)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
        if (path == null) throw new IllegalArgumentException("Path being matched cannot be null");
 	if (property == null) throw new IllegalArgumentException("URI template property cannot be null");

        Map<UriTemplate, OntClass> matchedClasses = new HashMap<>();
        StmtIterator it = ontModel.listStatements(null, property, (RDFNode)null);

        try
	{
	    while (it.hasNext())
	    {
                Statement stmt = it.next();
                if (((ontModel.isInBaseModel(stmt) && inBaseModel) || (!ontModel.isInBaseModel(stmt) && !inBaseModel)) &&
                        stmt.getSubject().canAs(OntClass.class))
                {
                    OntClass ontClass = stmt.getSubject().as(OntClass.class);
                    if (ontClass.hasSuperClass(FOAF.Document) && 
                            ontClass.hasProperty(property) && ontClass.getPropertyValue(property).isLiteral())
                    {
                        UriTemplate uriTemplate = new UriTemplate(ontClass.getPropertyValue(property).asLiteral().getString());
                        HashMap<String, String> map = new HashMap<>();

                        if (uriTemplate.match(path, map))
                        {
                            if (log.isDebugEnabled()) log.debug("Path {} matched UriTemplate {}", path, uriTemplate);
                            if (log.isDebugEnabled()) log.debug("Path {} matched OntClass {}", path, ontClass);
                            matchedClasses.put(uriTemplate, ontClass);
                        }
                        else
                            if (log.isTraceEnabled()) log.trace("Path {} did not match UriTemplate {}", path, uriTemplate);
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return matchedClasses;
    }

    public OntClass matchOntClass(Resource resource, OntClass parentClass)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
        if (parentClass == null) throw new IllegalArgumentException("OntClass cannot be null");

        StmtIterator it = resource.listProperties(RDF.type);
        try
        {
            while (it.hasNext())
            {
                Statement stmt = it.next();
                if (stmt.getObject().isURIResource())
                {
                    OntClass typeClass = parentClass.getOntModel().getOntClass(stmt.getObject().asResource().getURI());
                    // return resource type which is defined by the sitemap ontology
                    if (typeClass != null && typeClass.getIsDefinedBy() != null &&
                            typeClass.getIsDefinedBy().equals(parentClass.getIsDefinedBy()))
                        return typeClass;
                }
            }
        }
        finally
        {
            it.close();
        }

        return null;
    }

    public Map<Property, OntClass> matchOntClasses(OntModel ontModel, Property property, OntClass ontClass)
    {
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");
        if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        
        Map<Property, OntClass> matchedClasses = new HashMap<>();        
        ExtendedIterator<Restriction> it = ontModel.listRestrictions();        
        try
        {
            while (it.hasNext())
            {
                Restriction restriction = it.next();	    
                if (restriction.canAs(AllValuesFromRestriction.class))
                {
                    AllValuesFromRestriction avfr = restriction.asAllValuesFromRestriction();
                    if (avfr.getOnProperty().equals(property) &&
                            (avfr.hasAllValuesFrom(ontClass) ||
                                (avfr.getAllValuesFrom().canAs(UnionClass.class) && avfr.getAllValuesFrom().as(UnionClass.class).listOperands().toList().contains(ontClass))))
                    {
                        ExtendedIterator<OntClass> classIt = avfr.listSubClasses(true);
                        try
                        {
                            if (classIt.hasNext())
                            {
                                OntClass matchingClass = classIt.next();
                                if (log.isDebugEnabled()) log.debug("Value {} matched endpoint OntClass {}", ontClass, matchingClass);
                                //return matchingClass;
                                matchedClasses.put(property, matchingClass);
                            }
                        }
                        finally
                        {
                            classIt.close();
                        }    
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return matchedClasses;
    }

    public OntModel getOntModel()
    {
	ContextResolver<OntModel> cr = getProviders().getContextResolver(OntModel.class, null);
	return cr.getContext(OntModel.class);
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