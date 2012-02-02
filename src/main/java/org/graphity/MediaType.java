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
