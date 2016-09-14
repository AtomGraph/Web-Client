/**
 *  Copyright 2014 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.vocabulary;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Linked Data Templates vocabulary.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public final class LDT
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/ldt#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final OntClass Application = m_model.createClass( NS + "Application" );

    public static final OntClass Template = m_model.createClass( NS + "Template" );

    public static final OntClass TemplateCall = m_model.createClass( NS + "TemplateCall" );

    public static final OntClass Argument = m_model.createClass( NS + "Argument" );

    public static final ObjectProperty baseUri = m_model.createObjectProperty( NS + "baseUri" );

    public static final ObjectProperty ontology = m_model.createObjectProperty( NS + "ontology" );

    public static final AnnotationProperty template = m_model.createAnnotationProperty( NS + "template" );
    
    public static final AnnotationProperty query = m_model.createAnnotationProperty( NS + "query" );

    public static final AnnotationProperty update = m_model.createAnnotationProperty( NS + "update" );

    public static final AnnotationProperty path = m_model.createAnnotationProperty( NS + "path" );

    public static final AnnotationProperty priority = m_model.createAnnotationProperty( NS + "priority" );

    public static final AnnotationProperty tunnel = m_model.createAnnotationProperty( NS + "tunnel" );

    public static final AnnotationProperty skolemTemplate = m_model.createAnnotationProperty( NS + "skolemTemplate" );

    public static final AnnotationProperty fragmentTemplate = m_model.createAnnotationProperty( NS + "fragmentTemplate" );

    public static final AnnotationProperty param = m_model.createAnnotationProperty( NS + "param" );
    
    public static final AnnotationProperty loadClass = m_model.createAnnotationProperty( NS + "loadClass" );

    public static final AnnotationProperty cacheControl = m_model.createAnnotationProperty( NS + "cacheControl" );
    
    public static final AnnotationProperty lang = m_model.createAnnotationProperty( NS + "lang" );
    
}
