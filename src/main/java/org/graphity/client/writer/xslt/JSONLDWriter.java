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