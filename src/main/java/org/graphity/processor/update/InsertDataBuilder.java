/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @see InsertDataBuilder
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/1.2.0/spin/apidocs/org/topbraid/spin/model/Query.html">SPIN Query</a>
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