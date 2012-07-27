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

import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
public class LocatorZipGRDDL implements Locator
{
    private static final Logger log = LoggerFactory.getLogger(LocatorZipGRDDL.class);
    
    private String zipFilenameOrURI = null;
    private ZipInputStream zis = null;
    private Source stylesheet = null;
    private XSLTBuilder builder = null;

    public LocatorZipGRDDL(String zipFilenameOrURI, Source stylesheet) throws TransformerConfigurationException, MalformedURLException, IOException
    {
	this.zipFilenameOrURI = zipFilenameOrURI;	
	zis = new ZipInputStream(new BufferedInputStream(new URL(zipFilenameOrURI).openStream()));
	this.stylesheet = stylesheet;
	builder = XSLTBuilder.fromStylesheet(stylesheet);
    }

    @Override
    public String getName()
    {
	return "LocatorZipGRDDL(" + zipFilenameOrURI + ")";
    }
    @Override
    public TypedStream open(String entryFilename)
    {
	log.debug("Opening Zip URI {} via GRDDL: {}", zipFilenameOrURI, stylesheet.getSystemId());
	
	ZipEntry entry;

	try
	{
	    while ((entry = zis.getNextEntry()) != null)
	    {
		log.trace("ZIP entry name: {} size: {}", entry.getName(), entry.getSize());

		if (entry.getName().equals("xl/workbook.xml"))
		{
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();

		    getXSLTBuilder().document(new StreamSource(zis)).
		    result(new StreamResult(bos)).
		    parameter("base-uri", UriBuilder.fromUri(zipFilenameOrURI).build()).
		    parameter("uri", UriBuilder.fromUri(entryFilename).build()).
		    transform();

		    log.trace("GRDDL RDF/XML output: {}", bos.toString());

		    return new TypedStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())),
			    WebContent.contentTypeRDFXML,
			    "UTF-8");
		}
	    }

	}
	catch (IOException ex)
	{
	    log.error("Could not read Zip entry", ex);
	}
	catch (TransformerException ex)
	{
	    log.error("Error in GRDDL XSLT transformation", ex);
	}

	return null;
    }

    protected XSLTBuilder getXSLTBuilder()
    {
	return builder;
    }

}
