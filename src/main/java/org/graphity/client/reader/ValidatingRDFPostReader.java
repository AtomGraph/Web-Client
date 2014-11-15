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

package org.graphity.client.reader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.processor.exception.ConstraintViolationException;
import org.graphity.client.vocabulary.GC;
import org.graphity.server.model.QueriedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.system.SPINModuleRegistry;

/**
 * JAX-RS reader that validates instances read from RDF/POST request using SPIN constraints from the sitemap ontology.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://spinrdf.org/spin.html#spin-constraints">SPIN - Modeling Vocabulary</a>
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class ValidatingRDFPostReader extends RDFPostReader
{
    private static final Logger log = LoggerFactory.getLogger(ValidatingRDFPostReader.class);

    @Context private Providers providers;
    @Context private UriInfo uriInfo;

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        return validate(getOntModel(),
                super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream),
                getMode(),
                (QueriedResource)getUriInfo().getMatchedResources().get(0));
    }

    public Model validate(OntModel ontModel, Model model, URI mode, QueriedResource match)
    {
        OntModel tempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        tempModel.add(ontModel).add(model);
	SPINModuleRegistry.get().registerAll(tempModel, null);
	List<ConstraintViolation> cvs = SPINConstraints.check(tempModel, null);
	if (!cvs.isEmpty())
        {
            if (log.isDebugEnabled()) log.debug("SPIN constraint violations: {}", cvs);
            if (mode != null && mode.equals(URI.create(GC.EditMode.getURI()))) // check by HTTP request method?
            {
                throw new ConstraintViolationException(cvs, model);
            }
            else // gc:CreateMode
            {
                throw new ConstraintViolationException(cvs, match.describe().add(model));
            }
        }
        
        return model;
    }
    
    public OntModel getOntModel()
    {
	ContextResolver<OntModel> cr = getProviders().getContextResolver(OntModel.class, null);
	return cr.getContext(OntModel.class);
    }

    public Providers getProviders()
    {
        return providers;
    }

    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
    public URI getMode()
    {
        if (getUriInfo().getQueryParameters().containsKey(GC.mode.getLocalName()))
            return URI.create(getUriInfo().getQueryParameters().getFirst(GC.mode.getLocalName()));
                    
        return null;
    }
    
}
