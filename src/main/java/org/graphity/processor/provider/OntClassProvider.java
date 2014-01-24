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

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.TreeMap;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.vocabulary.LDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class OntClassProvider extends PerRequestTypeInjectableProvider<Context, OntClass> implements ContextResolver<OntClass>
{
    private static final Logger log = LoggerFactory.getLogger(OntClassProvider.class);

    @Context UriInfo uriInfo;
    @Context Providers providers;

    public OntClassProvider()
    {
        super(OntClass.class);
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Providers getProviders()
    {
	return providers;
    }

    public OntModel getOntModel()
    {
	ContextResolver<OntModel> cr = getProviders().getContextResolver(OntModel.class, null);
	return cr.getContext(OntModel.class);
    }

    @Override
    public Injectable<OntClass> getInjectable(ComponentContext cc, Context a)
    {
	return new Injectable<OntClass>()
	{
	    @Override
	    public OntClass getValue()
	    {
		return getOntClass(getUriInfo().getAbsolutePath(), getUriInfo().getBaseUri());
	    }

	};
    }

    @Override
    public OntClass getContext(Class<?> type)
    {
        return getOntClass(getUriInfo().getAbsolutePath(), getUriInfo().getBaseUri());
    }
    
    /**
     * Given an absolute URI and a base URI, returns ontology class with a matching URI template, if any.
     * 
     * @param uri absolute URI being matched
     * @param base base URI
     * @return matching ontology class or null, if none
     */
    public OntClass getOntClass(URI uri, URI base)
    {
	if (uri == null) throw new IllegalArgumentException("URI being matched cannot be null");
	if (base == null) throw new IllegalArgumentException("Base URI cannot be null");
	if (!uri.isAbsolute()) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not absolute");
	if (base.relativize(uri).equals(uri)) throw new IllegalArgumentException("URI being matched \"" + uri + "\" is not relative to the base URI \"" + base + "\"");
	    
	StringBuilder path = new StringBuilder();
	// instead of path, include query string by relativizing request URI against base URI
	path.append("/").append(base.relativize(uri));
	return matchOntClass(path);
    }

    /**
     * Given a relative URI, returns ontology class with a matching URI template, if any.
     * By default, <code>lda:uriTemplate</code> property (from Linked Data API) is used for the <code>owl:HasValue</code>
     * restrictions, with URI template string as the object literal.
     * 
     * @param path absolute path (relative URI)
     * @return matching ontology class or null, if none
     * @see <a href="https://code.google.com/p/linked-data-api/wiki/API_Vocabulary">Linked Data API Vocabulary</a>
     */
    public OntClass matchOntClass(CharSequence path)
    {
        return matchOntClass(path, LDA.uriTemplate);
    }
    
    /**
     * Given a relative URI and URI template property, returns ontology class with a matching URI template, if any.
     * URIs are matched against the URI templates specified in ontology class <code>owl:hasValue</code> restrictions
     * on the given property in the sitemap ontology.
     * This method uses Jersey implementation of the JAX-RS URI matching algorithm.
     * 
     * @param path absolute path (relative URI)
     * @param property restriction property holding the URI template value
     * @return matching ontology class or null, if none
     * @see <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec3.html#x3-340003.7">3.7 Matching Requests to Resource Methods (JAX-RS 1.1)</a>
     * @see <a href="https://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/api/uri/UriTemplate.html">Jersey UriTemplate</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public OntClass matchOntClass(CharSequence path, Property property)
    {
	if (path == null) throw new IllegalArgumentException("Path being matched cannot be null");
	ExtendedIterator<Restriction> it = getOntModel().listRestrictions();

	try
	{
	    TreeMap<UriTemplate, OntClass> matchedClasses = new TreeMap<>(UriTemplate.COMPARATOR);

	    while (it.hasNext())
	    {
		Restriction restriction = it.next();	    
		if (restriction.canAs(HasValueRestriction.class))
		{
		    HasValueRestriction hvr = restriction.asHasValueRestriction();
		    if (hvr.getOnProperty().equals(property))
		    {
			UriTemplate uriTemplate = new UriTemplate(hvr.getHasValue().toString());
			HashMap<String, String> map = new HashMap<>();

			if (uriTemplate.match(path, map))
			{
			    if (log.isDebugEnabled()) log.debug("Path {} matched UriTemplate {}", path, uriTemplate);

			    OntClass ontClass = hvr.listSubClasses(true).next(); //hvr.getSubClass();	    
			    if (log.isDebugEnabled()) log.debug("Path {} matched endpoint OntClass {}", path, ontClass);
			    matchedClasses.put(uriTemplate, ontClass);
			}
			else
			    if (log.isDebugEnabled()) log.debug("Path {} did not match UriTemplate {}", path, uriTemplate);
		    }
		}
	    }
	    
	    if (!matchedClasses.isEmpty())
	    {
		if (log.isDebugEnabled()) log.debug("Matched UriTemplate: {} OntClass: {}", matchedClasses.firstKey(), matchedClasses.firstEntry().getValue());
		return matchedClasses.firstEntry().getValue();
	    }

	    if (log.isDebugEnabled()) log.debug("Path {} has no OntClass match in this OntModel", path);
	    return null;
	}
	finally
	{
	    it.close();
	}	
    }

}
