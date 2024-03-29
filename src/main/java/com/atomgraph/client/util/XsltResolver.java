/**
 *  Copyright 2021 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.util;

import com.atomgraph.core.client.LinkedDataClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.MediaType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.LocationMapper;

/**
 * URI resolver that loads XSLT stylesheets only.
 * Useful in the compilation phase, for the resolution of <code>xsl:include</code> and <code>xsl:import</code> URLs.
 * 
 * @author {@literal Martynas Jusevičius <martynas@atomgraph.com>}
 */
public class XsltResolver extends DataManagerImpl
{

    private final MediaType[] acceptedXMLMediaTypes;

    public XsltResolver(LocationMapper mapper, Map<String, Model> modelCache, LinkedDataClient ldc, boolean cacheModelLoads, boolean preemptiveAuth, boolean resolvingUncached)
    {
        super(mapper, modelCache, ldc, cacheModelLoads, preemptiveAuth, resolvingUncached);
        
        List<jakarta.ws.rs.core.MediaType> acceptableXMLMediaTypeList = new ArrayList();
        acceptableXMLMediaTypeList.add(com.atomgraph.client.MediaType.TEXT_XSL_TYPE);
        acceptedXMLMediaTypes = acceptableXMLMediaTypeList.toArray(MediaType[]::new);
    }

    @Override
    public MediaType[] getAcceptedXMLMediaTypes()
    {
        return acceptedXMLMediaTypes;
    }
    
}
