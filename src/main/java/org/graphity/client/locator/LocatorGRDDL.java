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
package org.graphity.client.locator;

import com.hp.hpl.jena.util.TypedStream;
import com.sun.jersey.api.uri.UriTemplate;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.util.XSLTBuilder;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jena-compatible Locator that uses GRDDL (XSLT) stylesheet to load RDF data (possibly from a remote location)
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.client.util.DataManager
 */
public class LocatorGRDDL extends LocatorLinkedData
{
    private static final Logger log = LoggerFactory.getLogger(LocatorGRDDL.class);
    
    private Source stylesheet = null;
    private UriTemplate uriTemplate = null;
    private XSLTBuilder builder = null;

    public LocatorGRDDL(String uriTemplate, Source stylesheet) throws TransformerConfigurationException
    {
	this(new UriTemplate(uriTemplate), stylesheet);
    }
    
    public LocatorGRDDL(UriTemplate uriTemplate, Source stylesheet) throws TransformerConfigurationException
    {
	this.uriTemplate = uriTemplate;
	this.stylesheet = stylesheet;
	builder = XSLTBuilder.fromStylesheet(stylesheet);
    }

    @Override
    public TypedStream open(String filenameOrURI)
    {
	if (log.isDebugEnabled()) log.debug("Opening URI {} via GRDDL: {}", filenameOrURI, stylesheet.getSystemId());
	
	if (!getUriTemplate().match(filenameOrURI, new HashMap<String, String>()))
	{
	    if (log.isDebugEnabled()) log.debug("URI {} does not match UriTemplate {} of this GRDDL locator", filenameOrURI, getUriTemplate());
	    return null;	    
	}
	
	TypedStream ts = super.open(filenameOrURI);
	if (ts == null)
	{
	    if (log.isDebugEnabled()) log.debug("Could not open HTTP stream from URI: {}", filenameOrURI);
	    return null;
	}

	try
	{
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    getXSLTBuilder().document(new StreamSource(ts.getInput())).
	    result(new StreamResult(bos)).
	    parameter("uri", new URI(filenameOrURI)).
	    transform();
	    
	    if (log.isTraceEnabled()) log.trace("GRDDL RDF/XML output: {}", bos.toString());

	    return new TypedStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())),
		    WebContent.contentTypeRDFXML,
		    "UTF-8");
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error in GRDDL XSLT transformation", ex);
	}
	catch (URISyntaxException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error parsing location URI", ex);
	}

	return null;
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

    public UriTemplate getUriTemplate()
    {
	return uriTemplate;
    }

    protected XSLTBuilder getXSLTBuilder()
    {
	return builder;
    }

}
