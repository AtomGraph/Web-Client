/**
 *  Copyright 2026 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.util.jena;

import com.atomgraph.core.client.GraphStoreClient;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.streammgr.LocatorClassLoader;
import org.apache.jena.riot.system.streammgr.LocatorFile;
import org.apache.jena.riot.system.streammgr.StreamManager;
import org.apache.jena.vocabulary.LocationMappingVocab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link org.apache.jena.ontapi.GraphRepository} that resolves graph IDs to local documents,
 * supporting both exact URI mappings and longest-namespace-prefix mappings.
 *
 * Replaces the legacy data-access stack: the {@code DataManagerImpl} loader (RDF retrieval +
 * caching), the {@code PrefixMapper} ({@code LocationMapper} subclass with prefix matching), and
 * the ontology {@code ModelGetter} used for {@code owl:imports} resolution. HTTP/HTTPS locations
 * are loaded via the {@link GraphStoreClient}; other locations (classpath, file) via a RIOT
 * {@link StreamManager}. Loaded graphs are cached by ID in the inherited repository store.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class PrefixGraphRepository extends DocumentGraphRepository
{

    private static final Logger log = LoggerFactory.getLogger(PrefixGraphRepository.class);

    private final Map<String, String> exactLocations = new HashMap<>();
    private final Map<String, String> prefixLocations = new HashMap<>();
    private final GraphStoreClient gsc;
    private final StreamManager streamManager;

    /**
     * Constructs the repository with the Graph Store client used for HTTP loading.
     *
     * @param gsc Graph Store client
     */
    public PrefixGraphRepository(GraphStoreClient gsc)
    {
        this.gsc = gsc;
        this.streamManager = new StreamManager(); // HTTP/HTTPS handled via GraphStoreClient, so only file + classpath locators needed
        streamManager.addLocator(new LocatorFile());
        streamManager.addLocator(new LocatorClassLoader(getClass().getClassLoader()));
    }

    /**
     * Maps an exact URI to a document location.
     *
     * @param uri graph URI
     * @param location document location (URL or classpath path)
     * @return this repository
     */
    public PrefixGraphRepository addLocationMapping(String uri, String location)
    {
        exactLocations.put(uri, location);
        return this;
    }

    /**
     * Maps a URI namespace prefix to a document location.
     *
     * @param prefix URI prefix
     * @param location document location (URL or classpath path)
     * @return this repository
     */
    public PrefixGraphRepository addPrefixMapping(String prefix, String location)
    {
        prefixLocations.put(prefix, location);
        return this;
    }

    /**
     * Loads exact ({@code lm:name}/{@code lm:altName}) and prefix ({@code lm:prefix}/{@code lm:altName})
     * mappings from a Jena location-mapping configuration model.
     *
     * @param config location-mapping model
     * @return this repository
     */
    public PrefixGraphRepository processConfig(Model config)
    {
        StmtIterator mappings = config.listStatements(null, LocationMappingVocab.mapping, (RDFNode)null);
        while (mappings.hasNext())
        {
            Statement stmt = mappings.nextStatement();
            Resource mapping = stmt.getResource();

            if (mapping.hasProperty(LocationMappingVocab.name) && mapping.hasProperty(LocationMappingVocab.altName))
                addLocationMapping(mapping.getRequiredProperty(LocationMappingVocab.name).getString(), mapping.getRequiredProperty(LocationMappingVocab.altName).getString());

            if (mapping.hasProperty(LocationMappingVocab.prefix) && mapping.hasProperty(LocationMappingVocab.altName))
                addPrefixMapping(mapping.getRequiredProperty(LocationMappingVocab.prefix).getString(), mapping.getRequiredProperty(LocationMappingVocab.altName).getString());
        }

        return this;
    }

    /**
     * Resolves a graph ID to a document location: an exact mapping wins, otherwise the longest
     * matching namespace prefix; if neither matches, the ID is its own location.
     *
     * @param id graph ID
     * @return document location
     */
    public String resolve(String id)
    {
        if (exactLocations.containsKey(id)) return exactLocations.get(id);

        String prefix = null;
        for (Iterator<String> it = prefixLocations.keySet().iterator(); it.hasNext();)
        {
            String candidate = it.next();
            if (id.startsWith(candidate) && (prefix == null || candidate.length() > prefix.length())) prefix = candidate;
        }
        if (prefix != null) return prefixLocations.get(prefix);

        return id;
    }

    /**
     * Returns the exact URI→location mappings (live view).
     *
     * @return exact mappings
     */
    public Map<String, String> getLocationMappings()
    {
        return exactLocations;
    }

    /**
     * Returns the namespace-prefix→location mappings (live view).
     *
     * @return prefix mappings
     */
    public Map<String, String> getPrefixMappings()
    {
        return prefixLocations;
    }

    /**
     * Returns true if the ID has a (non-HTTP) document mapping — e.g. a bundled local ontology.
     *
     * @param id graph ID
     * @return true if mapped to a non-HTTP location
     */
    public boolean isMapped(String id)
    {
        String location = resolve(id);
        return !location.equals(id) && !location.startsWith("http://") && !location.startsWith("https://");
    }

    /**
     * Returns true if a graph for the ID is already loaded into the store.
     *
     * @param id graph ID
     * @return true if cached
     */
    public boolean isCached(String id)
    {
        return getIds().contains(id);
    }

    @Override
    public Graph get(String id)
    {
        if (getIds().contains(id)) return super.get(id); // already loaded into the store

        String location = resolve(id);
        if (log.isDebugEnabled()) log.debug("Loading graph '{}' from location '{}'", id, location);
        Graph graph = load(id, location);
        put(id, graph);
        return graph;
    }

    /**
     * Loads a graph from a document location. HTTP/HTTPS via the Graph Store client; everything
     * else (classpath, file) via the RIOT stream manager. The graph id is used as the parser base so that
     * relative URIs in the document (e.g. {@code rdf:about=""} for the ontology resource) resolve against the
     * graph URI rather than the (classpath/file) location.
     *
     * @param base parser base URI (the graph id)
     * @param location document location
     * @return loaded graph
     */
    protected Graph load(String base, String location)
    {
        if (location.startsWith("http://") || location.startsWith("https://"))
            return getGraphStoreClient().getModel(location).getGraph();

        Model model = ModelFactory.createDefaultModel();
        RDFParser.create().source(location).base(base).streamManager(getStreamManager()).build().parse(model);
        return model.getGraph();
    }

    /**
     * Returns the Graph Store client used for HTTP loading.
     *
     * @return Graph Store client
     */
    public GraphStoreClient getGraphStoreClient()
    {
        return gsc;
    }

    /**
     * Returns the stream manager used for classpath/file loading.
     *
     * @return stream manager
     */
    public StreamManager getStreamManager()
    {
        return streamManager;
    }

}
