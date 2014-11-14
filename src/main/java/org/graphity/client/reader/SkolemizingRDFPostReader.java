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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.graphity.processor.provider.OntClassMatcher;
import org.graphity.processor.util.Skolemizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS reader that skolemizes blank nodes in a model parsed from RDF/POST request.
 * Blank node URIs are built using URI templates from sitemap ontology. Nodes that do not match any resource class are left as is.
 * Note that the instances have already been validated by the superclass, i.e. invalid instances will not get to this point.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.util.Skolemizer
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class SkolemizingRDFPostReader extends ValidatingRDFPostReader
{
    private static final Logger log = LoggerFactory.getLogger(SkolemizingRDFPostReader.class);

    @Context private Request request;
    @Context private UriInfo uriInfo;
    
    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        Model model = super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
        
        if (getRequest().getMethod().equalsIgnoreCase("POST"))
        {
            try
            {
                return skolemize(getUriInfo(), getOntModel(), getOntClass(), new OntClassMatcher(), model);
            }
            catch (IllegalArgumentException ex)
            {
                if (log.isErrorEnabled()) log.error("Blank node skolemization failed for model: {}", model);
                throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
            }
        }
        
        return model;
    }

    public Model skolemize(UriInfo uriInfo, OntModel ontModel, OntClass ontClass, OntClassMatcher ontClassMatcher, Model model)
    {
        return Skolemizer.fromOntModel(ontModel).
                uriInfo(uriInfo).
                ontClass(ontClass).
                ontClassMatcher(ontClassMatcher).
                build(model);
    }
    
    public OntClass getOntClass()
    {
	ContextResolver<OntClass> cr = getProviders().getContextResolver(OntClass.class, null);
	return cr.getContext(OntClass.class);
    }
    
    @Override
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Request getRequest()
    {
        return request;
    }
    
}