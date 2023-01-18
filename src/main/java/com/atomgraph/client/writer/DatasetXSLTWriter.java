/**
 *  Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.atomgraph.client.writer;

import com.atomgraph.client.util.DataManager;
import org.apache.jena.ontology.OntModelSpec;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.inject.Singleton;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.query.Dataset;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response.
 * Needs to be registered in the JAX-RS application.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see org.apache.jena.query.Dataset
 * @see jakarta.ws.rs.ext.MessageBodyWriter
 */
@Provider
@Singleton
@Produces({MediaType.TEXT_HTML + ";charset=UTF-8", MediaType.APPLICATION_XHTML_XML + ";charset=UTF-8"})
@Deprecated
public class DatasetXSLTWriter extends ModelXSLTWriterBase implements MessageBodyWriter<Dataset>
{
    
    /**
     * Constructs dataset writer from XSLT executable and ontology model specification.
     * 
     * @param xsltExec compiled XSLT stylesheet
     * @param ontModelSpec ontology model specification
     * @param dataManager RDF data manager
     */
    public DatasetXSLTWriter(XsltExecutable xsltExec, OntModelSpec ontModelSpec, DataManager dataManager)
    {
        super(xsltExec, ontModelSpec, dataManager);
    }
    
    @Override
    public void writeTo(Dataset dataset, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException
    {
        super.writeTo(dataset.getDefaultModel(), type, genericType, annotations, mediaType, headerMap, entityStream);
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Dataset.class.isAssignableFrom(type);
    }

}