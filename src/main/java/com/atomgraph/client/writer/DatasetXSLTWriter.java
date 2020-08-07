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

import com.atomgraph.client.exception.OntClassNotFoundException;
import com.atomgraph.client.exception.OntologyException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdfxml.xmloutput.impl.Basic;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.checker.CheckerIRI;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.util.OntologyProvider;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.vocabulary.LDT;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see org.apache.jena.query.Dataset
 * @see javax.ws.rs.ext.MessageBodyWriter
 */
@Provider
@Singleton
@Produces({MediaType.TEXT_HTML + ";charset=UTF-8"}) // MediaType.APPLICATION_XHTML_XML + ";charset=UTF-8", 
public class DatasetXSLTWriter implements MessageBodyWriter<Dataset>
{
    private static final Logger log = LoggerFactory.getLogger(DatasetXSLTWriter.class);

    private static final Set<String> NAMESPACES;
    static
    {
        NAMESPACES = new HashSet<>();
        NAMESPACES.add(AC.NS);
    }

//    private final Xslt30Transformer xsltTrans;
    private final XsltExecutable xsltExec;
    private final OntModelSpec ontModelSpec;

    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private HttpServletRequest httpServletRequest;
    
    @Inject DataManager dataManager;
    
    /**
     * Constructs from XSLT builder.
     * 
     * @param xsltExec compiled XSLT stylesheet
     * @param ontModelSpec ontology model specification
     */
    public DatasetXSLTWriter(XsltExecutable xsltExec, OntModelSpec ontModelSpec)
    {
        if (xsltExec == null) throw new IllegalArgumentException("XsltExecutable cannot be null");
        if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");
        this.xsltExec = xsltExec;
        this.ontModelSpec = ontModelSpec;
    }
    
