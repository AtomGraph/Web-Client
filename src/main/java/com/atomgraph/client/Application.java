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
import com.atomgraph.core.provider.MediaTypesProvider;
import com.atomgraph.core.vocabulary.A;
import com.atomgraph.core.vocabulary.SD;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.atomgraph.core.Application.getClient;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

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

    private final DataManager dataManager;
    private final Source stylesheet;
    private final Boolean cacheStylesheet;
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    /**
     * Initializes root resource classes and provider singletons
     * @param servletConfig
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    public Application(@Context ServletConfig servletConfig) throws URISyntaxException, IOException
    {
        this(servletConfig.getInitParameter(A.dataset.getURI()) != null ? getDataset(servletConfig.getInitParameter(A.dataset.getURI()), null) : null,
            servletConfig.getInitParameter(SD.endpoint.getURI()) != null ? servletConfig.getInitParameter(SD.endpoint.getURI()) : null,
            servletConfig.getInitParameter(A.graphStore.getURI()) != null ? servletConfig.getInitParameter(A.graphStore.getURI()) : null,
            servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) != null ? servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) : null,
            servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) != null ? servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) : null,
            new MediaTypes(), getClient(new DefaultClientConfig()),
            servletConfig.getInitParameter(A.maxGetRequestSize.getURI()) != null ? Integer.parseInt(servletConfig.getInitParameter(A.maxGetRequestSize.getURI())) : null,            
            servletConfig.getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(A.preemptiveAuth.getURI())) : false,
            getDataManager(new PrefixMapper(servletConfig.getInitParameter(AC.prefixMapping.getURI()) != null ? servletConfig.getInitParameter(AC.prefixMapping.getURI()) : null),
                com.atomgraph.client.Application.getClient(new DefaultClientConfig()),
                new MediaTypes(),
                servletConfig.getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(A.preemptiveAuth.getURI())) : false,
                servletConfig.getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(AC.resolvingUncached.getURI())) : false),
            getSource(servletConfig.getServletContext(), servletConfig.getInitParameter(AC.stylesheet.getURI()) != null ? servletConfig.getInitParameter(AC.stylesheet.getURI()) : null),
            servletConfig.getInitParameter(AC.cacheStylesheet.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(AC.cacheStylesheet.getURI())) : false,
            servletConfig.getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(AC.resolvingUncached.getURI())) : null
        );
    }
    
    public Application(final Dataset dataset, final String endpointURI, final String graphStoreURI, final String authUser, final String authPwd,
            final MediaTypes mediaTypes, final Client client, final Integer maxGetRequestSize, final boolean preemptiveAuth,
            final DataManager dataManager, final Source stylesheet, final boolean cacheStylesheet, final boolean resolvingUncached)
    {
        super(dataset, endpointURI, graphStoreURI, authUser, authPwd, 
                mediaTypes, client, maxGetRequestSize, preemptiveAuth);
        this.stylesheet = stylesheet;
        this.cacheStylesheet = cacheStylesheet;
        this.dataManager = dataManager;
        
        // initialize mapping for locally stored vocabularies
        //LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // check if file exists?
        //LocationMapper.setGlobalLocationMapper(mapper);
        //if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        //DataManager manager = new DataManager(mapper, client, mediaTypes, preemptiveAuth, resolvingUncached);
        FileManager.setStdLocators(dataManager);
        FileManager.setGlobalFileManager(dataManager);
        if (log.isDebugEnabled()) log.debug("FileManager.get(): {} LocationMapper.get(): {}", FileManager.get(), LocationMapper.get());

        OntDocumentManager.getInstance().setFileManager(dataManager);
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
        singletons.add(new MediaTypesProvider(getMediaTypes()));
        singletons.add(new DataManagerProvider(getDataManager()));
        singletons.add(new ClientProvider(getClient()));
        singletons.add(new com.atomgraph.core.provider.DataManagerProvider(getDataManager()));
        singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ClientErrorExceptionMapper());
        singletons.add(new UniformInterfaceExceptionMapper());
        singletons.add(new ClientHandlerExceptionMapper());
        singletons.add(new AuthenticationExceptionMapper());
        singletons.add(new ModelXSLTWriter()); // writes XHTML responses
        singletons.add(new TemplatesProvider(((SAXTransformerFactory)TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)),
                getDataManager(), getStylesheet(), isCacheStylesheet())); // loads XSLT stylesheet
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", classes, singletons);        
    }
    
    @Override
    public DataManager getDataManager()
    {
        return dataManager;
    }
    
    public Source getStylesheet()
    {
        return stylesheet;
    }
    
    public Boolean isCacheStylesheet()
    {
        return cacheStylesheet;
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

    public static DataManager getDataManager(final LocationMapper mapper, final Client client, final MediaTypes mediaTypes, final boolean preemptiveAuth, final boolean resolvingUncached)
    {
        return new DataManager(mapper, client, mediaTypes, preemptiveAuth, resolvingUncached);        
    }
        
    /**
     * Provides XML source from filename
     * 
     * @param servletContext
     * @param path stylesheet filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @throws java.net.MalformedURLException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public static Source getSource(ServletContext servletContext, String path) throws URISyntaxException, IOException
    {
        if (servletContext == null) throw new IllegalArgumentException("servletContext name cannot be null");        
        if (path == null) throw new IllegalArgumentException("XML file name cannot be null");        

        if (log.isDebugEnabled()) log.debug("Resource paths used to load Source: {} from filename: {}", servletContext.getResourcePaths("/"), path);
        URL xsltUrl = servletContext.getResource(path);
        if (xsltUrl == null) throw new FileNotFoundException("File '" + path + "' not found");
        String xsltUri = xsltUrl.toURI().toString();
        if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
        return new StreamSource(xsltUri);
    }
    
}
