/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.client.model.impl.GlobalResourceBase;
import org.graphity.client.provider.DataManagerProvider;
import org.graphity.client.provider.TemplatesProvider;
import org.graphity.client.writer.ModelXSLTWriter;
import org.graphity.processor.provider.DatasetProvider;
import org.graphity.core.provider.QueryParamProvider;
import org.graphity.core.provider.ResultSetWriter;
import org.graphity.core.provider.UpdateRequestReader;
import org.graphity.client.util.DataManager;
import org.graphity.client.writer.xslt.JSONLDWriter;
import org.graphity.core.riot.RDFLanguages;
import org.graphity.processor.mapper.ConstraintViolationExceptionMapper;
import org.graphity.processor.mapper.NotFoundExceptionMapper;
import org.graphity.processor.provider.GraphStoreOriginProvider;
import org.graphity.processor.provider.GraphStoreProvider;
import org.graphity.processor.provider.OntClassMatcher;
import org.graphity.processor.provider.OntologyProvider;
import org.graphity.processor.provider.SPARQLEndpointOriginProvider;
import org.graphity.processor.provider.SPARQLEndpointProvider;
import org.graphity.core.vocabulary.G;
import org.graphity.processor.provider.ConstraintViolationExceptionProvider;
import org.graphity.processor.provider.SkolemizingModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graphity Client JAX-RS application base class.
 * Can be extended or used as it is (needs to be registered in web.xml).
 * Needs to register JAX-RS root resource classes and providers.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html">JAX-RS Application</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
public class ApplicationBase extends org.graphity.processor.ApplicationBase
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationBase.class);

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    private @Context UriInfo uriInfo;
    
    /**
     * Initializes root resource classes and provider singletons
     * @param servletConfig
     */
    public ApplicationBase(@Context ServletConfig servletConfig)
    {
        super(servletConfig);
        
	classes.add(GlobalResourceBase.class); // handles /

	singletons.add(new SkolemizingModelProvider());
	singletons.add(new ResultSetWriter());
	singletons.add(new QueryParamProvider());
	singletons.add(new UpdateRequestReader());
        
        singletons.add(new DataManagerProvider());
        singletons.add(new org.graphity.core.provider.DataManagerProvider());
        singletons.add(new DatasetProvider());
        singletons.add(new OntologyProvider());
        singletons.add(new OntClassMatcher());
	singletons.add(new SPARQLEndpointProvider());
	singletons.add(new SPARQLEndpointOriginProvider());
        singletons.add(new GraphStoreProvider());
        singletons.add(new GraphStoreOriginProvider());
        singletons.add(new ConstraintViolationExceptionProvider());        
	singletons.add(new ConstraintViolationExceptionMapper());
	singletons.add(new NotFoundExceptionMapper());
        singletons.add(new org.graphity.client.mapper.jena.DoesNotExistExceptionMapper());
	singletons.add(new org.graphity.client.mapper.jena.NotFoundExceptionMapper());
	singletons.add(new org.graphity.processor.mapper.jena.QueryExceptionHTTPMapper());
	singletons.add(new org.graphity.processor.mapper.jena.QueryParseExceptionMapper());
	singletons.add(new org.graphity.processor.mapper.jena.HttpExceptionMapper());
        singletons.add(new ModelXSLTWriter()); // writes XHTML responses
	singletons.add(new TemplatesProvider(servletConfig)); // loads XSLT stylesheet
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators, and Context
     * 
     * @see org.graphity.client.util.DataManager
     * @see org.graphity.processor.locator
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html">LocationMapper</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html">Locator</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/sparql/util/Context.html">Context</a>
     */
    @PostConstruct
    @Override
    public void init()
    {
        super.init(); // Graphity Processor initialization
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", getClasses(), getSingletons());
        
	// initialize mapping for locally stored vocabularies
	LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // check if file exists?
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        DataManager manager = new DataManager(mapper, ARQ.getContext(), getPreemptiveAuth(getServletConfig(), G.preemptiveAuth), getUriInfo());
        FileManager.setStdLocators(manager);
	manager.addLocatorLinkedData();
	manager.removeLocatorURL();
        FileManager.setGlobalFileManager(manager);
	if (log.isDebugEnabled()) log.debug("FileManager.get(): {}", FileManager.get());

        OntDocumentManager.getInstance().setFileManager(FileManager.get());
	if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());
        
	try
	{
	    singletons.add(new JSONLDWriter(getSource("/static/org/graphity/client/xsl/rdfxml2json-ld.xsl")));
            // add RDF/POST serialization. It will support JSON-LD during conneg, even though it's a JAX-RS and not Jena writer
            RDFLanguages.register(RDFLanguages.JSONLD) ;            
	}
	catch (TransformerConfigurationException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformer config error", ex);
	}
	catch (FileNotFoundException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not found", ex);
	}
	catch (URISyntaxException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URI error", ex);
	}
	catch (MalformedURLException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URL error", ex);
	}
    }
    
    /**
     * Provides JAX-RS root resource classes.
     *
     * @return set of root resource classes
     * @see <a
     * href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getClasses()">Application.getClasses()</a>
     */
    @Override
    public Set<Class<?>> getClasses()
    {
	return classes;
    }

    /**
     * Provides JAX-RS singleton objects (e.g. resources or Providers)
     * 
     * @return set of singleton objects
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getSingletons()">Application.getSingletons()</a>
     */
    @Override
    public Set<Object> getSingletons()
    {
	return singletons;
    }
    
    /**
     * Provides XML source from filename
     * 
     * @param filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @throws java.net.MalformedURLException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public Source getSource(String filename) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	URL xsltUrl = getServletConfig().getServletContext().getResource(filename);
	if (xsltUrl == null) throw new FileNotFoundException("File '" + filename + "' not found");
	String xsltUri = xsltUrl.toURI().toString();
	if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
	return new StreamSource(xsltUri);
    }
    
    /**
     * Returns URI information
     * 
     * @return injected UriInfo
     */
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

}
