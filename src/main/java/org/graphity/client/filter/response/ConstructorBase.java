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

package org.graphity.client.filter.response;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphity.client.exception.SitemapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.inference.SPINConstructors;
import org.topbraid.spin.util.CommandWrapper;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINQueryFinder;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ConstructorBase
{
    private static final Logger log = LoggerFactory.getLogger(ConstructorBase.class);

    public void construct(OntClass forClass, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");

        addInstance(forClass, SPIN.constructor, targetModel.createResource(), targetModel);
        addClass(forClass, targetModel);
    }

    // workaround for SPIN API limitation: https://groups.google.com/d/msg/topbraid-users/AVXXEJdbQzk/w5NrJFs35-0J
    public OntModel fixOntModel(OntModel ontModel)
    {
        if (ontModel == null) throw new IllegalArgumentException("OntModel cannot be null");

        OntModel fixedModel = ModelFactory.createOntologyModel(ontModel.getSpecification());
        fixedModel.add(ontModel);
        
        List<Statement> toDelete = new ArrayList<>();
        StmtIterator it = fixedModel.listStatements(null, SP.text, (RDFNode)null);
        try
        {
            while (it.hasNext())
            {
                Statement stmt = it.next();
                com.hp.hpl.jena.rdf.model.Resource queryOrTemplateCall = stmt.getSubject();
                StmtIterator propIt = queryOrTemplateCall.listProperties();
                try
                {
                    while (propIt.hasNext())
                    {
                        Statement propStmt = propIt.next();
                        if (!propStmt.getPredicate().equals(RDF.type) && !propStmt.getPredicate().equals(SP.text))
                            toDelete.add(propStmt);
                    }
                }
                finally
                {
                    propIt.close();
                }
            }            
        }
        finally
        {
            it.close();
        }
        
        fixedModel.remove(toDelete);
        
        return fixedModel;
    }
    
    public void addInstance(OntClass forClass, Property property, Resource instance, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");
        if (instance == null) throw new IllegalArgumentException("Resource cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");
        
        Statement stmt = getConstructorStmt(forClass, property);
        if (stmt == null || !stmt.getObject().isResource())
        {
            if (log.isErrorEnabled()) log.error("Constructor is invoked but {} is not defined for class '{}'", property, forClass.getURI());
            throw new SitemapException("Constructor is invoked but '" + property.getURI() + "' not defined for class '" + forClass.getURI() +"'");
        }

        List<com.hp.hpl.jena.rdf.model.Resource> newResources = new ArrayList<>();
        Set<com.hp.hpl.jena.rdf.model.Resource> reachedTypes = new HashSet<>();
        OntModel fixedModel = fixOntModel(forClass.getOntModel());
        Map<com.hp.hpl.jena.rdf.model.Resource, List<CommandWrapper>> class2Constructor = SPINQueryFinder.getClass2QueryMap(fixedModel, fixedModel, property, false, false);
        SPINConstructors.constructInstance(fixedModel, instance, forClass, targetModel, newResources, reachedTypes, class2Constructor, null, null, null);
        instance.addProperty(RDF.type, forClass);
        
        // evaluate AllValuesFromRestriction to construct related instances
        ExtendedIterator<OntClass> superClassIt = forClass.listSuperClasses();
        try
        {
            while (superClassIt.hasNext())
            {
                OntClass superClass = superClassIt.next();
                if (superClass.canAs(AllValuesFromRestriction.class))
                {
                    AllValuesFromRestriction avfr = superClass.as(AllValuesFromRestriction.class);
                    if (avfr.getAllValuesFrom().canAs(OntClass.class))
                    {
                        OntClass valueClass = avfr.getAllValuesFrom().as(OntClass.class);
                        if (!valueClass.equals(forClass)) // avoid circular restrictions
                        {
                            com.hp.hpl.jena.rdf.model.Resource value = targetModel.createResource().
                                addProperty(RDF.type, valueClass);
                            instance.addProperty(avfr.getOnProperty(), value);
                        
                            // add inverse properties
                            ExtendedIterator<? extends OntProperty> it = avfr.getOnProperty().listInverseOf();
                            try
                            {
                                while (it.hasNext())
                                {
                                    value.addProperty(it.next(), instance);
                                }
                            }
                            finally
                            {
                                it.close();
                            }

                            addInstance(valueClass, property, value, targetModel);
                        }
                    }
                }
            }
        }
        finally
        {
            superClassIt.close();
        }        
    }
    
    public Statement getConstructorStmt(Resource cls, Property property)
    {
        if (cls == null) throw new IllegalArgumentException("Resource cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        Statement stmt = cls.getProperty(property);        
        if (stmt != null) return stmt;
        
        for (Resource superCls : JenaUtil.getAllSuperClasses(cls))
        {
            Statement superClassStmt = getConstructorStmt(superCls, property);
            if (superClassStmt != null) return superClassStmt;
        }
        
        return null;
    }
    
    // TO-DO: this method should not be necessary when system ontologies/classes are dereferencable! -->
    public void addClass(OntClass forClass, Model targetModel)
    {
        if (forClass == null) throw new IllegalArgumentException("OntClass cannot be null");
        if (targetModel == null) throw new IllegalArgumentException("Model cannot be null");    

        String queryString = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
"PREFIX  spin: <http://spinrdf.org/spin#>\n" +
"\n" +
"DESCRIBE ?Class ?Constraint\n" +
"WHERE\n" +
"  { ?Class rdfs:isDefinedBy ?Ontology\n" +
"    OPTIONAL\n" +
"      { ?Class spin:constraint ?Constraint }\n" +
"  }";
        
        // the client needs at least labels and constraints
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add(RDFS.Class.getLocalName(), forClass);
        Query query = new ParameterizedSparqlString(queryString, qsm).asQuery();
        QueryExecution qex = QueryExecutionFactory.create(query, forClass.getOntModel());
        try
        {
            targetModel.add(qex.execDescribe());
        }
        finally
        {
            qex.close();
        }
    }
    
}
