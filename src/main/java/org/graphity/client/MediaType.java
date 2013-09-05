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
