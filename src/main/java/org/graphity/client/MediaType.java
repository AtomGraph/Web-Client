/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client;

import java.util.Map;

/**
 * Extends standard JAX-RS media type with RDF media types
 * 
 * @see <a href="http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/core/MediaType.html">javax.ws.rs.core.MediaType</a>
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class MediaType extends org.graphity.server.MediaType
{

    /** "application/ld+json" */
    public final static String APPLICATION_LD_JSON = "application/ld+json";
    /** "application/ld+json" */
    public final static MediaType APPLICATION_LD_JSON_TYPE = new MediaType("application","ld+json");

    public MediaType(String type, String subtype, Map<String, String> parameters)
    {
	super(type, subtype, parameters);
    }

    public MediaType(String type, String subtype)
    {
	super(type, subtype);
    }

    public MediaType()
    {
    }
    
}