    @Override
    public void writeTo(Dataset dataset, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            //RDFWriter writer = model.getWriter(RDFLanguages.RDFXML.getName());
            RDFWriter writer = new Basic(); // workaround for Jena 3.0.1 bug: https://issues.apache.org/jira/browse/JENA-1168
            writer.setProperty("allowBadURIs", true); // round-tripping RDF/POST with user input may contain invalid URIs
            writer.write(dataset.getDefaultModel(), baos, null);

            Xslt30Transformer xsltTrans = getXsltExecutable().load30();
            Serializer out = xsltTrans.newSerializer();
            out.setOutputStream(entityStream);
            out.setOutputProperty(Serializer.Property.ENCODING, UTF_8.name());

            if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE))
            {
                out.setOutputProperty(Serializer.Property.METHOD, "html");
                out.setOutputProperty(Serializer.Property.MEDIA_TYPE, MediaType.TEXT_HTML);
                out.setOutputProperty(Serializer.Property.DOCTYPE_SYSTEM, "http://www.w3.org/TR/html4/strict.dtd");
                out.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01//EN");
            }
            if (mediaType.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            {
                out.setOutputProperty(Serializer.Property.METHOD, "xhtml");
                out.setOutputProperty(Serializer.Property.MEDIA_TYPE, MediaType.APPLICATION_XHTML_XML);
                out.setOutputProperty(Serializer.Property.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
                out.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Strict//EN");
            }

            xsltTrans.setURIResolver((URIResolver)getDataManager());
            xsltTrans.getUnderlyingController().setUnparsedTextURIResolver((UnparsedTextURIResolver)getDataManager());
            xsltTrans.setStylesheetParameters(getParameters(dataset, headerMap));
            xsltTrans.transform(new StreamSource(new ByteArrayInputStream(baos.toByteArray())), out);
        }
        catch (TransformerException | SaxonApiException ex)
        {
            if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR); // TO-DO: make Mapper
        }
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Dataset.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Dataset dataset, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }
    
    public URI getAbsolutePath()
    {
        return getUriInfo().getAbsolutePath();
    }
    
    public URI getRequestURI()
    {
        return getUriInfo().getRequestUri();
    }

    public URI getURIParam(UriInfo uriInfo, String name) throws URISyntaxException
    {
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        if (name == null) throw new IllegalArgumentException("String cannot be null");

        if (uriInfo.getQueryParameters().containsKey(name))
            return new URI(uriInfo.getQueryParameters().getFirst(name));
        
        return null;
    }

    public URI getURI() throws URISyntaxException
    {
        return getURIParam(getUriInfo(), AC.uri.getLocalName()); // TO-DO: remove possible #fragment from URI
    }

    public URI getEndpointURI() throws URISyntaxException
    {
        return getURIParam(getUriInfo(), AC.endpoint.getLocalName());
    }

    public String getQuery()
    {
        if (getUriInfo().getQueryParameters().containsKey(AC.query.getLocalName()))
            return getUriInfo().getQueryParameters().getFirst(AC.query.getLocalName());
        
        return null;
    }
    
    public static StreamSource getSource(Model model) throws IOException
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
    
    public URI getContextURI()
    {
        return URI.create(getHttpServletRequest().getRequestURL().toString()).
                resolve(getHttpServletRequest().getContextPath() + "/");
    }

    public <T extends XdmValue> Map<QName, XdmValue> getParameters(Dataset dataset, MultivaluedMap<String, Object> headerMap) throws TransformerException
    {
        if (headerMap == null) throw new IllegalArgumentException("MultivaluedMap cannot be null");
        
        Map<QName, XdmValue> params = new HashMap<>();
        
        params.put(new QName("ac", AC.httpHeaders.getNameSpace(), AC.httpHeaders.getLocalName()), new XdmAtomicValue(headerMap.toString()));
        params.put(new QName("ac", AC.method.getNameSpace(), AC.method.getLocalName()), new XdmAtomicValue(getRequest().getMethod()));
        params.put(new QName("ac", AC.requestUri.getNameSpace(), AC.requestUri.getLocalName()), new XdmAtomicValue(getRequestURI()));
        params.put(new QName("ac", AC.contextUri.getNameSpace(), AC.contextUri.getLocalName()), new XdmAtomicValue(getContextURI()));
     
        try
        {
            if (getURI() != null) params.put(new QName("ac", AC.uri.getNameSpace(), AC.uri.getLocalName()), new XdmAtomicValue(getURI()));
            if (getEndpointURI() != null) params.put(new QName("ac", AC.endpoint.getNameSpace(), AC.endpoint.getLocalName()), new XdmAtomicValue(getEndpointURI()));
            if (getQuery() != null) params.put(new QName("ac", AC.query.getNameSpace(), AC.query.getLocalName()), new XdmAtomicValue(getQuery()));

            List<URI> modes = getModes(getSupportedNamespaces()); // check if explicit mode URL parameter is provided
            params.put(new QName("ac", AC.mode.getNameSpace(), AC.mode.getLocalName()), XdmValue.makeSequence(modes));

            URI ontologyURI = (URI)getHttpServletRequest().getAttribute(LDT.ontology.getURI());
            if (ontologyURI != null)
            {
                params.put(new QName("ldt", LDT.ontology.getNameSpace(), LDT.ontology.getLocalName()), new XdmAtomicValue(ontologyURI));

                OntModel sitemap = getOntModel(ontologyURI.toString());
                
                if (getBaseUri() != null)
                {
                    params.put(new QName("ldt", LDT.base.getNameSpace(), LDT.base.getLocalName()), new XdmAtomicValue(getBaseUri()));

                    String forClassURI = getUriInfo().getQueryParameters().getFirst(AC.forClass.getLocalName());
                    if (forClassURI != null)
                    {
                        OntClass forClass = sitemap.getOntClass(forClassURI);

                        if (forClass == null) throw new OntClassNotFoundException(forClassURI); // do we need this check here?
                        params.put(new QName("ac", AC.forClass.getNameSpace(), AC.forClass.getLocalName()), new XdmAtomicValue(URI.create(forClass.getURI())));
                    }
                }

                if (getTemplateURI() != null)
                {
                    params.put(new QName("ldt", LDT.template.getNameSpace(), LDT.template.getLocalName()), new XdmAtomicValue(getTemplateURI()));
                    if (modes.isEmpty()) // attempt to retrieve default mode via matched template Link from the app (server) sitemap ontology
                    {
                        Resource template = sitemap.getResource(getTemplateURI().toString());

                        StmtIterator it = template.listProperties(AC.mode);
                        try
                        {
                            while (it.hasNext())
                            {
                                Statement modeStmt = it.next();

                                if (!modeStmt.getObject().isURIResource())
                                    throw new OntologyException("Value is not a URI resource", template, AC.mode);

                                modes.add(URI.create(modeStmt.getResource().getURI()));
                            }
                        }
                        finally
                        {
                            it.close();
                        }
                    }
                }
            }

            Locale locale = (Locale)headerMap.getFirst(HttpHeaders.CONTENT_LANGUAGE);
            if (locale != null)
            {
                if (log.isDebugEnabled()) log.debug("Writing Model using language: {}", locale.toLanguageTag());
                params.put(new QName("ldt", LDT.lang.getNameSpace(), LDT.lang.getLocalName()), new XdmAtomicValue(locale.toLanguageTag()));
            }

            return params;
        }
        catch (URISyntaxException ex)
        {
            if (log.isErrorEnabled()) log.error("URI syntax exception: {}", ex.getMessage());
            throw new TransformerException(ex);
        }
    }
    
    public Set<String> getSupportedNamespaces()
    {
        return NAMESPACES;
    }
    
    public OntDocumentManager getOntDocumentManager()
    {
        return OntDocumentManager.getInstance();
    }
    
    public OntModel getOntModel(String ontologyURI)
    {
        return getOntModel(ontologyURI, getOntModelSpec());
    }
    
    public OntModel getOntModel(String ontologyURI, OntModelSpec ontModelSpec)
    {
        return new OntologyProvider().getOntModel(getOntDocumentManager(), ontologyURI, ontModelSpec);
    }
    
    public List<URI> getModes(Set<String> namespaces)
    {
        return getModes(getUriInfo(), namespaces);
    }
    
    public List<URI> getModes(UriInfo uriInfo, Set<String> namespaces) // mode is a client parameter, no need to parse hypermedia state here
    {
        if (uriInfo == null) throw new IllegalArgumentException("UriInfo cannot be null");
        if (namespaces == null) throw new IllegalArgumentException("Namespace Set cannot be null");
        
        List<URI> modes = new ArrayList<>();
        
        if (uriInfo.getQueryParameters().containsKey(AC.mode.getLocalName()))
        {
            // matching parameter names not to namespace URIs, but to local names instead!
            List<String> modeParamValues = uriInfo.getQueryParameters().get(AC.mode.getLocalName());
            for (String modeParamValue : modeParamValues)
            {
                Resource paramMode = ResourceFactory.createResource(modeParamValue);
                // only consider values from the known namespaces
                if (namespaces.contains(paramMode.getNameSpace())) modes.add(URI.create(modeParamValue));
            }
        }
        
        return modes;
    }
    
    public static IRI checkURI(String classIRIStr)
    {
        if (classIRIStr == null) throw new IllegalArgumentException("URI String cannot be null");

        IRI classIRI = IRIFactory.iriImplementation().create(classIRIStr);
        // throws Exceptions on bad URIs:
        CheckerIRI.iriViolations(classIRI, ErrorHandlerFactory.getDefaultErrorHandler());

        return classIRI;
    }
    
    public URI getBaseUri()
    {
        return (URI)getHttpServletRequest().getAttribute(LDT.base.getURI()); // set in ProxyResourceBase
    }
    
    public URI getTemplateURI()
    {
        return (URI)getHttpServletRequest().getAttribute(LDT.template.getURI()); // set in ProxyResourceBase
    }
    
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }

    public Request getRequest()
    {
        return request;
    }
    
    public HttpHeaders getHttpHeaders()
    {
        return httpHeaders;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

    public OntModelSpec getOntModelSpec()
    {
        return ontModelSpec;
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }

    public XsltExecutable getXsltExecutable()
    {
        return xsltExec;
    }
    
}