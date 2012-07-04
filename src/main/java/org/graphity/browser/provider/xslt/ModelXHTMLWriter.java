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
package org.graphity.browser.provider.xslt;

import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import org.graphity.provider.xslt.ModelXSLTWriter;
import org.graphity.util.XSLTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Singleton
@Produces({MediaType.APPLICATION_XHTML_XML})
public class ModelXHTMLWriter extends ModelXSLTWriter
{
    private static final Logger log = LoggerFactory.getLogger(ModelXHTMLWriter.class);
    
    @Context private UriInfo uriInfo;

    public ModelXHTMLWriter(Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	super(stylesheet, resolver);
    }

    @Override
    public XSLTBuilder getXSLTBuilder()
    {
	super.getXSLTBuilder().getTransformer().clearParameters(); // remove previously set param values
	    
	return super.getXSLTBuilder().
	    parameter("uri", uriInfo.getAbsolutePath()).
	    parameter("base-uri", uriInfo.getBaseUri()).
	    parameter("query", uriInfo.getQueryParameters().getFirst("query"));
    }

}
