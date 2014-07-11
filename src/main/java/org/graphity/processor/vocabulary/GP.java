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
package org.graphity.processor.vocabulary;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class GP
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://processor.graphity.org/ontology#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final ObjectProperty base = m_model.createObjectProperty( NS + "base" );
    
    public static final ObjectProperty service = m_model.createObjectProperty( NS + "service" );

    public static final ObjectProperty graphStore = m_model.createObjectProperty( NS + "graphStore" );

    public static final DatatypeProperty ontologyPath = m_model.createDatatypeProperty( NS + "ontologyPath" );

    public static final ObjectProperty datasetLocation = m_model.createObjectProperty( NS + "datasetLocation" );

    public static final ObjectProperty datasetEndpoint = m_model.createObjectProperty( NS + "datasetEndpoint" );

    public static final DatatypeProperty ontologyQuery = m_model.createDatatypeProperty( NS + "ontologyQuery" );

    public static final DatatypeProperty applicationQuery = m_model.createDatatypeProperty( NS + "applicationQuery" );

    public static final DatatypeProperty uriTemplate = m_model.createDatatypeProperty( NS + "uriTemplate" );
    
    public static final DatatypeProperty cacheControl = m_model.createDatatypeProperty( NS + "cacheControl" );
    
    public static final DatatypeProperty offset = m_model.createDatatypeProperty( NS + "offset" );
    
    public static final DatatypeProperty limit = m_model.createDatatypeProperty( NS + "limit" );

    public static final DatatypeProperty orderBy = m_model.createDatatypeProperty( NS + "orderBy" );

    public static final DatatypeProperty desc = m_model.createDatatypeProperty( NS + "desc" );

}
