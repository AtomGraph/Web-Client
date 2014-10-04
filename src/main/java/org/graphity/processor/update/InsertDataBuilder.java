/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.update;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import java.net.URI;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.update.InsertData;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.vocabulary.SP;

/**
 * SPARQL INSERT DATA builder based on SPIN RDF syntax
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/1.2.0/spin/apidocs/org/topbraid/spin/model/update/InsertData.html">SPIN InsertData</a>
 */
public class InsertDataBuilder extends UpdateBuilder implements InsertData
{
    private InsertData insertData = null;
    
    protected InsertDataBuilder(InsertData insertData)
    {
	super(insertData);
	this.insertData = insertData;
    }
    
    public static InsertDataBuilder fromInsertData(InsertData insertData)
    {
	return new InsertDataBuilder(insertData);
    }

    public static InsertDataBuilder fromResource(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("InsertData Resource cannot be null");
	
	Update update = SPINFactory.asUpdate(resource);
	if (update == null || !(update instanceof InsertData))
	    throw new IllegalArgumentException("InsertDataBuilder Resource must be a SPIN INSERT DATA Query");

	return fromInsertData((InsertData)update);
    }

    public static InsertDataBuilder newInstance()
    {
	return fromResource(ModelFactory.createDefaultModel().createResource().
	    addProperty(RDF.type, SP.InsertData));
    }

    public static InsertDataBuilder fromData(Model model)
    {
	return newInstance().data(model);
    }

    public static InsertDataBuilder fromData(NamedGraph graph, Model model)
    {
	return newInstance().data(graph, model);
    }

    public static InsertDataBuilder fromData(URI graphUri, Model model)
    {
	return newInstance().data(graphUri, model);
    }

    public InsertDataBuilder data(RDFList dataList)
    {
	if (dataList == null) throw new IllegalArgumentException("INSERT DATA data List cannot be null");

	addProperty(SP.data, dataList);

	return this;
    }
    
    public InsertDataBuilder data(Model model)
    {
	return data(createDataList(model));
    }

    @Override
    protected InsertData getUpdate()
    {
	return insertData;
    }

    public InsertDataBuilder data(NamedGraph graph, RDFList dataList)
    {
	if (graph == null) throw new IllegalArgumentException("INSERT DATA graph resource cannot be null");
	if (dataList == null) throw new IllegalArgumentException("INSERT DATA data List resource cannot be null");
	
	return data(getModel().createList().
		with(graph.addProperty(SP.elements, dataList)));
    }

    public InsertDataBuilder data(NamedGraph graph, Model model)
    {
	return data(graph, createDataList(model));
    }

    public InsertDataBuilder data(URI graphUri, RDFList dataList)
    {
	if (graphUri == null) throw new IllegalArgumentException("INSERT DATA graph resource cannot be null");
	if (dataList == null) throw new IllegalArgumentException("INSERT DATA data List cannot be null");

	NamedGraph graph = getModel().createResource().
	    addProperty(RDF.type, SP.NamedGraph).
	    addProperty(SP.graphNameNode, getModel().createResource(graphUri.toString())).
	    as(NamedGraph.class);
	
	return data(graph, dataList);
    }

    public InsertDataBuilder data(URI graphUri, Model model)
    {
	return data(graphUri, createDataList(model));
    }

}