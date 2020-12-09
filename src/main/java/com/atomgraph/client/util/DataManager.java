/*
 * Copyright 2020 Martynas Jusevičius <martynas@atomgraph.com>.
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

import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public interface DataManager extends com.atomgraph.core.util.jena.DataManager, URIResolver, UnparsedTextURIResolver
{
    
    javax.ws.rs.core.MediaType[] getAcceptedMediaTypes();
    
    MediaType[] getAcceptedXMLMediaTypes();
    
    Response load(String filenameOrURI);
    
    boolean resolvingUncached(String filenameOrURI);
    
    boolean isResolvingMapped();
    
    boolean isMapped(String filenameOrURI);
    
    Source getSource(Model model, String systemId) throws IOException;
        
    Source getSource(ResultSet results, String systemId) throws IOException;
    
}
