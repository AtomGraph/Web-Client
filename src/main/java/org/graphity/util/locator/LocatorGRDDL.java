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

package org.graphity.util.locator;

import com.hp.hpl.jena.util.TypedStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

	try
	{
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    getXSLTBuilder().document(new StreamSource(ts.getInput())).
	    result(new StreamResult(bos)).
	    parameter("uri", UriBuilder.fromUri(filenameOrURI).build()).
	    transform();
	    
	    log.trace("GRDDL RDF/XML output: {}", bos.toString());

	    return new TypedStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())),
		    WebContent.contentTypeRDFXML,
		    "UTF-8");
	}
	catch (TransformerException ex)
	{
	    log.error("Error in GRDDL XSLT transformation", ex);
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
    
    protected XSLTBuilder getXSLTBuilder()
    {
	return builder;
    }
    
}
