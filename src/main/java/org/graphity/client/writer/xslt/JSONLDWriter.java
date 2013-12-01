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
package org.graphity.client.writer.xslt;

import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.graphity.client.util.XSLTBuilder;
import org.graphity.client.writer.ModelXSLTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Writes Model as JSON-LD.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://json-ld.org">JSON-LD - JSON for Linking Data</a>
 */
@Provider
@Singleton
@Produces("application/ld+json")
public class JSONLDWriter extends ModelXSLTWriter
{
    private static final Logger log = LoggerFactory.getLogger(JSONLDWriter.class);
    
    public JSONLDWriter(XSLTBuilder builder)
    {
	super(builder);
	log.debug("Constructing JSONLDWriter with stylesheet: {} and URIResolver: ", builder.getHandler().getSystemId(), builder.getTransformer().getURIResolver());
    }

}