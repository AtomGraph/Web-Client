/*
 * Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>.
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

import com.sun.jersey.api.client.ClientResponse;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ClientErrorException extends RuntimeException
{
    
    transient private final ClientResponse cr;
    transient private final Model model;
    
    public ClientErrorException(ClientResponse cr)
    {
        this(cr, null);
    }
    
    public ClientErrorException(ClientResponse cr, Model model)
    {
        this.cr = cr;
        this.model = model;
    }
    
    public ClientResponse getClientResponse()
    {
        return cr;
    }
    
    public Model getModel()
    {
        return model;
    }
    
}
