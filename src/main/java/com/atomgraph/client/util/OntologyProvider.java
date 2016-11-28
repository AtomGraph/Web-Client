/*
 * Copyright 2016 Martynas Jusevičius <martynas@atomgraph.com>.
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

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads ontology by URI.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class OntologyProvider
{

    private static final Logger log = LoggerFactory.getLogger(OntologyProvider.class);
    
    /**
     * Loads ontology by URI.
     * 
     * @param ontologyURI ontology location
     * @param ontModelSpec ontology model specification
     * @return ontology model
     */
    public OntModel getOntModel(String ontologyURI, OntModelSpec ontModelSpec)
    {
        if (ontologyURI == null) throw new IllegalArgumentException("URI cannot be null");
        if (ontModelSpec == null) throw new IllegalArgumentException("OntModelSpec cannot be null");        
        if (log.isDebugEnabled()) log.debug("Loading sitemap ontology from URI: {}", ontologyURI);

        OntModel ontModel = OntDocumentManager.getInstance().getOntology(ontologyURI, ontModelSpec);
        
        // explicitly loading owl:imports -- workaround for Jena bug: https://issues.apache.org/jira/browse/JENA-1210
        ontModel.enterCriticalSection(Lock.WRITE);
        try
        {
            ontModel.loadImports();
        }
        finally
        {
            ontModel.leaveCriticalSection();
        }

        // lock and clone the model to avoid ConcurrentModificationExceptions
        ontModel.enterCriticalSection(Lock.READ);
        try
        {            
            return ModelFactory.createOntologyModel(ontModelSpec,
                    ModelFactory.createUnion(ModelFactory.createDefaultModel(), ontModel.getBaseModel()));
        }
        finally
        {
            ontModel.leaveCriticalSection();
        }
    }

}
