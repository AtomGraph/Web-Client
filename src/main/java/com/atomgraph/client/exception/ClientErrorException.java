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

import javax.ws.rs.core.Response;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ClientErrorException extends RuntimeException
{
    
    transient private final Response cr;
    transient private final Dataset dataset;
    
    public ClientErrorException(Response cr)
    {
        this(cr, null);
    }
    
    public ClientErrorException(Response cr, Dataset dataset)
    {
        this.cr = cr;
        this.dataset = dataset;
    }
    
    public Response getResponse()
    {
        return cr;
    }
    
    public Model getModel()
    {
        return dataset.getDefaultModel();
    }
    
}
