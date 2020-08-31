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
import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import com.atomgraph.client.locator.PrefixMapper;
import com.atomgraph.client.mapper.ClientErrorExceptionMapper;
import com.atomgraph.client.mapper.NotFoundExceptionMapper;
import com.atomgraph.client.mapper.RiotExceptionMapper;
import com.atomgraph.client.model.impl.ProxyResourceBase;
import com.atomgraph.client.writer.DatasetXSLTWriter;
import com.atomgraph.core.provider.QueryParamProvider;
import com.atomgraph.core.io.ResultSetProvider;
import com.atomgraph.core.io.UpdateRequestProvider;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.util.DataManagerImpl;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.writer.ModelXSLTWriter;
import com.atomgraph.client.writer.function.ConstructDocument;
import com.atomgraph.client.writer.function.UUID;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.vocabulary.A;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atomgraph.core.io.DatasetProvider;
import com.atomgraph.core.io.QueryProvider;
import com.atomgraph.core.riot.RDFLanguages;
import com.atomgraph.core.riot.lang.RDFPostReaderFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.glassfish.jersey.client.ClientConfig;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;

/**
 * AtomGraph Client JAX-RS application base class.
 * Can be extended or used as it is (needs to be registered in web.xml).
 * Needs to register JAX-RS root resource classes and providers.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html">JAX-RS Application</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
public class Application extends ResourceConfig
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final MediaTypes mediaTypes;
    private final Client client;
    private final DataManager dataManager;
    private final Source stylesheet;
    private final Boolean cacheStylesheet;
    private final OntModelSpec ontModelSpec;
    private final Processor xsltProc = new Processor(false);
    private final XsltExecutable xsltExec;


    /**
     * Initializes root resource classes and provider singletons
     * @param servletConfig
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    public Application(@Context ServletConfig servletConfig) throws URISyntaxException, IOException
    {
        this(new MediaTypes(), getClient(new ClientConfig()),
            servletConfig.getServletContext().getInitParameter(A.maxGetRequestSize.getURI()) != null ? Integer.parseInt(servletConfig.getServletContext().getInitParameter(A.maxGetRequestSize.getURI())) : null,
            servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI())) : false,
            getDataManager(new PrefixMapper(servletConfig.getServletContext().getInitParameter(AC.prefixMapping.getURI()) != null ? servletConfig.getServletContext().getInitParameter(AC.prefixMapping.getURI()) : null),
                com.atomgraph.client.Application.getClient(new ClientConfig()),
                new MediaTypes(),
                servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI())) : false,
                servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI())) : false),
            getSource(servletConfig.getServletContext(), servletConfig.getServletContext().getInitParameter(AC.stylesheet.getURI()) != null ? servletConfig.getServletContext().getInitParameter(AC.stylesheet.getURI()) : null),
            servletConfig.getServletContext().getInitParameter(AC.cacheStylesheet.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.cacheStylesheet.getURI())) : false,
            servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI())) : null
        );
    }
    
    public Application(final MediaTypes mediaTypes, final Client client, final Integer maxGetRequestSize, final boolean preemptiveAuth,
            final DataManager dataManager, final Source stylesheet, final boolean cacheStylesheet, final boolean resolvingUncached)
    {
        this.mediaTypes = mediaTypes;
        this.client = client;
        this.stylesheet = stylesheet;
        this.cacheStylesheet = cacheStylesheet;
        this.dataManager = dataManager;

        OntDocumentManager.getInstance().setFileManager((FileManager)dataManager);
        if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());
        
        OntModelSpec rdfsReasonerSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
        Resource reasonerConfig = ModelFactory.createDefaultModel().
            createResource().
            addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().
                create(reasonerConfig);
        //reasoner.setDerivationLogging(true);
        //reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);
        rdfsReasonerSpec.setReasoner(reasoner);
        this.ontModelSpec = rdfsReasonerSpec;
        
        // add RDF/POST serialization
        RDFLanguages.register(RDFLanguages.RDFPOST);
        RDFParserRegistry.registerLangTriples(RDFLanguages.RDFPOST, new RDFPostReaderFactory());
        // register plain RDF/XML writer as default
        RDFWriterRegistry.register(Lang.RDFXML, RDFFormat.RDFXML_PLAIN);
        
        xsltProc.registerExtensionFunction(new UUID());
        xsltProc.registerExtensionFunction(new ConstructDocument(xsltProc));

        try
        {
            XsltCompiler xsltComp = xsltProc.newXsltCompiler();
            xsltExec = xsltComp.compile(stylesheet);
        }
        catch (SaxonApiException ex)
        {
            if (log.isErrorEnabled()) log.error("System XSLT stylesheet error: {}", ex);
            throw new WebApplicationException(ex);
        }
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators, and Context
     * 
     * @see com.atomgraph.client.util.DataManager
     * @see com.atomgraph.client.locator.PrefixMapper
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/org/apache/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/org/apache/jena/util/LocationMapper.html">LocationMapper</a>
     */
    @PostConstruct
    public void init()
    {
        register(ProxyResourceBase.class);
        register(new HttpMethodOverrideFilter());
        
        register(new ModelProvider());
        register(new ResultSetProvider());
        register(new QueryParamProvider());
        register(new UpdateRequestProvider());
        register(NotFoundExceptionMapper.class);
        register(RiotExceptionMapper.class);
        register(ClientErrorExceptionMapper.class);
        register(new ModelXSLTWriter(getXsltExecutable(), getOntModelSpec())); // writes (X)HTML responses
        register(new DatasetXSLTWriter(getXsltExecutable(), getOntModelSpec())); // writes (X)HTML responses
        
        register(new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(getDataManager()).to(DataManager.class);
            }
        });
        register(new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(new MediaTypes()).to(MediaTypes.class);
            }
        });
        register(new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                bind(getClient()).to(Client.class);
            }
        });
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

    public static Client getClient(ClientConfig clientConfig)
    {
//        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.property(FOLLOW_REDIRECTS, Boolean.TRUE);

        clientConfig.register(new ModelProvider());
        clientConfig.register(new DatasetProvider());
        clientConfig.register(new ResultSetProvider());
        clientConfig.register(new QueryProvider());
        clientConfig.register(new UpdateRequestProvider()); // TO-DO: UpdateRequestProvider

        Client client = ClientBuilder.newClient(clientConfig);
        //if (log.isDebugEnabled()) client.addFilter(new LoggingFilter(System.out));
        
        return client;
    }

    public static DataManager getDataManager(final LocationMapper mapper, final Client client, final MediaTypes mediaTypes, final boolean preemptiveAuth, final boolean resolvingUncached)
    {
        return new DataManagerImpl(mapper, client, mediaTypes, preemptiveAuth, resolvingUncached);
    }

    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
    public Client getClient()
    {
        return client;
    }
    
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
    
    public OntModelSpec getOntModelSpec()
    {
        return ontModelSpec;
    }

    public XsltExecutable getXsltExecutable()
    {
        return xsltExec;
    }

}
