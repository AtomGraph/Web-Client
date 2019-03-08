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
import com.atomgraph.client.util.Constructor;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import com.sun.jersey.spi.resource.Singleton;
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
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdfxml.xmloutput.impl.Basic;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.checker.CheckerIRI;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.util.OntologyProvider;
import com.atomgraph.client.util.XSLTBuilder;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.vocabulary.LDT;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.xml.transform.sax.SAXTransformerFactory;
import net.sf.saxon.trans.UnparsedTextURIResolver;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
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
    
    private final Templates templates;
    private final OntModelSpec ontModelSpec;

    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private Providers providers;
    @Context private HttpServletRequest httpServletRequest;
    
    /**
     * Constructs from XSLT builder.
     * 
     * @param templates compiled XSLT stylesheet
     * @param ontModelSpec ontology model specification
     * @see com.atomgraph.client.util.XSLTBuilder
     */
    public DatasetXSLTWriter(Templates templates, OntModelSpec ontModelSpec)
    {
        if (templates == null) throw new IllegalArgumentException("Templates cannot be null");
        if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");
        this.templates = templates;
        this.ontModelSpec = ontModelSpec;
    }
    
    @Override
    public void writeTo(Dataset dataset, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            Templates stylesheet = getTemplates();
            
            //RDFWriter writer = model.getWriter(RDFLanguages.RDFXML.getName());
            RDFWriter writer = new Basic(); // workaround for Jena 3.0.1 bug: https://issues.apache.org/jira/browse/JENA-1168
            writer.setProperty("allowBadURIs", true); // round-tripping RDF/POST with user input may contain invalid URIs
            writer.write(dataset.getDefaultModel(), baos, null);
            
            XSLTBuilder builder = setParameters(com.atomgraph.client.util.saxon.XSLTBuilder.newInstance(getTransformerFactory()).
                    resolver((UnparsedTextURIResolver)getDataManager()).
                    stylesheet(stylesheet).
                    document(new ByteArrayInputStream(baos.toByteArray())),
                    dataset,
                    headerMap).
                resolver(getDataManager()).
                result(new StreamResult(entityStream));

            builder.outputProperty(OutputKeys.ENCODING, UTF_8.name());
            if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE))
            {
                builder.outputProperty(OutputKeys.METHOD, "html");
                builder.outputProperty(OutputKeys.MEDIA_TYPE, MediaType.TEXT_HTML);
                builder.outputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/html4/strict.dtd");
                builder.outputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01//EN");
            }
            if (mediaType.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
            {
                builder.outputProperty(OutputKeys.METHOD, "xhtml");
                builder.outputProperty(OutputKeys.MEDIA_TYPE, MediaType.APPLICATION_XHTML_XML);
                builder.outputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
                builder.outputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Strict//EN");
            }
            
            builder.transform();
        }
        catch (TransformerException ex)
        {
            if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR); // TO-DO: make Mapper
        }
    }

    public void write(Dataset dataset, OutputStream entityStream) throws TransformerException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dataset.getDefaultModel().write(baos, RDFLanguages.RDFXML.getName(), null);

        XSLTBuilder.newInstance(getTransformerFactory()).
            stylesheet(getTemplates()).
            document(new ByteArrayInputStream(baos.toByteArray())).
            resolver(getDataManager()).
            result(new StreamResult(entityStream)).
            transform();
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
        return getURIParam(getUriInfo(), AC.uri.getLocalName());
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
    
    public static Source getSource(Model model) throws IOException
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
    
    public static Source getSource(OntModel ontModel, boolean writeAll) throws IOException
    {
        if (!writeAll) return getSource(ontModel);
        if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");

        if (log.isDebugEnabled()) log.debug("Number of OntModel stmts read: {}", ontModel.size());

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            ontModel.writeAll(stream, Lang.RDFXML.getName(), null);

            if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
            return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
        }
    }
    
    public URI getContextURI()
    {
        return URI.create(getHttpServletRequest().getRequestURL().toString()).
                resolve(getHttpServletRequest().getContextPath() + "/");
    }
    
    public Templates getTemplates()
    {
        if (templates != null) return templates;
        else
        {
            ContextResolver<Templates> cr = getProviders().getContextResolver(Templates.class, null);
            return cr.getContext(Templates.class);
        }
    }

    public XSLTBuilder setParameters(XSLTBuilder builder, Dataset dataset, MultivaluedMap<String, Object> headerMap) throws TransformerException
    {
        if (builder == null) throw new IllegalArgumentException("XSLTBuilder cannot be null");
        if (headerMap == null) throw new IllegalArgumentException("MultivaluedMap cannot be null");
        
        builder.
            //parameter("{" + A.absolutePath.getNameSpace() + "}" + A.absolutePath.getLocalName(), getAbsolutePath()).
            //parameter("{" + A.requestUri.getNameSpace() + "}" + A.requestUri.getLocalName(), getRequestURI()).
//            parameter("{" + A.method.getNameSpace() + "}" + A.method.getLocalName(), getRequest().getMethod()).
//            parameter("{" + A.httpHeaders.getNameSpace() + "}" + A.httpHeaders.getLocalName(), headerMap.toString()).
            parameter("{" + AC.requestUri.getNameSpace() + "}" + AC.requestUri.getLocalName(), getRequestURI()).
            parameter("{" + AC.contextUri.getNameSpace() + "}" + AC.contextUri.getLocalName(), getContextURI());
     
        try
        {
            if (getURI() != null) builder.parameter("{" + AC.uri.getNameSpace() + "}" + AC.uri.getLocalName(), getURI());
            if (getEndpointURI() != null) builder.parameter("{" + AC.endpoint.getNameSpace() + "}" + AC.endpoint.getLocalName(), getEndpointURI());
            if (getQuery() != null) builder.parameter("{" + AC.query.getNameSpace() + "}" + AC.query.getLocalName(), getQuery());

            List<URI> modes = getModes(getSupportedNamespaces()); // check if explicit mode URL parameter is provided

            URI ontologyURI = (URI)getHttpServletRequest().getAttribute(LDT.ontology.getURI());
            if (ontologyURI != null)
            {
                builder.parameter("{" + LDT.ontology.getNameSpace() + "}" + LDT.ontology.getLocalName(), ontologyURI);

                OntModel sitemap = getOntModel(ontologyURI.toString());
                builder.parameter("{" + AC.sitemap.getNameSpace() + "}" + AC.sitemap.getLocalName(), getSource(sitemap, true));

                if (getBaseUri() != null)
                {
                    builder.parameter("{" + LDT.base.getNameSpace() + "}" + LDT.base.getLocalName(), getBaseUri());

                    String forClassURI = getUriInfo().getQueryParameters().getFirst(AC.forClass.getLocalName());
                    if (forClassURI != null)
                    {
                        OntClass forClass = sitemap.getOntClass(forClassURI);

                        if (forClass == null) throw new OntClassNotFoundException(forClassURI); // do we need this check here?
                        builder.parameter("{" + AC.forClass.getNameSpace() + "}" + AC.forClass.getLocalName(), URI.create(forClass.getURI()));
                    }
                }

                if (getTemplateURI() != null)
                {
                    builder.parameter("{" + LDT.template.getNameSpace() + "}" + LDT.template.getLocalName(), getTemplateURI());
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
            
            builder.parameter("{" + AC.mode.getNameSpace() + "}" + AC.mode.getLocalName(), modes);

            MediaType contentType = (MediaType)headerMap.getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType != null)
            {
                if (log.isDebugEnabled()) log.debug("Writing Model using XSLT media type: {}", contentType);
                builder.outputProperty(OutputKeys.MEDIA_TYPE, contentType.toString());
            }
            Locale locale = (Locale)headerMap.getFirst(HttpHeaders.CONTENT_LANGUAGE);
            if (locale != null)
            {
                if (log.isDebugEnabled()) log.debug("Writing Model using language: {}", locale.toLanguageTag());
                builder.parameter("{" + LDT.lang.getNameSpace() + "}" + LDT.lang.getLocalName(), locale.toLanguageTag());
            }

            return builder;
        }
        catch (IOException ex)
        {
            if (log.isErrorEnabled()) log.error("Error reading Source stream");
            throw new TransformerException(ex);
        }
        catch (URISyntaxException ex)
        {
            if (log.isErrorEnabled()) log.error("URI syntax exception");
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

    public static Source getConstructedSource(URI ontologyURI, List<URI> classURIs, URI baseURI) throws URISyntaxException, IOException
    {
        if (ontologyURI == null) throw new IllegalArgumentException("Ontology URI cannot be null");
        if (classURIs == null) throw new IllegalArgumentException("Class URIs cannot be null");
        if (baseURI == null) throw new IllegalArgumentException("Base URI cannot be null");

        OntModel ontModel = OntDocumentManager.getInstance().getOntology(ontologyURI.toString(), OntModelSpec.OWL_MEM);
        Model instances = ModelFactory.createDefaultModel();

        for (URI classURI : classURIs)
        {
            OntClass forClass = ontModel.getOntClass(checkURI(classURI.toString()).toURI().toString());
            if (forClass != null) new Constructor().construct(forClass, instances, baseURI.toString()); // TO-DO: else throw error?
        }

        return getSource(instances);
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
 
    public Providers getProviders()
    {
        return providers;
    }

    public DataManager getDataManager()
    {
        ContextResolver<DataManager> cr = getProviders().getContextResolver(DataManager.class, null);
        return cr.getContext(DataManager.class);
    }
    
    public SAXTransformerFactory getTransformerFactory()
    {
        return ((SAXTransformerFactory)TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null));
    }
    
}