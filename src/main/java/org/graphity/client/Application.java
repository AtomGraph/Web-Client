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
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.client.mapper.ClientErrorExceptionMapper;
import org.graphity.client.mapper.jersey.ClientHandlerExceptionMapper;
import org.graphity.client.mapper.jersey.UniformInterfaceExceptionMapper;
import org.graphity.client.model.impl.ProxyResourceBase;
import org.graphity.client.provider.DataManagerProvider;
import org.graphity.client.provider.MediaTypesProvider;
import org.graphity.client.provider.TemplatesProvider;
import org.graphity.client.writer.ModelXSLTWriter;
import org.graphity.core.provider.QueryParamProvider;
import org.graphity.core.provider.ResultSetProvider;
import org.graphity.core.provider.UpdateRequestReader;
import org.graphity.client.util.DataManager;
import org.graphity.client.vocabulary.GC;
import org.graphity.client.writer.xslt.JSONLDWriter;
import org.graphity.core.provider.ModelProvider;
import org.graphity.core.provider.SPARQLEndpointOriginProvider;
import org.graphity.core.provider.SPARQLEndpointProvider;
import org.graphity.core.riot.RDFLanguages;
import org.graphity.core.vocabulary.G;
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
public class Application extends org.graphity.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    private @Context UriInfo uriInfo;
    
    /**
     * Initializes root resource classes and provider singletons
     * @param servletConfig
     */
    public Application(@Context ServletConfig servletConfig)
    {
        super(servletConfig);
        
	classes.add(ProxyResourceBase.class);
	//classes.add(AdapterBase.class);

        /*
        singletons.add(new GraphStoreOriginProvider());
        */
	singletons.add(new ModelProvider());
        singletons.add(new SPARQLEndpointOriginProvider());
        singletons.add(new SPARQLEndpointProvider());
        
	singletons.add(new ResultSetProvider());
	singletons.add(new QueryParamProvider());
	singletons.add(new UpdateRequestReader());
        singletons.add(new MediaTypesProvider());
        //singletons.add(new ModifiersProvider());
        singletons.add(new DataManagerProvider());
        singletons.add(new org.graphity.core.provider.DataManagerProvider());
	singletons.add(new ClientErrorExceptionMapper());
	singletons.add(new UniformInterfaceExceptionMapper());
	singletons.add(new ClientHandlerExceptionMapper());
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
    public void init()
    {
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", getClasses(), getSingletons());
        
	// initialize mapping for locally stored vocabularies
	LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // check if file exists?
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        DataManager manager = new DataManager(mapper, ARQ.getContext(),
                getBooleanParam(getServletConfig(), G.cacheModelLoads),
                getBooleanParam(getServletConfig(), G.preemptiveAuth),
                getBooleanParam(getServletConfig(), GC.resolvingUncached));
        FileManager.setStdLocators(manager);
	manager.addLocatorLinkedData();
	manager.removeLocatorURL();
        FileManager.setGlobalFileManager(manager);
	if (log.isDebugEnabled()) log.debug("FileManager.get(): {}", FileManager.get());

        OntDocumentManager.getInstance().setFileManager(FileManager.get());
	if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());
        
	try
	{
	    singletons.add(new JSONLDWriter(new TemplatesProvider(getServletConfig()).getSource("/static/org/graphity/client/xsl/rdfxml2json-ld.xsl")));
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
     * Returns URI information
     * 
     * @return injected UriInfo
     */
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

}
