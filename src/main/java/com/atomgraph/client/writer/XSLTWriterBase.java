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
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.core.util.Link;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.time.ZonedDateTime;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.lib.ResourceResolverWrappingURIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.value.DateTimeValue;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Model and ResultSet (X)HTML writers.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public abstract class XSLTWriterBase
{
    
    private static final Logger log = LoggerFactory.getLogger(XSLTWriterBase.class);
    private static final Set<String> NAMESPACES;
    static
    {
        NAMESPACES = new HashSet<>();
        NAMESPACES.add(AC.NS);
    }
    
    private final XsltExecutable xsltExec;
    private final OntModelSpec ontModelSpec;
    private final DataManager dataManager;

    @Context private UriInfo uriInfo;
    @Context private Request request;
    @Context private HttpHeaders httpHeaders;
    @Context private HttpServletRequest httpServletRequest;

    public XSLTWriterBase(XsltExecutable xsltExec, OntModelSpec ontModelSpec, DataManager dataManager)
    {
        if (xsltExec == null) throw new IllegalArgumentException("XsltExecutable cannot be null");
        if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");
        this.xsltExec = xsltExec;
        this.ontModelSpec = ontModelSpec;
        this.dataManager = dataManager;
    }

    public void transform(ByteArrayOutputStream baos, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws TransformerException, SaxonApiException
    {
        transform(getXsltExecutable().load30(), getDataManager(), baos, mediaType, getParameters(headerMap), entityStream);
    }
    
    public void transform(Xslt30Transformer xsltTrans, DataManager dataManager, ByteArrayOutputStream baos, MediaType mediaType, Map<QName, XdmValue> parameters, OutputStream entityStream) throws TransformerException, SaxonApiException
    {
        if (xsltTrans == null) throw new IllegalArgumentException("Xslt30Transformer cannot be null");
        if (dataManager == null) throw new IllegalArgumentException("DataManager cannot be null");

        Serializer out = xsltTrans.newSerializer();
        out.setOutputStream(entityStream);
        out.setOutputProperty(Serializer.Property.ENCODING, UTF_8.name());

        if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE))
        {
            out.setOutputProperty(Serializer.Property.METHOD, "html");
            out.setOutputProperty(Serializer.Property.HTML_VERSION, "5.0");
            out.setOutputProperty(Serializer.Property.MEDIA_TYPE, MediaType.TEXT_HTML);
            out.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC, "");
            out.setOutputProperty(Serializer.Property.DOCTYPE_SYSTEM, "");
        }
        if (mediaType.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
        {
            out.setOutputProperty(Serializer.Property.METHOD, "xhtml");
            out.setOutputProperty(Serializer.Property.HTML_VERSION, "5.0");
            out.setOutputProperty(Serializer.Property.MEDIA_TYPE, MediaType.APPLICATION_XHTML_XML);
            out.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC, "");
            out.setOutputProperty(Serializer.Property.DOCTYPE_SYSTEM, "");
        }

        xsltTrans.setResourceResolver(new ResourceResolverWrappingURIResolver((URIResolver)dataManager));
        xsltTrans.getUnderlyingController().setUnparsedTextURIResolver((UnparsedTextURIResolver)dataManager);
        xsltTrans.getUnderlyingController().setCurrentDateTime(DateTimeValue.fromZonedDateTime(ZonedDateTime.now())); // TO-DO: make TZ configurable
        if (parameters != null) xsltTrans.setStylesheetParameters(parameters);
        
        try
        {
            Source source = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
            String systemId = getSystemId();
            if (systemId != null) source.setSystemId(systemId); // systemId value is used for base-uri()
            xsltTrans.transform(source, out);
        }
        catch (URISyntaxException ex)
        {
            if (log.isErrorEnabled()) log.error("URI syntax exception: {}", ex.getMessage());
            throw new TransformerException(ex);
        }
    }
    
    public <T extends XdmValue> Map<QName, XdmValue> getParameters(MultivaluedMap<String, Object> headerMap) throws TransformerException
    {
        if (headerMap == null) throw new IllegalArgumentException("MultivaluedMap cannot be null");
        
        Map<QName, XdmValue> params = new HashMap<>();
        
        params.put(new QName("ac", AC.httpHeaders.getNameSpace(), AC.httpHeaders.getLocalName()), new XdmAtomicValue(headerMap.toString()));
        params.put(new QName("ac", AC.method.getNameSpace(), AC.method.getLocalName()), new XdmAtomicValue(getRequest().getMethod()));
        params.put(new QName("ac", AC.contextUri.getNameSpace(), AC.contextUri.getLocalName()), new XdmAtomicValue(getContextURI()));
     
        try
        {
            //if (getURI() != null) params.put(new QName("ac", AC.uri.getNameSpace(), AC.uri.getLocalName()), new XdmAtomicValue(getURI()));
            if (getEndpointURI() != null) params.put(new QName("ac", AC.endpoint.getNameSpace(), AC.endpoint.getLocalName()), new XdmAtomicValue(getEndpointURI()));
            if (getQuery() != null) params.put(new QName("ac", AC.query.getNameSpace(), AC.query.getLocalName()), new XdmAtomicValue(getQuery()));

            List<URI> modes = getModes(getSupportedNamespaces()); // check if explicit mode URL parameter is provided
            if (!modes.isEmpty()) params.put(new QName("ac", AC.mode.getNameSpace(), AC.mode.getLocalName()), XdmValue.makeSequence(modes));

            URI ontologyURI = getLinkURI(headerMap, LDT.ontology);
            if (ontologyURI != null) params.put(new QName("ldt", LDT.ontology.getNameSpace(), LDT.ontology.getLocalName()), new XdmAtomicValue(ontologyURI));

            URI baseURI = getLinkURI(headerMap, LDT.base);
            if (baseURI != null) params.put(new QName("ldt", LDT.base.getNameSpace(), LDT.base.getLocalName()), new XdmAtomicValue(baseURI));

            String forClassURI = getUriInfo().getQueryParameters().getFirst(AC.forClass.getLocalName());
            if (forClassURI != null) params.put(new QName("ac", AC.forClass.getNameSpace(), AC.forClass.getLocalName()), new XdmAtomicValue(URI.create(forClassURI)));
            
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

    public String getSystemId() throws URISyntaxException
    {
        URI uri = getURI();
        
        if (uri != null) return uri.toString();
        
        return null;
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

    public URI getContextURI()
    {
        return URI.create(getHttpServletRequest().getRequestURL().toString()).
                resolve(getHttpServletRequest().getContextPath() + "/");
    }

    public Set<String> getSupportedNamespaces()
    {
        return NAMESPACES;
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
        Checker.iriViolations(classIRI);

        return classIRI;
    }
    
    public URI getLinkURI(MultivaluedMap<String, Object> headerMap, ObjectProperty property)
    {
        if (headerMap.get(HttpHeaders.LINK) == null) return null;
        
        List<URI> baseLinks = headerMap.get(HttpHeaders.LINK).
            stream().
            map((Object header) ->
            {
                try
                {
                    return Link.valueOf(header.toString());
                }
                catch (URISyntaxException ex)
                {
                    if (log.isWarnEnabled()) log.warn("Could not parse Link URI", ex);
                    return null;
                }
            }).
            filter(link -> link != null && link.getRel().equals(property.getURI())).
            map(link -> link.getHref()).
            collect(Collectors.toList());

        if (!baseLinks.isEmpty()) return baseLinks.get(0);

        return null;
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
    
    public XsltExecutable getXsltExecutable()
    {
        return xsltExec;
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }
}
