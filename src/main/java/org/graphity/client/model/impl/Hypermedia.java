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

package org.graphity.client.model.impl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.graphity.client.vocabulary.GC;
import org.graphity.client.vocabulary.GP;
import org.graphity.core.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Hypermedia
{
    private static final Logger log = LoggerFactory.getLogger(Hypermedia.class);
    
    private final OntClass matchedOntClass;

    public Hypermedia(OntClass matchedOntClass)
    {
        if (matchedOntClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        this.matchedOntClass = matchedOntClass;
    }
        
    public Model addStates(Resource resource, Model model)
    {
        NodeIterator it = getMatchedOntClass().listPropertyValues(GC.supportedMode);
        try
        {
            while (it.hasNext())
            {
                RDFNode supportedMode = it.next();
                if (!supportedMode.isURIResource())
                {
                    if (log.isErrorEnabled()) log.error("Invalid Mode defined for template '{}' (gc:supportedMode)", getMatchedOntClass().getURI());
                    throw new ConfigurationException("Invalid Mode defined for template '" + getMatchedOntClass().getURI() +"'");
                }

                if (!supportedMode.equals(GP.ConstructMode))
                {
                    ResIterator resIt = model.listSubjectsWithProperty(GP.pageOf, resource);
                    try
                    {
                        while (resIt.hasNext())
                        {
                            Resource page = resIt.next();
                            String modeURI = getStateUriBuilder(UriBuilder.fromUri(page.getURI()), URI.create(supportedMode.asResource().getURI())).build().toString();
                            createState(model.createResource(modeURI), supportedMode.asResource()).
                                addProperty(RDF.type, FOAF.Document).
                                addProperty(GC.layoutOf, resource);
                        }
                    }
                    finally
                    {
                        resIt.close();
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return model;
    }
    
    /**
     * Creates a page resource for the current container. Includes HATEOS previous/next links.
     * 
     * @param state
     * @param mode
     * @return page resource
     */
    public com.hp.hpl.jena.rdf.model.Resource createState(com.hp.hpl.jena.rdf.model.Resource state, com.hp.hpl.jena.rdf.model.Resource mode)
    {
        if (state == null) throw new IllegalArgumentException("Resource subject cannot be null");        

        if (mode != null) state.addProperty(GC.mode, mode);
        
        return state;
    }

    /**
     * Returns URI builder instantiated with pagination parameters for the current page.
     * 
     * @param uriBuilder
     * @param mode
     * @return URI builder
     */    
    public UriBuilder getStateUriBuilder(UriBuilder uriBuilder, URI mode)
    {        
        if (uriBuilder == null) throw new IllegalArgumentException("UriBuilder cannot be null");        

        if (mode != null) uriBuilder.queryParam(GC.mode.getLocalName(), mode);
        
	return uriBuilder;
    }

    public boolean hasSuperClass(OntClass subClass, OntClass superClass)
    {
        ExtendedIterator<OntClass> it = subClass.listSuperClasses(false);
        
        try
        {
            while (it.hasNext())
            {
                OntClass nextClass = it.next();
                if (nextClass.equals(superClass) || hasSuperClass(nextClass, superClass)) return true;
            }
        }
        finally
        {
            it.close();
        }
        
        return false;
    }
    
    public OntClass getMatchedOntClass()
    {
        return matchedOntClass;
    }

}
