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
