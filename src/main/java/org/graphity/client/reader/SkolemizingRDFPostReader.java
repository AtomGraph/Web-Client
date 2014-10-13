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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.util.Skolemizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider that skolemizes blank nodes in a model parsed from RDF/POST.
 * Blank node URIs are built using URI templates from sitemap ontology. Nodes that do not match any resource class are left as is.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.util.Skolemizer
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class SkolemizingRDFPostReader extends RDFPostReader
{
    private static final Logger log = LoggerFactory.getLogger(SkolemizingRDFPostReader.class);

    @Context private UriInfo uriInfo;
    @Context Providers providers;
    
    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        Skolemizer skolemizer = Skolemizer.fromOntModel(getOntModel()).uriInfo(getUriInfo()).ontClass(getOntClass());
        return skolemizer.build(super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream));
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