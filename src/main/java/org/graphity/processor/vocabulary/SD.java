/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.processor.vocabulary;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Martynas
 */
public class SD
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/sparql-service-description#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final OntClass Dataset = m_model.createClass( NS + "Dataset" );

    public static final OntClass Service = m_model.createClass( NS + "Service" );

    public static final OntClass Graph = m_model.createClass( NS + "Graph" );
    
    public static final OntClass NamedGraph = m_model.createClass( NS + "NamedGraph" );
    
    public static final ObjectProperty endpoint = m_model.createObjectProperty( NS + "endpoint" );

    public static final ObjectProperty graph = m_model.createObjectProperty( NS + "graph" );

    public static final ObjectProperty name = m_model.createObjectProperty( NS + "name" );

    public static final ObjectProperty defaultGraph = m_model.createObjectProperty( NS + "defaultGraph" );

    public static final ObjectProperty namedGraph = m_model.createObjectProperty( NS + "namedGraph" );

}
