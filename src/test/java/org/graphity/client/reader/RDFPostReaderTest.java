/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client.reader;

import org.graphity.client.reader.RDFPostReader;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@RunWith(JUnit4.class)
public class RDFPostReaderTest
{
    public static String POST_BODY = "&rdf=&su=" + URLEncoder.encode("http://subject1") + "&pu=" + URLEncoder.encode("http://dc.org/#title") + "&ol=" + URLEncoder.encode("title") + "&ll=da" +
    "&su=" + URLEncoder.encode("http://subject1") + "&pu=" + URLEncoder.encode("http://predicate1") + "&ou=" + URLEncoder.encode("http://object1") +
						    "&pu=" + URLEncoder.encode("http://predicate2") + "&ou=" + URLEncoder.encode("http://object2") +
													"&ou=" + URLEncoder.encode("http://object3") +
    "&su=" + URLEncoder.encode("http://subject2") + "&pu=" + URLEncoder.encode("http://predicate3") + "&ol=" + URLEncoder.encode("literal1") +
    "&su=" + URLEncoder.encode("http://subject3") + "&pu=" + URLEncoder.encode("http://predicate4") + "&ol=" + URLEncoder.encode("literal2") + "&ll=da" +
    "&su=" + URLEncoder.encode("http://subject4") + "&pu=" + URLEncoder.encode("http://predicate5") + "&ol=" + URLEncoder.encode("literal3") + "&lt=" + URLEncoder.encode("http://type") +
						    "&pu=" + URLEncoder.encode("http://dct.org/#hasPart") + "&ob=" + URLEncoder.encode("b1") +
    "&sb=" + URLEncoder.encode("b1") + "&pu=" + URLEncoder.encode("http://rdf.org/#first") + "&ou=" + URLEncoder.encode("http://something/") +
					"&pu=" + URLEncoder.encode("http://rdf.org/#rest") + "&ou=" + URLEncoder.encode("http://rdf.org/#nil");
    
    //public static Model MODEL = ModelFactory.createDefaultModel().
    //	    add(ResourceFactory.createResource("http://subject1"), ResourceFactory.createProperty("http://dc.org/#title"),
    
    public RDFPostReaderTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of initKeysValues method, of class RDFPostReader.
     */
    @Test
    @Ignore
    public void testInitKeysValues()
    {
	System.out.println("initKeysValues");
	String charsetName = "";
	RDFPostReader instance = new RDFPostReader();
	instance.initKeysValues(POST_BODY, charsetName);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of isReadable method, of class RDFPostReader.
     */
    @Test
    @Ignore
    public void testIsReadable()
    {
	System.out.println("isReadable");
	Class<?> type = null;
	Type genericType = null;
	Annotation[] annotations = null;
	MediaType mediaType = null;
	RDFPostReader instance = new RDFPostReader();
	boolean expResult = false;
	boolean result = instance.isReadable(type, genericType, annotations, mediaType);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of readFrom method, of class RDFPostReader.
     */
    @Test
    @Ignore
    public void testReadFrom() throws Exception
    {
	System.out.println("readFrom");
	Class<Model> type = null;
	Type genericType = null;
	Annotation[] annotations = null;
	MediaType mediaType = null;
	MultivaluedMap<String, String> httpHeaders = null;
	InputStream entityStream = null;
	RDFPostReader instance = new RDFPostReader();
	Model expResult = null;
	Model result = instance.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class RDFPostReader.
     */
    @Test
    @Ignore
    public void testParse()
    {
	RDFPostReader instance = new RDFPostReader();
	Model expected = ModelFactory.createDefaultModel();
	expected.add(expected.createResource("http://subject1"), expected.createProperty("http://dc.org/#title"), expected.createLiteral("title", "da")).
		add(expected.createResource("http://subject1"), expected.createProperty("http://predicate1"), expected.createResource("http://object1")).
		add(expected.createResource("http://subject1"), expected.createProperty("http://predicate2"), expected.createResource("http://object2")).
		add(expected.createResource("http://subject1"), expected.createProperty("http://predicate2"), expected.createResource("http://object3")).
		add(expected.createResource("http://subject2"), expected.createProperty("http://predicate3"), expected.createLiteral("literal1")).
		add(expected.createResource("http://subject3"), expected.createProperty("http://predicate4"), expected.createLiteral("literal2", "da")).
		add(expected.createResource("http://subject4"), expected.createProperty("http://predicate5"), expected.createTypedLiteral("literal3", new BaseDatatype("http://type"))).
		add(expected.createResource("http://subject4"), expected.createProperty("http://dct.org/#hasPart"), expected.createResource(AnonId.create("b1"))).
		add(expected.createResource(AnonId.create("b1")), expected.createProperty("http://rdf.org/#first"), expected.createResource("http://something/")).
		add(expected.createResource(AnonId.create("b1")), expected.createProperty("http://rdf.org/#rest"), expected.createResource("http://rdf.org/#nil"));
	System.out.println("Expected Model");
	System.out.println(expected.listStatements().toList().toString());

	Model parsed = instance.parse(POST_BODY, "UTF-8");
	System.out.println("Parsed RDF/POST Model");
	System.out.println(parsed.listStatements().toList().toString());

	assertIsoModels(expected, parsed);
    }

    public static void assertIsoModels(Model wanted, Model got)
    {
	if (!wanted.isIsomorphicWith(got))
	    fail("Models not isomorphic (not structurally equal))");
    }
}
