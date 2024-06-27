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

package com.atomgraph.client.util;

import com.atomgraph.client.exception.OntologyException;
import com.atomgraph.client.vocabulary.SP;
import com.atomgraph.client.vocabulary.SPIN;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class Constructor
{

    private static final Logger log = LoggerFactory.getLogger(Constructor.class);

    public Resource construct(OntClass forClass, Model targetModel, String baseURI)
    {
        return construct(forClass, targetModel, baseURI, null);
    }
    
    public Resource construct(OntClass forClass, Model targetModel, String baseURI, String resourceURI)
    {
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");

        final Resource resource;
        if (resourceURI == null) resource = targetModel.createResource(); // blank node
        else resource = targetModel.createResource(resourceURI); // URI resource
        
        return constructInstance(forClass, SPIN.constructor, resource, baseURI).
            addProperty(RDF.type, forClass);
    }

    /**
     * Constructs new anonymous individual of an ontology class.
     * It walks up the superclass chains and executes SPIN constructors.
     *
     * @param forClass class for which to construct new instance
     * @param property property that attaches <code>CONSTRUCT</code> query resource to class resource, usually <code>spin:constructor</code>
     * @param instance the instance resource
     * @param baseURI base URI of the query
     * @return the instance resource with constructed properties
     */
    public Resource constructInstance(OntClass forClass, Property property, Resource instance, String baseURI)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (instance == null) throw new IllegalArgumentException("Instance Resource cannot be null");
        if (baseURI == null) throw new IllegalArgumentException("Base URI cannot be null");
        
        NodeIterator constructorIt = forClass.listPropertyValues(property);
        try
        {
            while (constructorIt.hasNext()) // traverse all constructors
            {
                RDFNode constructor = constructorIt.next();
                if (!constructor.isResource())
                {
                    if (log.isErrorEnabled()) log.error("Constructor is invoked but {} is not defined for class '{}'", property, forClass.getURI());
                    throw new OntologyException("Constructor property not defined", forClass, property);
                }

                Statement queryText = constructor.asResource().getProperty(SP.text);
                if (queryText == null || !queryText.getObject().isLiteral())
                {
                    if (log.isErrorEnabled()) log.error("Constructor resource '{}' does not have sp:text property", constructor);
                    throw new OntologyException("Query property not defined", constructor.asResource(), SP.text);
                }

                try
                {
                    Query basedQuery = new ParameterizedSparqlString(queryText.getString(), baseURI).asQuery();
                    QuerySolutionMap bindings = new QuerySolutionMap();
                    bindings.add(SPIN.THIS_VAR_NAME, instance);
                    // skip SPIN template bindings for now - might support later

                    // execute the constructor on the target model
                    try (QueryExecution qex = QueryExecution.create().query(basedQuery).model(instance.getModel()).initialBinding(bindings).build())
                    {
                        instance.getModel().add(qex.execConstruct());
                    }
                }
                catch (QueryParseException ex)
                {
                    if (log.isErrorEnabled()) log.error("Constructor resource '{}' sp:text property contains an invalid SPARQL CONSTRUCT", constructor);
                    throw new OntologyException("Invalid SPARQL CONSTRUCT", ex, constructor.asResource(), SP.text);
                }
            }
        }
        finally
        {
            constructorIt.close();
        }
        
        ExtendedIterator<OntClass> superClassIt = forClass.listSuperClasses();
        try
        {
            while (superClassIt.hasNext())
            {
                OntClass superClass = superClassIt.next();
                constructInstance(superClass, property, instance, baseURI);
            }
        }
        finally
        {
            superClassIt.close();
        }

        return instance;
    }

}
