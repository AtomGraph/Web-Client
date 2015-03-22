/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.writer;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.resource.Singleton;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.graphity.client.util.DataManager;
import org.graphity.client.util.XSLTBuilder;
import org.graphity.client.vocabulary.GC;
import org.graphity.processor.util.Link;
import org.graphity.processor.vocabulary.GP;
import org.graphity.core.provider.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms RDF with XSLT stylesheet and writes (X)HTML result to response.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html">Model</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyWriter.html">MessageBodyWriter</a>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML,MediaType.TEXT_HTML}) // MediaType.APPLICATION_XML ?
public class ModelXSLTWriter extends ModelProvider // implements RDFWriter
{
    private static final Logger log = LoggerFactory.getLogger(ModelXSLTWriter.class);

    private Templates templates;
 
    @Context private UriInfo uriInfo;
    @Context private HttpHeaders httpHeaders;
    @Context private ServletConfig servletConfig;
    @Context private Providers providers;
    @Context private HttpServletRequest httpServletRequest;
    
    /**
     * Constructs from XSLT builder.
     * 
     * @param templates
     * @see org.graphity.client.util.XSLTBuilder
     */
    public ModelXSLTWriter(Templates templates)
    {
	if (templates == null) throw new IllegalArgumentException("Templates cannot be null");
	this.templates = templates;
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
    
    public ModelXSLTWriter()
    {
        this.templates = null;
    }
    
    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headerMap, OutputStream entityStream) throws IOException, WebApplicationException
    {
	if (log.isTraceEnabled()) log.trace("Writing Model with HTTP headers: {} MediaType: {}", headerMap, mediaType);

	try
	{
            synchronized (this)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                model.write(baos, RDFLanguages.RDFXML.getName(), null);
                
                getXSLTBuilder(XSLTBuilder.fromStylesheet(getTemplates()).document(new ByteArrayInputStream(baos.toByteArray())),
                        headerMap).
                    resolver(getDataManager()).
                    result(new StreamResult(entityStream)).
                    transform();
            }
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformation failed", ex);
	    throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
	}
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Model.class.isAssignableFrom(type);
    }
    
    public Source getSource(Model model)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");	
	if (log.isDebugEnabled()) log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	model.write(stream, RDFLanguages.RDFXML.getName(), null);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }
    
    public Source getSource(OntModel ontModel, boolean writeAll)
    {
	if (!writeAll) return getSource(ontModel);
	if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");	
	
	if (log.isDebugEnabled()) log.debug("Number of OntModel stmts read: {}", ontModel.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ontModel.writeAll(stream, Lang.RDFXML.getName(), null);

	if (log.isDebugEnabled()) log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }
    
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public HttpHeaders getHttpHeaders()
    {
	return httpHeaders;
    }

    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
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

    public XSLTBuilder getXSLTBuilder(XSLTBuilder bld, MultivaluedMap<String, Object> headerMap) throws TransformerConfigurationException
    {        
	    bld.parameter("{" + GP.baseUri.getNameSpace() + "}" + GP.baseUri.getLocalName(), getUriInfo().getBaseUri()).
	    parameter("{" + GP.absolutePath.getNameSpace() + "}" + GP.absolutePath.getLocalName(), getUriInfo().getAbsolutePath()).
	    parameter("{" + GP.requestUri.getNameSpace() + "}" + GP.requestUri.getLocalName(), getUriInfo().getRequestUri()).
	    parameter("{" + GP.httpHeaders.getNameSpace() + "}" + GP.httpHeaders.getLocalName(), headerMap.toString()).
	    parameter("{" + GC.contextUri.getNameSpace() + "}" + GC.contextUri.getLocalName(), getContextURI());
     
        if (headerMap.containsKey("Link"))
        {
            Link classLink = Link.valueOf(headerMap.getFirst("Link").toString());
	    bld.parameter("{" + RDF.type.getNameSpace() + "}" + RDF.type.getLocalName(), classLink.getHref());
            OntModel sitemap = OntDocumentManager.getInstance().getOntology(classLink.getHref().toString(), OntModelSpec.OWL_MEM);
            bld.parameter("{" + GP.sitemap.getNameSpace() + "}" + GP.sitemap.getLocalName(), getSource(sitemap, true)); // $ont-model from the current Resource (with imports)
        }
        
	Object contentType = headerMap.getFirst(HttpHeaders.CONTENT_TYPE);
	if (contentType != null)
	{
	    if (log.isDebugEnabled()) log.debug("Writing Model using XSLT media type: {}", contentType);
	    bld.outputProperty(OutputKeys.MEDIA_TYPE, contentType.toString());
	}
	Object contentLanguage = headerMap.getFirst(HttpHeaders.CONTENT_LANGUAGE);
	if (contentLanguage != null)
	{
	    if (log.isDebugEnabled()) log.debug("Writing Model using language: {}", contentLanguage.toString());
	    bld.parameter("{" + GP.lang.getNameSpace() + "}" + GP.lang.getLocalName(), contentLanguage.toString());
	}

        // pass HTTP query parameters into XSLT, ignore reserved param names (as params cannot be unset)
	Iterator<Entry<String, List<String>>> it = getUriInfo().getQueryParameters().entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, List<String>> entry = it.next();
            bld.parameter(entry.getKey(), entry.getValue().get(0)); // set string value
            if (log.isDebugEnabled()) log.debug("Setting XSLT param \"{}\" from HTTP query string with value: {}", entry.getKey(), entry.getValue().get(0));
        }

        // override the reserved parameters that need special types
	if (getUriInfo().getQueryParameters().getFirst(GP.offset.getLocalName()) != null)
	    bld.parameter("{" + GP.offset.getNameSpace() + "}" + GP.offset.getLocalName(),
                Long.valueOf(getUriInfo().getQueryParameters().getFirst(GP.offset.getLocalName())));
	if (getUriInfo().getQueryParameters().getFirst(GP.limit.getLocalName()) != null)
	    bld.parameter("{" + GP.limit.getNameSpace() + "}" + GP.limit.getLocalName(),
                Long.valueOf(getUriInfo().getQueryParameters().getFirst(GP.limit.getLocalName())));
	if (getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName()) != null)
	    bld.parameter("{" + GP.orderBy.getNameSpace() + "}" + GP.orderBy.getLocalName(),
                getUriInfo().getQueryParameters().getFirst(GP.orderBy.getLocalName()));
        if (getUriInfo().getQueryParameters().getFirst(GP.desc.getLocalName()) != null)
	    bld.parameter("{" + GP.desc.getNameSpace() + "}" + GP.desc.getLocalName(),
                Boolean.valueOf(getUriInfo().getQueryParameters().getFirst(GP.desc.getLocalName())));
	if (getUriInfo().getQueryParameters().getFirst(GP.lang.getLocalName()) != null)
	    bld.parameter("{" + GP.lang.getNameSpace() + "}" + GP.lang.getLocalName(),
                getUriInfo().getQueryParameters().getFirst(GP.lang.getLocalName()));
        if (getUriInfo().getQueryParameters().getFirst(GP.mode.getLocalName()) != null)
	    bld.parameter("{" + GP.mode.getNameSpace() + "}" + GP.mode.getLocalName(),
                URI.create(getUriInfo().getQueryParameters().getFirst(GP.mode.getLocalName())));
        if (getUriInfo().getQueryParameters().getFirst(GP.forClass.getLocalName()) != null)
	    bld.parameter("{" + GP.forClass.getNameSpace() + "}" + GP.forClass.getLocalName(),
                URI.create(getUriInfo().getQueryParameters().getFirst(GP.forClass.getLocalName())));
        if (getUriInfo().getQueryParameters().getFirst(GC.uri.getLocalName()) != null)
	    bld.parameter("{" + GC.uri.getNameSpace() + "}" + GC.uri.getLocalName(),
                URI.create(getUriInfo().getQueryParameters().getFirst(GC.uri.getLocalName())));
	if (getUriInfo().getQueryParameters().getFirst(GC.endpointUri.getLocalName()) != null)
	    bld.parameter("{" + GC.endpointUri.getNameSpace() + "}" + GC.endpointUri.getLocalName(),
                URI.create(getUriInfo().getQueryParameters().getFirst(GC.endpointUri.getLocalName())));

	return bld;
    }
    
}