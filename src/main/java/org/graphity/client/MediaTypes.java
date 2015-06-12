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

package org.graphity.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class MediaTypes extends org.graphity.core.MediaTypes
{

    @Override
    public List<MediaType> getModelMediaTypes()
    {
        List<MediaType> list = super.getModelMediaTypes();
        Map<String, String> utf8Param = new HashMap<>();
        utf8Param.put("charset", "UTF-8");

        MediaType xhtmlXml = new MediaType(MediaType.APPLICATION_XHTML_XML_TYPE.getType(), MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), utf8Param);
        list.add(0, xhtmlXml);
        
        /*
        if (getMode() != null && getMode().equals(URI.create(GC.MapMode.getURI())))
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Google Maps workaround", getMode());
            MediaType textHtml = new MediaType(MediaType.TEXT_HTML_TYPE.getType(), MediaType.TEXT_HTML_TYPE.getSubtype(), utf8Param);
            list.add(0, textHtml);
	}
        */

        return list;
    }
    
}
