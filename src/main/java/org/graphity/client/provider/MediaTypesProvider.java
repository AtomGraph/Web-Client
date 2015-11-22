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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
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

    private final MediaTypes mediaTypes;
    
    {
        List<MediaType> modelMediaTypes = new ArrayList<>(super.getMediaTypes().getModelMediaTypes());
        Map<String, String> utf8Param = new HashMap<>();
        utf8Param.put("charset", "UTF-8");
        
        MediaType xhtmlXml = new MediaType(MediaType.APPLICATION_XHTML_XML_TYPE.getType(), MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), utf8Param);
        modelMediaTypes.add(0, xhtmlXml);

        mediaTypes = new MediaTypes(Collections.unmodifiableList(modelMediaTypes), super.getMediaTypes().getResultSetMediaTypes());
    }
    
    @Override
    public MediaTypes getMediaTypes()
    {
        return mediaTypes;
    }
    
}