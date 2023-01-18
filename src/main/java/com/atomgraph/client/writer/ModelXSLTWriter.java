/*
 * Copyright 2020 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.client.writer;

import com.atomgraph.client.util.DataManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response.
 * Needs to be registered in the JAX-RS application.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see org.apache.jena.rdf.model.Model
 * @see jakarta.ws.rs.ext.MessageBodyWriter
 */
@Provider
@Singleton
@Produces({MediaType.TEXT_HTML + ";charset=UTF-8", MediaType.APPLICATION_XHTML_XML + ";charset=UTF-8"})
public class ModelXSLTWriter extends ModelXSLTWriterBase implements MessageBodyWriter<Model>
{

    /**
     * Constructs model writer from XSLT executable and ontology model specification.
     * 
     * @param xsltExec compiled XSLT stylesheet
     * @param ontModelSpec ontology model specification
     * @param dataManager RDF data manager
     */
    public ModelXSLTWriter(XsltExecutable xsltExec, OntModelSpec ontModelSpec, DataManager dataManager)
    {
        super(xsltExec, ontModelSpec, dataManager);
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Model.class.isAssignableFrom(type);
    }
    
}
