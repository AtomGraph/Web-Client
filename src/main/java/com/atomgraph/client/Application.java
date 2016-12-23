/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import com.atomgraph.client.locator.PrefixMapper;
import com.atomgraph.client.mapper.ClientErrorExceptionMapper;
import com.atomgraph.client.mapper.NotFoundExceptionMapper;
import com.atomgraph.client.mapper.jersey.ClientHandlerExceptionMapper;
import com.atomgraph.client.mapper.jersey.UniformInterfaceExceptionMapper;
import com.atomgraph.client.model.impl.ProxyResourceBase;
import com.atomgraph.client.provider.DataManagerProvider;
import com.atomgraph.client.provider.MediaTypesProvider;
import com.atomgraph.client.provider.TemplatesProvider;
import com.atomgraph.client.writer.ModelXSLTWriter;
import com.atomgraph.core.provider.QueryParamProvider;
import com.atomgraph.core.io.ResultSetProvider;
import com.atomgraph.core.io.UpdateRequestReader;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.core.provider.ClientProvider;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.mapper.AuthenticationExceptionMapper;
import com.atomgraph.core.vocabulary.A;
import org.apache.jena.rdf.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AtomGraph Client JAX-RS application base class.
 * Can be extended or used as it is (needs to be registered in web.xml).
 * Needs to register JAX-RS root resource classes and providers.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html">JAX-RS Application</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
public class Application extends com.atomgraph.core.Application
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
                
	// initialize mapping for locally stored vocabularies
	LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // check if file exists?
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        DataManager manager = new DataManager(mapper,
                new ClientProvider().getClient(),                
                new MediaTypesProvider().getMediaTypes(),
                getBooleanParam(servletConfig, A.preemptiveAuth),
                getBooleanParam(servletConfig, AC.resolvingUncached));
        FileManager.setStdLocators(manager);
        FileManager.setGlobalFileManager(manager);
	if (log.isDebugEnabled()) log.debug("FileManager.get(): {}", FileManager.get());

        OntDocumentManager.getInstance().setFileManager(FileManager.get());
	if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());

        // register plain RDF/XML writer as default
        RDFWriterRegistry.register(Lang.RDFXML, RDFFormat.RDFXML_PLAIN);
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators, and Context
     * 
     * @see com.atomgraph.client.util.DataManager
     * @see com.atomgraph.processor.locator
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html">LocationMapper</a>
     */
    @PostConstruct
    @Override
    public void init()
    {
	classes.add(ProxyResourceBase.class);

	singletons.add(new ModelProvider());
	singletons.add(new ResultSetProvider());
	singletons.add(new QueryParamProvider());
	singletons.add(new UpdateRequestReader());
        singletons.add(new MediaTypesProvider());
        singletons.add(new DataManagerProvider());
        singletons.add(new ClientProvider());
        singletons.add(new com.atomgraph.core.provider.DataManagerProvider(getServletConfig()));
	singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ClientErrorExceptionMapper());
	singletons.add(new UniformInterfaceExceptionMapper());
	singletons.add(new ClientHandlerExceptionMapper());
	singletons.add(new AuthenticationExceptionMapper());
        singletons.add(new ModelXSLTWriter()); // writes XHTML responses
	singletons.add(new TemplatesProvider(getServletConfig())); // loads XSLT stylesheet
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", classes, singletons);        
    }

    public final boolean getBooleanParam(ServletConfig servletConfig, Property property)
    {
	if (servletConfig == null) throw new IllegalArgumentException("ServletConfig cannot be null");
	if (property == null) throw new IllegalArgumentException("Property cannot be null");

        boolean value = false;
        if (servletConfig.getInitParameter(property.getURI()) != null)
            value = Boolean.parseBoolean(servletConfig.getInitParameter(property.getURI()));
        return value;
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
