/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.vocabulary;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Graphity Processor vocabulary.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public final class GP
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://graphity.org/gp#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final OntClass SPARQLEndpoint = m_model.createClass( NS + "SPARQLEndpoint" );

    public static final OntClass GraphStore = m_model.createClass( NS + "GraphStore" );
    
    public static final OntClass Space = m_model.createClass( NS + "Space" );
    
    public static final OntClass Container = m_model.createClass( NS + "Container" );

    public static final OntClass Item = m_model.createClass( NS + "Item" );

    public static final OntClass Constructor = m_model.createClass( NS + "Constructor" );

    public static final OntClass ConstructMode = m_model.createClass( NS + "ConstructMode" );
    
    public static final OntClass Page = m_model.createClass( NS + "Page" );

    public static final AnnotationProperty query = m_model.createAnnotationProperty( NS + "query" );

    public static final AnnotationProperty update = m_model.createAnnotationProperty( NS + "update" );

    public static final AnnotationProperty template = m_model.createAnnotationProperty( NS + "template" );

    public static final AnnotationProperty defaultOffset = m_model.createAnnotationProperty( NS + "defaultOffset" );
    
    public static final AnnotationProperty defaultLimit = m_model.createAnnotationProperty( NS + "defaultLimit" );

    public static final AnnotationProperty defaultOrderBy = m_model.createAnnotationProperty( NS + "defaultOrderBy" );

    public static final AnnotationProperty defaultDesc = m_model.createAnnotationProperty( NS + "defaultDesc" );

    public static final AnnotationProperty uriTemplate = m_model.createAnnotationProperty( NS + "uriTemplate" );

    public static final AnnotationProperty skolemTemplate = m_model.createAnnotationProperty( NS + "skolemTemplate" );

    public static final AnnotationProperty loadClass = m_model.createAnnotationProperty( NS + "loadClass" );

    public static final AnnotationProperty cacheControl = m_model.createAnnotationProperty( NS + "cacheControl" );
    
    public static final AnnotationProperty lang = m_model.createAnnotationProperty( NS + "lang" );
        
    public static final ObjectProperty pageOf = m_model.createObjectProperty( NS + "pageOf" );
    
    public static final ObjectProperty baseUri = m_model.createObjectProperty( NS + "baseUri" );

    public static final ObjectProperty absolutePath = m_model.createObjectProperty( NS + "absolutePath" );

    public static final ObjectProperty requestUri = m_model.createObjectProperty( NS + "requestUri" );

    public static final DatatypeProperty httpHeaders = m_model.createDatatypeProperty( NS + "httpHeaders" );

    public static final ObjectProperty forClass = m_model.createObjectProperty( NS + "forClass" );

    public static final ObjectProperty datasetLocation = m_model.createObjectProperty( NS + "datasetLocation" );

    public static final DatatypeProperty sitemap = m_model.createDatatypeProperty( NS + "sitemap" );
    
    public static final DatatypeProperty offset = m_model.createDatatypeProperty( NS + "offset" );
    
    public static final DatatypeProperty limit = m_model.createDatatypeProperty( NS + "limit" );

    public static final DatatypeProperty orderBy = m_model.createDatatypeProperty( NS + "orderBy" );

    public static final DatatypeProperty desc = m_model.createDatatypeProperty( NS + "desc" );

    public static final DatatypeProperty slug = m_model.createDatatypeProperty( NS + "slug" );
    
    public static final ObjectProperty mode = m_model.createObjectProperty( NS + "mode" );
    
    public static final ObjectProperty constructorOf = m_model.createObjectProperty( NS + "constructorOf" );

}
