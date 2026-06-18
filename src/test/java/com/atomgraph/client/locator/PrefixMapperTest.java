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
package com.atomgraph.client.locator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.LocationMappingVocab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests pinning the current {@link PrefixMapper} behavior so that
 * the migration to a {@code PrefixGraphRepository} (subclass of Jena's new
 * {@code DocumentGraphRepository}) can be proven to retain the same
 * longest-namespace-prefix matching semantics.
 *
 * These assertions describe the EXISTING (legacy) behavior and must continue to
 * hold — in equivalent form — after the migration.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class PrefixMapperTest
{

    private PrefixMapper mapper;

    @BeforeEach
    public void setUp()
    {
        mapper = new PrefixMapper();
    }

    /** The longest registered prefix that the URI starts with wins. */
    @Test
    public void testGetPrefixReturnsLongestMatch()
    {
        mapper.addAltPrefixEntry("http://example.org/", "file:short.ttl");
        mapper.addAltPrefixEntry("http://example.org/ns/", "file:long.ttl");

        assertEquals("http://example.org/ns/", mapper.getPrefix("http://example.org/ns/Foo"));
        // a URI covered only by the shorter prefix falls back to it
        assertEquals("http://example.org/", mapper.getPrefix("http://example.org/other"));
    }

    /** No registered prefix matches → null. */
    @Test
    public void testGetPrefixReturnsNullWhenNoMatch()
    {
        mapper.addAltPrefixEntry("http://example.org/ns/", "file:long.ttl");

        assertNull(mapper.getPrefix("http://other.example/thing"));
    }

    /** A URI under a registered namespace prefix resolves to that prefix's local file. */
    @Test
    public void testAltMappingResolvesByLongestPrefix()
    {
        mapper.addAltPrefixEntry("http://example.org/", "file:short.ttl");
        mapper.addAltPrefixEntry("http://example.org/ns/", "file:long.ttl");

        assertEquals("file:long.ttl", mapper.altMapping("http://example.org/ns/Foo", null));
        assertEquals("file:short.ttl", mapper.altMapping("http://example.org/other", null));
    }

    /** An exact (name) altEntry takes precedence over a prefix that also matches. */
    @Test
    public void testExactAltEntryWinsOverPrefix()
    {
        mapper.addAltPrefixEntry("http://example.org/ns/", "file:prefix.ttl");
        mapper.addAltEntry("http://example.org/ns/Exact", "file:exact.ttl");

        assertEquals("file:exact.ttl", mapper.altMapping("http://example.org/ns/Exact", null));
        // a sibling URI under the same namespace still resolves via the prefix
        assertEquals("file:prefix.ttl", mapper.altMapping("http://example.org/ns/Other", null));
    }

    /** When nothing matches, altMapping returns the supplied fallback unchanged. */
    @Test
    public void testAltMappingFallsBackToOtherwise()
    {
        mapper.addAltPrefixEntry("http://example.org/ns/", "file:long.ttl");

        assertEquals("http://nomatch.example/x", mapper.altMapping("http://nomatch.example/x", "http://nomatch.example/x"));
    }

    /** processConfig() reads an lm: location-mapping model into exact + prefix entries. */
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

        mapper.processConfig(config);

        assertEquals("file:exact.ttl", mapper.getAltEntry("http://example.org/exact#"));
        assertEquals("file:prefix.ttl", mapper.getPrefixAltEntry("http://example.org/prefix/"));
        // and the prefix entry is honored by longest-prefix resolution
        assertEquals("http://example.org/prefix/", mapper.getPrefix("http://example.org/prefix/Term"));
        assertEquals("file:prefix.ttl", mapper.altMapping("http://example.org/prefix/Term", null));
    }

}
