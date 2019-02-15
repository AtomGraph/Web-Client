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
package com.atomgraph.client.exception;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class OntologyException extends RuntimeException
{
    
    private final Resource root;
    
    public OntologyException(String message, Resource root, Property property)
    {
        this(message, null, root, property);
    }
    
    public OntologyException(String message, Throwable throwable, Resource root, Property property)
    {
        super("[" + root.getURI() + "]" + property != null ? "[" + property.getURI() + "] " : "" + message, throwable);
        this.root = root;
    }
    
    public Resource getRoot()
    {
        return root;
    }
    
}
