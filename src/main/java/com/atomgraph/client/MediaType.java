/*
 * Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.client;

import java.util.Map;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class MediaType extends com.atomgraph.core.MediaType
{
    
    /** "text/xsl" */
    public final static String TEXT_XSL = "text/xsl";
    /** "text/xsl" */
    public final static MediaType TEXT_XSL_TYPE = new MediaType("text","xsl");
    
    public MediaType(Lang lang)
    {
        super(lang);
    }

    public MediaType(Lang lang, Map<String, String> parameters)
    {
        super(lang, parameters);
    }
    
    public MediaType(ContentType ct)
    {
        super(ct);
    }

    public MediaType(ContentType ct, Map<String, String> parameters)
    {
        super(ct, parameters);
    }
    
    public MediaType(String type, String subtype, Map<String, String> parameters)
    {
        super(type, subtype, parameters);
    }

    public MediaType(String type, String subtype)
    {
        super(type, subtype);
    }

    public MediaType()
    {
        super();
    }
    
}
