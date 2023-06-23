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
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.SysRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ModelXSLTWriter extends XSLTWriterBase implements MessageBodyWriter<Model>
{

    private static final Logger log = LoggerFactory.getLogger(ModelXSLTWriter.class);

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
    
    @Override
    public long getSize(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }
    
    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            Map<String, Object> properties = new HashMap<>() ;
            properties.put("allowBadURIs", "true"); // round-tripping RDF/POST with user input may contain invalid URIs
            org.apache.jena.sparql.util.Context cxt = new org.apache.jena.sparql.util.Context();
            cxt.set(SysRIOT.sysRdfWriterProperties, properties);
        
            RDFWriter.create().
                format(RDFFormat.RDFXML_PLAIN).
                context(cxt).
                source(model).
                output(baos);
            
            transform(baos, mediaType, headerMap, entityStream);
        }
        catch (TransformerException | SaxonApiException ex)
        {
            if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
            throw new InternalServerErrorException(ex);
        }
    }
    
    public StreamSource getSource(Model model) throws IOException
    {
        if (model == null) throw new IllegalArgumentException("Model cannot be null");
        if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            model.write(stream, RDFLanguages.RDFXML.getName(), null);

            if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
        }
    }
    
}
