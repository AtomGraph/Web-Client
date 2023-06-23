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
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.inject.Singleton;
import jakarta.ws.rs.InternalServerErrorException;
import javax.xml.transform.TransformerException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.ontology.OntModelSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms SPARQL XML results with XSLT stylesheet and writes result to response.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/ResultSet.html">ResultSet</a>
 * @see <a href="https://jakarta.ee/specifications/restful-ws/3.0/apidocs/jakarta/ws/rs/ext/messagebodywriter">MessageBodyWriter</a>
 */
@Singleton
public class ResultSetXSLTWriter extends XSLTWriterBase implements MessageBodyWriter<ResultSet>
{
    private static final Logger log = LoggerFactory.getLogger(ResultSetXSLTWriter.class);

    /**
     * Constructs model writer from XSLT executable and ontology model specification.
     * 
     * @param xsltExec compiled XSLT stylesheet
     * @param ontModelSpec ontology model specification
     * @param dataManager RDF data manager
     */
    public ResultSetXSLTWriter(XsltExecutable xsltExec, OntModelSpec ontModelSpec, DataManager dataManager)
    {
        super(xsltExec, ontModelSpec, dataManager);
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return (ResultSet.class.isAssignableFrom(type));
    }

    @Override
    public long getSize(ResultSet results, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(ResultSet results, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            ResultSetFormatter.outputAsXML(baos, results);

            transform(baos, mediaType, headerMap, entityStream);
        }
        catch (TransformerException | SaxonApiException ex)
        {
            if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
            throw new InternalServerErrorException(ex);
        }
    }
    
}
