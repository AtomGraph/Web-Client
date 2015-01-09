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
public class GP
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

    public static final OntClass Template = m_model.createClass( NS + "Template" );

    public static final OntClass SPARQLEndpoint = m_model.createClass( NS + "SPARQLEndpoint" );

    public static final OntClass GraphStore = m_model.createClass( NS + "GraphStore" );
    
    public static final ObjectProperty baseUri = m_model.createObjectProperty( NS + "baseUri" );

    public static final ObjectProperty absolutePath = m_model.createObjectProperty( NS + "absolutePath" );

    public static final ObjectProperty requestUri = m_model.createObjectProperty( NS + "requestUri" );

    public static final DatatypeProperty httpHeaders = m_model.createDatatypeProperty( NS + "httpHeaders" );

    public static final ObjectProperty matchedOntClass = m_model.createObjectProperty( NS + "matchedOntClass" );

    public static final ObjectProperty ontModel = m_model.createObjectProperty( NS + "ontModel" );

    public static final ObjectProperty datasetLocation = m_model.createObjectProperty( NS + "datasetLocation" );

    public static final DatatypeProperty ontology = m_model.createDatatypeProperty( NS + "ontology" );

    public static final DatatypeProperty uriTemplate = m_model.createDatatypeProperty( NS + "uriTemplate" );

    public static final DatatypeProperty skolemTemplate = m_model.createDatatypeProperty( NS + "skolemTemplate" );

    public static final DatatypeProperty cacheControl = m_model.createDatatypeProperty( NS + "cacheControl" );
    
    public static final DatatypeProperty offset = m_model.createDatatypeProperty( NS + "offset" );
    
    public static final DatatypeProperty limit = m_model.createDatatypeProperty( NS + "limit" );

    public static final DatatypeProperty orderBy = m_model.createDatatypeProperty( NS + "orderBy" );

    public static final DatatypeProperty desc = m_model.createDatatypeProperty( NS + "desc" );

    public static final DatatypeProperty slug = m_model.createDatatypeProperty( NS + "slug" );

}
