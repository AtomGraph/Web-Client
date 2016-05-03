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

    public static final OntClass Application = m_model.createClass( NS + "Application" );

    public static final OntClass Template = m_model.createClass( NS + "Template" );
    
    public static final OntClass SPARQLEndpoint = m_model.createClass( NS + "SPARQLEndpoint" );

    public static final OntClass GraphStore = m_model.createClass( NS + "GraphStore" );
        
    public static final OntClass Container = m_model.createClass( NS + "Container" );

    public static final OntClass Document = m_model.createClass( NS + "Document" );

    public static final OntClass Page = m_model.createClass( NS + "Page" );

    public static final AnnotationProperty query = m_model.createAnnotationProperty( NS + "query" );

    public static final AnnotationProperty update = m_model.createAnnotationProperty( NS + "update" );
    
    public static final AnnotationProperty defaultOffset = m_model.createAnnotationProperty( NS + "defaultOffset" );
    
    public static final AnnotationProperty defaultLimit = m_model.createAnnotationProperty( NS + "defaultLimit" );

    public static final AnnotationProperty defaultOrderBy = m_model.createAnnotationProperty( NS + "defaultOrderBy" );

    public static final AnnotationProperty defaultDesc = m_model.createAnnotationProperty( NS + "defaultDesc" );

    public static final AnnotationProperty path = m_model.createAnnotationProperty( NS + "path" );

    public static final AnnotationProperty skolemTemplate = m_model.createAnnotationProperty( NS + "skolemTemplate" );

    public static final AnnotationProperty loadClass = m_model.createAnnotationProperty( NS + "loadClass" );

    public static final AnnotationProperty cacheControl = m_model.createAnnotationProperty( NS + "cacheControl" );
    
    public static final AnnotationProperty lang = m_model.createAnnotationProperty( NS + "lang" );
        
    public static final ObjectProperty pageOf = m_model.createObjectProperty( NS + "pageOf" );
    
    public static final ObjectProperty ontology = m_model.createObjectProperty( NS + "ontology" );

    public static final DatatypeProperty dataset = m_model.createDatatypeProperty( NS + "dataset" );
    
    public static final DatatypeProperty offset = m_model.createDatatypeProperty( NS + "offset" );
    
    public static final DatatypeProperty limit = m_model.createDatatypeProperty( NS + "limit" );

    public static final DatatypeProperty orderBy = m_model.createDatatypeProperty( NS + "orderBy" );

    public static final DatatypeProperty desc = m_model.createDatatypeProperty( NS + "desc" );

    public static final DatatypeProperty slug = m_model.createDatatypeProperty( NS + "slug" );

    public static final DatatypeProperty cacheSitemap = m_model.createDatatypeProperty( NS + "cacheSitemap" );

    public static final DatatypeProperty sitemapRules = m_model.createDatatypeProperty( NS + "sitemapRules" );

    public static final DatatypeProperty restrictionsQuery = m_model.createDatatypeProperty( NS + "restrictionsQuery" );

    public static final ObjectProperty viewOf = m_model.createObjectProperty( NS + "viewOf" );
    
}
