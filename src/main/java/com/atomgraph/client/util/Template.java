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
package com.atomgraph.client.util;

import com.atomgraph.client.exception.OntologyException;
import com.atomgraph.client.vocabulary.LDT;
import com.atomgraph.client.vocabulary.SPL;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see com.atomgraph.processor.model.impl.TemplateImpl
 */
public class Template
{
    
    private final Resource template;
    
    public Template(Resource template)
    {
        this.template = template;
    }
    
    protected Resource getTemplate()
    {
        return template;
    }
    
    public Map<Resource, Resource> getParameters()
    {
        return getParameters(getTemplate(), new HashMap<Resource, Resource>());
    }
    
    // TO-DO: align the Processor TemplateImpl methods with this
    protected Map<Resource, Resource> getParameters(Resource template, Map<Resource, Resource> params)
    {
        StmtIterator paramIt = template.listProperties(LDT.param);
        try
        {
            while(paramIt.hasNext())
            {
                Statement stmt = paramIt.next();
                if (!stmt.getObject().isResource())
                    throw new OntologyException(template, LDT.param, "Value is not a resource");
                    
                Resource param = stmt.getResource();
                if (param.getProperty(SPL.predicate) == null)
                    throw new OntologyException(param, SPL.predicate, "Value is missing");
                if (!param.getProperty(SPL.predicate).getObject().isResource())
                    throw new OntologyException(param, SPL.predicate, "Value is not a resource");
                
                params.putIfAbsent(param.getProperty(SPL.predicate).getResource(), param); // reject Parameters for existing predicates
            }
        }
        finally
        {
            paramIt.close();
        }

        // walk recursively up the super-template chain (if any)
        StmtIterator extendsIt = template.listProperties(LDT.extends_);
        try
        {
            while(extendsIt.hasNext())
            {
                Statement stmt = extendsIt.next();
                if (!stmt.getObject().isResource())
                    throw new OntologyException(template, LDT.extends_, "Value is not a resource");
                
                Resource superTemplate = stmt.getResource();
                params.putAll(getParameters(superTemplate, params));
            }
        }
        finally
        {
            extendsIt.close();
        }

        return params;
    }

}
