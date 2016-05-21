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

package org.graphity.client.vocabulary;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Graphity Client vocabulary.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public final class GC
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://graphity.org/gc#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final OntClass PageMode = m_model.createClass( NS + "PageMode" );
    
    public static final OntClass EditMode = m_model.createClass( NS + "EditMode" );

    public static final OntClass ListMode = m_model.createClass( NS + "ListMode" );
    
    public static final OntClass TableMode = m_model.createClass( NS + "TableMode" );

    public static final OntClass GridMode = m_model.createClass( NS + "GridMode" );
    
    public static final OntClass MapMode = m_model.createClass( NS + "MapMode" );

    public static final OntClass ReadMode = m_model.createClass( NS + "ReadMode" );
    
    public static final AnnotationProperty supportedMode = m_model.createAnnotationProperty( NS + "supportedMode" );

    public static final AnnotationProperty defaultMode = m_model.createAnnotationProperty( NS + "defaultMode" );
    
    public static final ObjectProperty mode = m_model.createObjectProperty( NS + "mode" );

    public static final ObjectProperty sitemap = m_model.createObjectProperty( NS + "sitemap" );
    
    public static final ObjectProperty stylesheet = m_model.createObjectProperty( NS + "stylesheet" );
    
    public static final ObjectProperty layoutOf = m_model.createObjectProperty( NS + "layoutOf" );

    //public static final ObjectProperty constructorOf = m_model.createObjectProperty( NS + "constructorOf" );
    
    public static final ObjectProperty forClass = m_model.createObjectProperty( NS + "forClass" );
    
    public static final ObjectProperty uri = m_model.createObjectProperty( NS + "uri" );

    public static final ObjectProperty endpointUri = m_model.createObjectProperty( NS + "endpointUri" );

    public static final ObjectProperty contextUri = m_model.createObjectProperty( NS + "contextUri" );

    public static final DatatypeProperty cacheStylesheet = m_model.createDatatypeProperty( NS + "cacheStylesheet" );
    
    public static final DatatypeProperty resolvingUncached = m_model.createDatatypeProperty( NS + "resolvingUncached" );    
    
    public static final DatatypeProperty accept = m_model.createDatatypeProperty( NS + "accept" );
    
}
