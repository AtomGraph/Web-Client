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

import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Property;

import org.apache.jena.rdf.model.Resource;

/**
 * AtomGraph Client vocabulary.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public final class AC
{

    static
    {
        org.apache.jena.sys.JenaSystem.init(); // ensure Jena (RDFS vocab) is initialized before ontapi touches it
    }
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = OntModelFactory.createModel(OntSpecification.OWL1_FULL_MEM);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "https://w3id.org/atomgraph/client#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
        return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final Resource PageMode = m_model.createOntClass( NS + "PageMode" );
    
    public static final Resource EditMode = m_model.createOntClass( NS + "EditMode" );

    public static final Resource ListMode = m_model.createOntClass( NS + "ListMode" );
    
    public static final Resource TableMode = m_model.createOntClass( NS + "TableMode" );

    public static final Resource GridMode = m_model.createOntClass( NS + "GridMode" );
    
    public static final Resource MapMode = m_model.createOntClass( NS + "MapMode" );

    public static final Resource ReadMode = m_model.createOntClass( NS + "ReadMode" );
    
    public static final Resource ConstructMode = m_model.createOntClass( NS + "ConstructMode" );

    public static final Property contextUri = m_model.createObjectProperty( NS + "contextUri" );

    public static final Property limit = m_model.createDataProperty( NS + "limit" );

    public static final Property offset = m_model.createDataProperty( NS + "offset" );
    
    public static final Property order_by = m_model.createDataProperty( NS + "order-by" );
    
    public static final Property desc = m_model.createDataProperty( NS + "desc" );

    public static final Property mode = m_model.createObjectProperty( NS + "mode" );

    public static final Property method = m_model.createDataProperty( NS + "method" );

    public static final Property httpHeaders = m_model.createDataProperty( NS + "httpHeaders" );

    public static final Property uri = m_model.createObjectProperty( NS + "uri" );

    public static final Property endpoint = m_model.createObjectProperty( NS + "endpoint" );

    public static final Property query = m_model.createDataProperty( NS + "query" );
   
    public static final Property accept = m_model.createDataProperty( NS + "accept" );

    public static final Property forClass = m_model.createObjectProperty( NS + "forClass" );

    public static final Property instance = m_model.createDataProperty( NS + "instance" );

    // CONFIG - separate?
    
    public static final Property stylesheet = m_model.createObjectProperty( NS + "stylesheet" );
    
    public static final Property cacheStylesheet = m_model.createDataProperty( NS + "cacheStylesheet" );
    
    public static final Property resolvingUncached = m_model.createDataProperty( NS + "resolvingUncached" );
    
    public static final Property prefixMapping = m_model.createDataProperty( NS + "prefixMapping" );
    

}
