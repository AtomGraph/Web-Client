/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Graphity
{
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://graphity.org/ontology/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI()
    {
	return NS;
    }
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property query = m_model.createProperty( NS + "query" );
    
    public static final Property service = m_model.createProperty( NS + "service" );
    
    public static final Property mode = m_model.createProperty( NS + "mode" );
    
    public static final Property apiKey = m_model.createProperty( NS + "apiKey" );
    
    //public static final Property defaultQuery = m_model.createProperty( NS + "defaultQuery" );
    
    //public static final Resource ConstructItem = m_model.createResource( NS + "ConstructItem" );
    
    public static final Resource SelectServices = m_model.createResource( NS + "SelectServices" );
    
    public static final Resource SPOOptional = m_model.createResource( NS + "SPOOptional" );
    
    public static final Resource SubjectVar = m_model.createResource( NS + "SubjectVar" );
    
    public static final Resource PredicateVar = m_model.createResource( NS + "PredicateVar" );
    
    public static final Resource ObjectVar = m_model.createResource( NS + "ObjectVar" );
    
}
