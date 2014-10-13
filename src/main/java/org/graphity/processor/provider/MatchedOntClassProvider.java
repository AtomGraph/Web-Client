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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
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
public class MatchedOntClassProvider extends PerRequestTypeInjectableProvider<Context, OntClass> implements ContextResolver<OntClass>
{
    private static final Logger log = LoggerFactory.getLogger(MatchedOntClassProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;
    
    public MatchedOntClassProvider()
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
     * By default, <code>lda:uriTemplate</code> property (from Linked Data API) is used for the <code>owl:HasValue</code>
     * restrictions, with URI template string as the object literal.
     * 
     * @param ontModel sitemap ontology model
     * @param path absolute path (relative URI)
     * @return matching ontology class or null, if none
     * @see <a href="https://code.google.com/p/linked-data-api/wiki/API_Vocabulary">Linked Data API Vocabulary</a>
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
                            if (log.isDebugEnabled()) log.debug("Path {} did not match UriTemplate {}", path, uriTemplate);
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