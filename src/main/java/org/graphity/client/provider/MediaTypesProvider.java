/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graphity.client.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.graphity.client.vocabulary.GC;
import org.graphity.client.vocabulary.GP;
import org.graphity.core.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class MediaTypesProvider extends org.graphity.core.provider.MediaTypesProvider
{
    private static final Logger log = LoggerFactory.getLogger(MediaTypesProvider.class);

    @Context UriInfo uriInfo;
    
    @Override
    public MediaTypes getMediaTypes()
    {
        List<MediaType> modelMediaTypes = new ArrayList<>(super.getMediaTypes().getModelMediaTypes());
        Map<String, String> utf8Param = new HashMap<>();
        utf8Param.put("charset", "UTF-8");
        
        MediaType xhtmlXml = new MediaType(MediaType.APPLICATION_XHTML_XML_TYPE.getType(), MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), utf8Param);
        modelMediaTypes.add(0, xhtmlXml);

        // will not work when gc:MapMode is gc:defaultMode
        if (getMode() != null && getMode().equals(URI.create(GC.MapMode.getURI())))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Google Maps workaround", getMode());
            MediaType textHtml = new MediaType(MediaType.TEXT_HTML_TYPE.getType(), MediaType.TEXT_HTML_TYPE.getSubtype(), utf8Param);
            modelMediaTypes.add(0, textHtml);
	}

        return new MediaTypes(Collections.unmodifiableList(modelMediaTypes), super.getMediaTypes().getResultSetMediaTypes());
    }
    
    public UriInfo getUriInfo()
    {
        return uriInfo;
    }
    
    public URI getMode()
    {
        if (getUriInfo().getQueryParameters().getFirst(GP.mode.getURI()) != null)
            return URI.create(getUriInfo().getQueryParameters().getFirst(GP.mode.getURI()));
        
        return null;
    }
    
}
