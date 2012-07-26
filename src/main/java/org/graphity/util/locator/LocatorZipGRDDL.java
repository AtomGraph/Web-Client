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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
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

    public LocatorZipGRDDL(String zipFilenameOrURI, Source stylesheet) throws TransformerConfigurationException, MalformedURLException, IOException
    {
	this.zipFilenameOrURI = zipFilenameOrURI;	
	zis = new ZipInputStream(new BufferedInputStream(new URL(zipFilenameOrURI).openStream()));
    }

    @Override
    public String getName()
    {
	return "LocatorZipGRDDL(" + zipFilenameOrURI + ")";
    }
    @Override
    public TypedStream open(String filename)
    {
	ZipEntry entry;
	log.debug("Opening with {}", getName());

	try
	{
	    while ((entry = zis.getNextEntry()) != null)
	    {
		log.trace("ZIP entry name: {} size: {}", entry.getName(), entry.getSize());
	    }

	}
	catch (IOException ex)
	{
	    log.error("Could not read Zip entry", ex);
	}
	
	return null;
    }

}
