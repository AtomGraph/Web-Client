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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
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
    
    @Override
    public MediaTypes getMediaTypes()
    {
        Map<Class, List<MediaType>> writable = new HashMap<>();

        List<MediaType> writableModelTypes = new ArrayList<>(super.getMediaTypes().getWritable(Model.class));
        MediaType xhtmlXml = new MediaType(MediaType.APPLICATION_XHTML_XML_TYPE.getType(), MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), MediaTypes.UTF8_PARAM);
        writableModelTypes.add(0, xhtmlXml);
        
        writable.put(Model.class, Collections.unmodifiableList(writableModelTypes));
        writable.put(ResultSet.class, Collections.unmodifiableList(super.getMediaTypes().getWritable(ResultSet.class)));
        
        return new MediaTypes(super.getMediaTypes().getReadable(), writable);
    }
    
}