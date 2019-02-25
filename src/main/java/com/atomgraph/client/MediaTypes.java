/*
 * Copyright 2017 Martynas Jusevičius <martynas@atomgraph.com>.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class MediaTypes extends com.atomgraph.core.MediaTypes
{

    public static final Map<Class, List<MediaType>> READABLE;
    public static final Map<Class, List<MediaType>> WRITABLE;
    
    static
    {
        com.atomgraph.core.MediaTypes coreTypes = new com.atomgraph.core.MediaTypes();
        READABLE = coreTypes.getReadable();
        
        // add XHTML as writable MediaType
        List<MediaType> writableDatasetTypes = new ArrayList<>(coreTypes.getWritable(Dataset.class));
        MediaType xhtml = new MediaType(MediaType.APPLICATION_XHTML_XML_TYPE.getType(), MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), com.atomgraph.core.MediaTypes.UTF8_PARAM);
        writableDatasetTypes.add(0, xhtml);
        // add HTML as writable MediaType
        MediaType html = new MediaType(MediaType.TEXT_HTML_TYPE.getType(), MediaType.TEXT_HTML_TYPE.getSubtype(), com.atomgraph.core.MediaTypes.UTF8_PARAM);
        writableDatasetTypes.add(1, html);
        
        WRITABLE = new HashMap<>();
        WRITABLE.put(Dataset.class, Collections.unmodifiableList(writableDatasetTypes));
        WRITABLE.put(Model.class, coreTypes.getWritable(Model.class));
        WRITABLE.put(ResultSet.class, coreTypes.getWritable(ResultSet.class));
    }
    
    public MediaTypes()
    {
        this(READABLE, WRITABLE);
    }
    
    public MediaTypes(Map<Class, List<javax.ws.rs.core.MediaType>> readable, Map<Class, List<javax.ws.rs.core.MediaType>> writable)
    {
        super(readable, writable);
    }
    
}
