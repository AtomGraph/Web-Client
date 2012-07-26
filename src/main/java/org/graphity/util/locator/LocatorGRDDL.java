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
package org.graphity.util.locator;

import com.hp.hpl.jena.util.TypedStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.util.XSLTBuilder;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LocatorGRDDL extends LocatorLinkedData
{
    private static final Logger log = LoggerFactory.getLogger(LocatorGRDDL.class);
    
    private Source stylesheet = null;
    private XSLTBuilder builder = null;
    
    public LocatorGRDDL(Source stylesheet) throws TransformerConfigurationException
    {
	this.stylesheet = stylesheet;
	builder = XSLTBuilder.fromStylesheet(stylesheet);
    }

    @Override
    public TypedStream open(String filenameOrURI)
    {
	log.debug("Opening URI {} via GRDDL: {}", filenameOrURI, stylesheet.getSystemId());

	TypedStream ts = super.open(filenameOrURI);
	if (ts == null) return null; // don't transform if there's no stream

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	try
	{
	    getXSLTBuilder().document(new StreamSource(ts.getInput())).
	    result(new StreamResult(bos)).
	    parameter("uri", UriBuilder.fromUri(filenameOrURI).build()).
	    transform();
	}
	catch (TransformerException ex)
	{
	    log.error("Error in transformation", ex);
	}

	log.trace("GRDDL RDF/XML output: {}", bos.toString());

	return new TypedStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())),
		WebContent.contentTypeRDFXML,
		"UTF-8");
    }

    @Override
    public  Map<String, Double> getQualifiedTypes()
    {
	Map<String, Double> xmlType = new HashMap<String, Double>();
	xmlType.put(WebContent.contentTypeXML, null);
	return xmlType;
    }
	
    @Override
    public String getName()
    {
	return "LocatorGRDDL(" + stylesheet.getSystemId() + ")";
    }
    
    protected XSLTBuilder getXSLTBuilder()
    {
	return builder;
    }
    
}
