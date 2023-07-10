/*
 * Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.client.util;

import com.atomgraph.client.exception.OntologyException;
import com.atomgraph.client.vocabulary.SP;
import com.atomgraph.client.vocabulary.SPIN;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ConstructorTest
{
    public static final String ONTOLOGY_URI = "http://test/ontology#";
    public static final String SUPER_PROPERTY_LOCAL_NAME = "super", RESOURCE_PROPERTY_LOCAL_NAME = "resource", LITERAL_PROPERTY_LOCAL_NAME = "literal";
    public static final String SUPER_RESOURCE_URI = "http://super", RESOURCE_URI = "http://resource";
    public static final Resource DATATYPE = XSD.xboolean;
    public static final String SUPER_CONSTRUCT = "PREFIX ont: <" + ONTOLOGY_URI + ">\n" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
"\n" +
"CONSTRUCT { ?this ont:" + SUPER_PROPERTY_LOCAL_NAME + " <" + SUPER_RESOURCE_URI + "> }\n" +
"WHERE {}";
    public static final String CONSTRUCT = "PREFIX ont: <" + ONTOLOGY_URI + ">\n" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
"\n" +
"CONSTRUCT { ?this ont:" + RESOURCE_PROPERTY_LOCAL_NAME + " <" + RESOURCE_URI + "> ; ont:" + LITERAL_PROPERTY_LOCAL_NAME + " [ a <" + DATATYPE.getURI() + "> ] }\n" +
"WHERE {}";
    
    private Ontology ontology;
    private OntClass forClass, noConstructorClass, invalidConstructorClass, invalidConstructClass;
    private Constructor constructor;
    
    @Before
    public void setUp()
    {
        ontology = ModelFactory.createOntologyModel().createOntology(ONTOLOGY_URI);
        
        OntClass superClass = ontology.getOntModel().createClass(ONTOLOGY_URI + "super-class");
        superClass.addProperty(SPIN.constructor, ontology.getOntModel().createResource().
                addProperty(SP.text, SUPER_CONSTRUCT));
        
        forClass = ontology.getOntModel().createClass(ONTOLOGY_URI + "class");
        forClass.addProperty(RDFS.subClassOf, superClass).
                addProperty(SPIN.constructor, ontology.getOntModel().createResource().
                        addLiteral(SP.text, CONSTRUCT));
        
        noConstructorClass = ontology.getOntModel().createClass(ONTOLOGY_URI + "no-constructor-class");
        
        invalidConstructorClass = ontology.getOntModel().createClass(ONTOLOGY_URI + "invalid-constructor-class");
        invalidConstructorClass.addLiteral(SPIN.constructor, 123);
        
        invalidConstructClass = ontology.getOntModel().createClass(ONTOLOGY_URI + "invalid-construct-class");
        invalidConstructClass.addProperty(SPIN.constructor, ontology.getOntModel().createResource().
                addProperty(SP.text, "INVALID { SPARQL } QUERY"));
        
        constructor = new Constructor();
    }

    /**
     * Test of construct method, of class Constructor.
     */
    @Test
    public void testConstruct()
    {
        Model result = ModelFactory.createDefaultModel();
        constructor.construct(forClass, result, "http://base/");
        
        Model expected = ModelFactory.createDefaultModel();
        expected.createResource().
                addProperty(RDF.type, forClass).
                addProperty(expected.createProperty(ONTOLOGY_URI, SUPER_PROPERTY_LOCAL_NAME),
                        expected.createResource(SUPER_RESOURCE_URI)).
                addProperty(expected.createProperty(ONTOLOGY_URI, RESOURCE_PROPERTY_LOCAL_NAME),
                        expected.createResource(RESOURCE_URI)).
                addProperty(expected.createProperty(ONTOLOGY_URI, LITERAL_PROPERTY_LOCAL_NAME),
                        expected.createResource().addProperty(RDF.type, DATATYPE));
        
        assertTrue(result.isIsomorphicWith(expected));
    }
    
    @Test
    public void testNoConstructorClass()
    {
        Model result = ModelFactory.createDefaultModel();
        constructor.construct(noConstructorClass, result, "http://base/");
        
        Model expected = ModelFactory.createDefaultModel();
        expected.createResource().addProperty(RDF.type, noConstructorClass);
        
        assertTrue(result.isIsomorphicWith(expected));
    }
    
    @Test(expected = OntologyException.class)
    public void testInvalidConstructorClass()
    {
        Model result = ModelFactory.createDefaultModel();
        constructor.construct(invalidConstructorClass, result, "http://base/");
    }
    
    @Test(expected = OntologyException.class)
    public void testInvalidConstructClass()
    {
        try
        {
            Model result = ModelFactory.createDefaultModel();
            constructor.construct(invalidConstructClass, result, "http://base/");
        }
        catch (OntologyException ex)
        {
            assertEquals(ex.getCause().getClass(), QueryParseException.class);
            throw ex;
        }
    }
    
}
