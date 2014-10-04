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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.update.UpdateRequest;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

/**
 * SPARQL update builder based on SPIN RDF syntax
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/1.2.0/spin/apidocs/org/topbraid/spin/model/update/Update.html">SPIN Update</a>
 */
public class UpdateBuilder implements Update
{
    private Update update = null;
    
    protected UpdateBuilder(Update update)
    {
	if (update == null) throw new IllegalArgumentException("SPIN Update cannot be null");

        // Initialize system functions and templates
        SPINModuleRegistry.get().init();

	this.update = update;
    }
    
    protected Update getUpdate()
    {
	return update;
    }

    public static UpdateBuilder fromUpdate(Update update)
    {
	return new UpdateBuilder(update);
    }

    public static UpdateBuilder fromResource(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Update Resource cannot be null");

	return fromUpdate(SPINFactory.asUpdate(resource));
    }

    public static UpdateBuilder fromUpdate(com.hp.hpl.jena.update.Update update, String uri, Model model)
    {
	if (update == null) throw new IllegalArgumentException("Update cannot be null");
	
	ARQ2SPIN arq2spin = new ARQ2SPIN(model);
	return fromUpdate(arq2spin.createUpdate(update, uri));
    }

    protected Resource createTripleTemplate(Statement stmt)
    {
	if (stmt == null) throw new IllegalArgumentException("Statement cannot be null");

	return getModel().createResource().
	    addProperty(SP.subject, stmt.getSubject()).
	    addProperty(SP.predicate, stmt.getPredicate()).
	    addProperty(SP.object, stmt.getObject());
    }

    protected RDFList createDataList(Model model)
    {
	if (model == null) throw new IllegalArgumentException("Model cannot be null");

	RDFList data = getModel().createList();
	
	StmtIterator it = model.listStatements();
	while (it.hasNext())
	    data = data.with(createTripleTemplate(it.next()));
	
	return data;
    }

    public UpdateRequest build()
    {
	com.hp.hpl.jena.update.UpdateRequest request = ARQFactory.get().createUpdateRequest(getUpdate());
	
	// generate SPARQL query string
	removeAll(SP.text)
	    .addLiteral(SP.text, getModel().createTypedLiteral(request.toString()));
	
	return request;
    }

    @Override
    public String getComment()
    {
	return getUpdate().getComment();
    }

    @Override
    public void print(PrintContext pc)
    {
	getUpdate().print(pc);
    }

    @Override
    public AnonId getId()
    {
	return getUpdate().getId();
    }

    @Override
    public Resource inModel(Model model)
    {
	return getUpdate().inModel(model);
    }

    @Override
    public boolean hasURI(String string)
    {
	return getUpdate().hasURI(string);
    }

    @Override
    public String getURI()
    {
	return getUpdate().getURI();
    }

    @Override
    public String getNameSpace()
    {
	return getUpdate().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return getUpdate().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property prprt)
    {
	return getUpdate().getRequiredProperty(prprt);
    }

    @Override
    public Statement getProperty(Property prprt)
    {
	return getUpdate().getProperty(prprt);
    }

    @Override
    public StmtIterator listProperties(Property prprt)
    {
	return getUpdate().listProperties(prprt);
    }

    @Override
    public StmtIterator listProperties()
    {
	return getUpdate().listProperties();
    }

    @Override
    public Resource addLiteral(Property prprt, boolean bln)
    {
	return getUpdate().addLiteral(prprt, bln);
    }

    @Override
    public Resource addLiteral(Property prprt, long l)
    {
	return getUpdate().addLiteral(prprt, l);
    }

    @Override
    public Resource addLiteral(Property prprt, char c)
    {
	return getUpdate().addLiteral(prprt, c);
    }

    @Override
    public Resource addLiteral(Property prprt, double d)
    {
	return getUpdate().addLiteral(prprt, d);
    }

    @Override
    public Resource addLiteral(Property prprt, float f)
    {
	return getUpdate().addLiteral(prprt, f);
    }

    @Override
    public Resource addLiteral(Property prprt, Object o)
    {
	return getUpdate().addLiteral(prprt, o);
    }

    @Override
    public Resource addLiteral(Property prprt, Literal ltrl)
    {
	return getUpdate().addLiteral(prprt, ltrl);
    }

    @Override
    public Resource addProperty(Property prprt, String string)
    {
	return getUpdate().addProperty(prprt, string);
    }

    @Override
    public Resource addProperty(Property prprt, String string, String string1)
    {
	return getUpdate().addProperty(prprt, string, string1);
    }

    @Override
    public Resource addProperty(Property prprt, String string, RDFDatatype rdfd)
    {
	return getUpdate().addProperty(prprt, string, rdfd);
    }

    @Override
    public Resource addProperty(Property prprt, RDFNode rdfn)
    {
	return getUpdate().addProperty(prprt, rdfn);
    }

    @Override
    public boolean hasProperty(Property prprt)
    {
	return getUpdate().hasProperty(prprt);
    }

    @Override
    public boolean hasLiteral(Property prprt, boolean bln)
    {
	return getUpdate().hasLiteral(prprt, bln);
    }

    @Override
    public boolean hasLiteral(Property prprt, long l)
    {
	return getUpdate().hasLiteral(prprt, l);
    }

    @Override
    public boolean hasLiteral(Property prprt, char c)
    {
	return getUpdate().hasLiteral(prprt, c);
    }

    @Override
    public boolean hasLiteral(Property prprt, double d)
    {
	return getUpdate().hasLiteral(prprt, d);
    }

    @Override
    public boolean hasLiteral(Property prprt, float f)
    {
	return getUpdate().hasLiteral(prprt, f);
    }

    @Override
    public boolean hasLiteral(Property prprt, Object o)
    {
	return getUpdate().hasLiteral(prprt, o);
    }

    @Override
    public boolean hasProperty(Property prprt, String string)
    {
	return getUpdate().hasProperty(prprt, string);
    }

    @Override
    public boolean hasProperty(Property prprt, String string, String string1)
    {
	return getUpdate().hasProperty(prprt, string, string1);
    }

    @Override
    public boolean hasProperty(Property prprt, RDFNode rdfn)
    {
	return getUpdate().hasProperty(prprt, rdfn);
    }

    @Override
    public Resource removeProperties()
    {
	return getUpdate().removeProperties();
    }

    @Override
    public Resource removeAll(Property prprt)
    {
	return getUpdate().removeAll(prprt);
    }

    @Override
    public Resource begin()
    {
	return getUpdate().begin();
    }

    @Override
    public Resource abort()
    {
	return getUpdate().abort();
    }

    @Override
    public Resource commit()
    {
	return getUpdate().commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property prprt)
    {
	return getUpdate().getPropertyResourceValue(prprt);
    }

    @Override
    public boolean isAnon()
    {
	return getUpdate().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return getUpdate().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return getUpdate().isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return getUpdate().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> type)
    {
	return getUpdate().as(type);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> type)
    {
	return getUpdate().canAs(type);
    }

    @Override
    public Model getModel()
    {
	return getUpdate().getModel();
    }

    @Override
    public Object visitWith(RDFVisitor rdfv)
    {
	return getUpdate().visitWith(rdfv);
    }

    @Override
    public Resource asResource()
    {
	return getUpdate().asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return getUpdate().asLiteral();
    }

    @Override
    public Node asNode()
    {
	return getUpdate().asNode();
    }

    @Override
    public String toString()
    {
	return getUpdate().toString();
    }
}