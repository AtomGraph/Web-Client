/*
 * Copyright 2025 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.client.util;

import java.util.function.Predicate;
import jakarta.ws.rs.core.MediaType;

/**
 * A predicate that determines whether a media type is considered HTML/XHTML.
 * This implementation returns true if the media type is compatible with either
 * <code>text/html</code> or <code>application/xhtml+xml</code>.
 * 
  * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class HTMLMediaTypePredicate implements Predicate<MediaType>
{

    @Override
    public boolean test(MediaType mediaType)
    {
        if (mediaType == null)
        {
            return false;
        }
        
        return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE) ||
               mediaType.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE);
    }
    
}
