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
package org.graphity.client.util;

import java.io.IOException;
import java.util.zip.ZipFile;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of URIResolver that can resolve URIs to file entries within ZIP archives.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ZipURIResolver implements URIResolver
{
    private static final Logger log = LoggerFactory.getLogger(ZipURIResolver.class);
    
    private ZipFile zipFile = null;
    
    public ZipURIResolver(ZipFile zipFile)
    {
	this.zipFile = zipFile;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	if (log.isDebugEnabled()) log.debug("Resolving href: {} base: {}", href, base);

	try
	{
	    // set system ID?
	    return new StreamSource(zipFile.getInputStream(zipFile.getEntry(href)));
	}
	catch (IOException ex)
	{
	    if (log.isDebugEnabled()) log.debug("Error resolving from ZipFile", ex);
	}
	
	return null;
    }
    
}
