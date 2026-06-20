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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.LocationMappingVocab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the URI→location resolution of {@link PrefixGraphRepository}, the replacement for the
 * legacy {@code PrefixMapper}. Carries over the same longest-namespace-prefix semantics so the
 * migration can be shown to retain behavior.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class PrefixGraphRepositoryTest
{

    private PrefixGraphRepository repo;

    @BeforeEach
    public void setUp()
    {
        repo = new PrefixGraphRepository(null); // GraphStoreClient only needed for HTTP loads, not resolution
    }

    /** The longest registered prefix that the URI starts with wins. */
    @Test
    public void testResolveReturnsLongestPrefixMatch()
    {
        repo.addPrefixMapping("http://example.org/", "file:short.ttl");
        repo.addPrefixMapping("http://example.org/ns/", "file:long.ttl");

        assertEquals("file:long.ttl", repo.resolve("http://example.org/ns/Foo"));
        assertEquals("file:short.ttl", repo.resolve("http://example.org/other"));
    }

    /** No registered mapping → the ID is its own location. */
    @Test
    public void testResolveFallsBackToIdWhenNoMapping()
    {
        repo.addPrefixMapping("http://example.org/ns/", "file:long.ttl");

        assertEquals("http://other.example/thing", repo.resolve("http://other.example/thing"));
    }

    /** An exact location mapping wins over a prefix that also matches. */
    @Test
    public void testExactMappingWinsOverPrefix()
    {
        repo.addPrefixMapping("http://example.org/ns/", "file:prefix.ttl");
        repo.addLocationMapping("http://example.org/ns/Exact", "file:exact.ttl");

        assertEquals("file:exact.ttl", repo.resolve("http://example.org/ns/Exact"));
        assertEquals("file:prefix.ttl", repo.resolve("http://example.org/ns/Other"));
    }

    /** processConfig() reads an lm: location-mapping model into exact + prefix mappings. */
    @Test
    public void testProcessConfigLoadsExactAndPrefixMappings()
    {
        Model config = ModelFactory.createDefaultModel();
        Resource root = config.createResource();

        Resource exact = config.createResource();
        config.add(root, LocationMappingVocab.mapping, exact);
        config.add(exact, LocationMappingVocab.name, "http://example.org/exact#");
        config.add(exact, LocationMappingVocab.altName, "file:exact.ttl");

        Resource prefix = config.createResource();
        config.add(root, LocationMappingVocab.mapping, prefix);
        config.add(prefix, LocationMappingVocab.prefix, "http://example.org/prefix/");
        config.add(prefix, LocationMappingVocab.altName, "file:prefix.ttl");

        repo.processConfig(config);

        assertEquals("file:exact.ttl", repo.resolve("http://example.org/exact#"));
        assertEquals("file:prefix.ttl", repo.resolve("http://example.org/prefix/Term"));
    }

}
