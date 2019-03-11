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

package com.atomgraph.client.vocabulary;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * AtomGraph Client vocabulary.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public final class AC
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://atomgraph.com/ns/client#";
    
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

    public static final ObjectProperty contextUri = m_model.createObjectProperty( NS + "contextUri" );

    public static final DatatypeProperty limit = m_model.createDatatypeProperty( NS + "limit" );

    public static final DatatypeProperty offset = m_model.createDatatypeProperty( NS + "offset" );
    
    public static final DatatypeProperty order_by = m_model.createDatatypeProperty( NS + "order-by" );
    
    public static final DatatypeProperty desc = m_model.createDatatypeProperty( NS + "desc" );

    public static final ObjectProperty mode = m_model.createObjectProperty( NS + "mode" );

    public static final ObjectProperty sitemap = m_model.createObjectProperty( NS + "sitemap" );

    public static final DatatypeProperty method = m_model.createDatatypeProperty( NS + "method" );

    public static final DatatypeProperty httpHeaders = m_model.createDatatypeProperty( NS + "httpHeaders" );

    public static final ObjectProperty uri = m_model.createObjectProperty( NS + "uri" );

    public static final ObjectProperty requestUri = m_model.createObjectProperty( NS + "requestUri" );

    public static final ObjectProperty endpoint = m_model.createObjectProperty( NS + "endpoint" );

    public static final DatatypeProperty query = m_model.createDatatypeProperty( NS + "query" );
   
    public static final DatatypeProperty accept = m_model.createDatatypeProperty( NS + "accept" );

    public static final ObjectProperty forClass = m_model.createObjectProperty( NS + "forClass" );

    public static final DatatypeProperty instance = m_model.createDatatypeProperty( NS + "instance" );

    // CONFIG
    
    public static final ObjectProperty stylesheet = m_model.createObjectProperty( NS + "stylesheet" );
    
    public static final DatatypeProperty cacheStylesheet = m_model.createDatatypeProperty( NS + "cacheStylesheet" );
    
    public static final DatatypeProperty resolvingUncached = m_model.createDatatypeProperty( NS + "resolvingUncached" );
    
    public static final DatatypeProperty prefixMapping = m_model.createDatatypeProperty( NS + "prefixMapping" );
    
    public static final DatatypeProperty sitemapRules = m_model.createDatatypeProperty( NS + "sitemapRules" );

}
