/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity;

import java.util.Map;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class MediaType extends javax.ws.rs.core.MediaType
{
    /** "application/rdf+xml" */
    public final static String APPLICATION_RDF_XML = "application/rdf+xml";
    /** "application/rdf+xml" */
    public final static MediaType APPLICATION_RDF_XML_TYPE = new MediaType("application","rdf+xml");

    /** "text/turtle" */
    public final static String TEXT_TURTLE = "text/turtle";
    /** "text/turtle" */
    public final static MediaType TEXT_TURTLE_TYPE = new MediaType("text","turtle");

    /** "application/sparql-results+xml" */
    public final static String APPLICATION_SPARQL_RESULTS_XML = "application/sparql-results+xml";
    /** "application/sparql-results+xml" */
    public final static MediaType APPLICATION_SPARQL_RESULTS_XML_TYPE = new MediaType("application","sparql-results+xml");

    /** "application/sparql-results+json" */
    public final static String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
    /** "application/sparql-results+json" */
    public final static MediaType APPLICATION_SPARQL_RESULTS_JSON_TYPE = new MediaType("application","sparql-results+json");
    
    public MediaType(String type, String subtype, Map<String, String> parameters)
    {
	super(type, subtype, parameters);
    }

    public MediaType(String type, String subtype)
    {
        super(type,subtype);
    }

    public MediaType()
    {
        super();
    }

}
