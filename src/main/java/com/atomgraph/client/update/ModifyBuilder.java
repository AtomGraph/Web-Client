/**
 *  Copyright 2013 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.update;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import java.net.URI;
import org.spinrdf.arq.ARQ2SPIN;
import org.spinrdf.model.Element;
import org.spinrdf.model.ElementList;
import org.spinrdf.model.NamedGraph;
import org.spinrdf.model.SPINFactory;
import org.spinrdf.model.Variable;
import org.spinrdf.model.update.Modify;
import org.spinrdf.model.update.Update;
import org.spinrdf.vocabulary.SP;

/**
 * SPARQL DELETE/INSERT builder based on SPIN RDF syntax
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/1.2.0/spin/apidocs/org/topbraid/spin/model/update/Modify.html">SPIN Modify</a>
 */
public class ModifyBuilder extends UpdateBuilder implements Modify
{
    private Modify modify = null;

    private ModifyBuilder(Modify modify)
    {
	super(modify);
	this.modify = modify;
    }

    public static ModifyBuilder fromModify(Modify modify)
    {
	return new ModifyBuilder(modify);
    }

    public static ModifyBuilder fromResource(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Modify Resource cannot be null");
	
	Update update = SPINFactory.asUpdate(resource);
	if (update == null || !(update instanceof Modify))
	    throw new IllegalArgumentException("ModifyBuilder Resource must be a SPIN INSERT/DELETE command");

	return fromModify((Modify)update);
    }
    
    public static ModifyBuilder fromModify(Model model)
    {
	return fromResource(model.createResource().
	    addProperty(RDF.type, SP.Modify));
    }

    public static ModifyBuilder fromUpdate(Update update)
    {
	return fromModify((Modify)update);
    }

    public static ModifyBuilder fromUpdate(org.apache.jena.update.Update update, Model model)
    {
        return fromUpdate(update, null, model);
    }
    
    public static ModifyBuilder fromUpdate(org.apache.jena.update.Update update, String uri, Model model)
    {
	if (update == null) throw new IllegalArgumentException("Update cannot be null");
	
	ARQ2SPIN arq2spin = new ARQ2SPIN(model);
	return fromUpdate(arq2spin.createUpdate(update, uri));
    }
    
    public ModifyBuilder insertPattern(RDFList pattern)
    {
	if (pattern == null) throw new IllegalArgumentException("INSERT pattern cannot be null");

	addProperty(SP.insertPattern, pattern);
	
	return this;
    }
    
    public ModifyBuilder insertPattern(Model model)
    {
	return insertPattern(createDataList(model));
    }

    public ModifyBuilder insertPattern(NamedGraph graph, RDFList dataList)
    {
	if (graph == null) throw new IllegalArgumentException("INSERT DATA graph resource cannot be null");
	if (dataList == null) throw new IllegalArgumentException("INSERT DATA data List resource cannot be null");
	
	return insertPattern(getModel().createList().
		with(graph.addProperty(SP.elements, dataList)));
    }

    public ModifyBuilder insertPattern(NamedGraph graph, Model model)
    {
	return insertPattern(graph, createDataList(model));
    }

    public ModifyBuilder insertPattern(URI graphUri, RDFList dataList)
    {
	if (graphUri == null) throw new IllegalArgumentException("INSERT graph resource cannot be null");
	if (dataList == null) throw new IllegalArgumentException("INSERT data List cannot be null");

	NamedGraph graph = getModel().createResource().
	    addProperty(RDF.type, SP.NamedGraph).
	    addProperty(SP.graphNameNode, getModel().createResource(graphUri.toString())).
	    as(NamedGraph.class);
	
	return insertPattern(graph, dataList);
    }

    public ModifyBuilder insertPattern(String graphVarName, RDFList dataList)
    {
        return insertPattern(SPINFactory.createVariable(getModel(), graphVarName), dataList);
    }    

    public ModifyBuilder insertPattern(Variable graphVar, RDFList dataList)
    {
	if (graphVar == null) throw new IllegalArgumentException("INSERT graph variable cannot be null");
	if (dataList == null) throw new IllegalArgumentException("INSERT data List cannot be null");

	NamedGraph graph = getModel().createResource().
	    addProperty(RDF.type, SP.NamedGraph).
	    addProperty(SP.graphNameNode, graphVar).
	    as(NamedGraph.class);
	
	return insertPattern(graph, dataList);
    }

    public ModifyBuilder insertPattern(URI graphUri, Model model)
    {
	return insertPattern(graphUri, createDataList(model));
    }
    
    public ModifyBuilder deletePattern(RDFList pattern)
    {
	if (pattern == null) throw new IllegalArgumentException("DELETE pattern cannot be null");

	addProperty(SP.deletePattern, pattern);
	
	return this;
    }

    public ModifyBuilder deletePattern(Model model)
    {
	return deletePattern(createDataList(model));
    }

    public ModifyBuilder where(Resource element)
    {
	if (element == null) throw new IllegalArgumentException("WHERE element cannot be null");

	return where(SPINFactory.asElement(element));
    }
    
    public ModifyBuilder where(Element element)
    {
	if (element == null) throw new IllegalArgumentException("WHERE element cannot be null");
	//getWhereElements().add(element); // doesn't work?

	if (!hasProperty(SP.where))
	    addProperty(SP.where, getModel().createList(new RDFNode[]{element}));
	else
	    getPropertyResourceValue(SP.where).
		    as(RDFList.class).
		    add(element);
	
	return this;
    }

    public ModifyBuilder where(ElementList elementList)
    {
	if (elementList == null) throw new IllegalArgumentException("WHERE element list cannot be null");
	if (getWhere() != null) throw new IllegalArgumentException("WHERE element already present in the query");

	addProperty(SP.where, elementList);
	
	return this;
    }
   
    @Override
    public Modify getUpdate()
    {
	return modify;
    }

    @Override
    public ElementList getWhere()
    {
	return getUpdate().getWhere();
    }

}