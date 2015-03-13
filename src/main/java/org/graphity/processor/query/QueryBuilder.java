/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.query;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.*;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.AbstractElementVisitor;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.model.visitor.ElementWalker;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

/**
 * SPARQL query builder based on SPIN RDF syntax
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see SelectBuilder
 * @see <a href="http://spinrdf.org/sp.html">SPIN - SPARQL Syntax</a>
 * @see <a href="http://topbraid.org/spin/api/">SPIN API</a>
 */
public class QueryBuilder implements org.topbraid.spin.model.Query
{
    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);
    private final org.topbraid.spin.model.Query query;
    private Select subSelect = null;

    private final ElementVisitor elementVisitor = new AbstractElementVisitor()
    {

	@Override
	public void visit(SubQuery subQuery)
	{
		org.topbraid.spin.model.Query sub = subQuery.getQuery();
		// only SELECTs can be subqueries??
		if (sub.canAs(Select.class)) setSubSelect(sub.as(Select.class));
	}

    };

    /**
     * Constructs builder from SPIN query
     * 
     * @param query SPIN query resource
     */
    protected QueryBuilder(org.topbraid.spin.model.Query query)
    {
	if (query == null) throw new IllegalArgumentException("SPIN Query cannot be null");

        // Initialize system functions and templates
        SPINModuleRegistry.get().init();
	
	this.query = query;
    }

    /**
     * SPIN query resource
     * 
     * @return the query resource of this builder
     */
    protected org.topbraid.spin.model.Query getQuery()
    {
	return query;
    }
    
    public static QueryBuilder fromQuery(org.topbraid.spin.model.Query query)
    {
	return new QueryBuilder(query);
    }

    public static QueryBuilder fromResource(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Query Resource cannot be null");

	return fromQuery(SPINFactory.asQuery(resource));
    }

    public static QueryBuilder fromQuery(Query query, String uri, Model model)
    {
	if (query == null) throw new IllegalArgumentException("Query cannot be null");
	
	ARQ2SPIN arq2spin = new ARQ2SPIN(model);
	return fromQuery(arq2spin.createQuery(query, uri));
    }

    public static QueryBuilder fromQuery(Query query, Model model)
    {
	return fromQuery(query, null, model);
    }

    public static QueryBuilder fromQueryString(String queryString, Model model)
    {
	return fromQuery(QueryFactory.create(queryString), model);
    }

    public static QueryBuilder fromDescribe(Model model)
    {
	return fromResource(model.createResource(SP.Describe));
    }

    public static QueryBuilder fromDescribe(String uri, Model model)
    {
	if (uri == null) throw new IllegalArgumentException("DESCRIBE URI cannot be null");
	
	return fromDescribe(model.createResource(uri));
    }

    public static QueryBuilder fromDescribe(Resource resultNode)
    {
	if (resultNode == null) throw new IllegalArgumentException("DESCRIBE resource cannot be null");
	
	if (resultNode.canAs(RDFList.class))
	    return fromDescribe(resultNode.as(RDFList.class));
	else
	    return fromDescribe(resultNode.getModel().createList(new RDFNode[]{resultNode}));
    }

    public static QueryBuilder fromDescribe(RDFList resultNodes)
    {
	if (resultNodes == null) throw new IllegalArgumentException("DESCRIBE resource list cannot be null");

	return fromResource(resultNodes.getModel().createResource(SP.Describe).
		addProperty(SP.resultNodes, resultNodes));
    }

    public QueryBuilder where(Resource element)
    {
	if (element == null) throw new IllegalArgumentException("WHERE element cannot be null");

	return where(SPINFactory.asElement(element));
    }
    
    public QueryBuilder where(Element element)
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
	
    public SelectBuilder getSubSelectBuilder()
    {
	setSubSelect(null); // clear any previously found sub-SELECTs
	
	if (getWhere() != null)
	{
	    ElementWalker walker = new ElementWalker(getElementVisitor(), null);
	    walker.visit(getWhere());
	    
	    if (getSubSelect() != null)
	    {
		if (log.isTraceEnabled()) log.trace("Found sub-SELECT: {}", getSubSelect());
		return SelectBuilder.fromSelect(getSubSelect());
	    }
	}
	
	return null;
    }
    
    public QueryBuilder subQuery(Select select)
    {
	if (select == null) throw new IllegalArgumentException("SELECT sub-query cannot be null");
	
	SubQuery subQuery = SPINFactory.createSubQuery(getModel(), select);
	if (log.isTraceEnabled()) log.trace("SubQuery: {}", subQuery);
	
	getModel().add(select.getModel());

	return where(subQuery);
    }

    public QueryBuilder optional(Resource optional)
    {
	return where(optional);
    }

    public QueryBuilder optional(Optional optional)
    {
	return where(optional);
    }

    public QueryBuilder optional(TriplePattern pattern)
    {
	if (pattern == null) throw new IllegalArgumentException("OPTIONAL pattern cannot be null");

	return where(SPINFactory.createOptional(getModel(),
		SPINFactory.createElementList(getModel(), new Element[]{pattern})));
    }

    public QueryBuilder optional(Resource subject, Resource predicate, RDFNode object)
    {
	if (subject == null) throw new IllegalArgumentException("OPTIONAL subject cannot be null");
	if (predicate == null) throw new IllegalArgumentException("OPTIONAL predicate cannot be null");
	if (object == null) throw new IllegalArgumentException("OPTIONAL object cannot be null");

	return optional(SPINFactory.createTriplePattern(getModel(), subject, predicate, object));
    }

    public QueryBuilder filter(Filter filter)
    {
	if (filter == null) throw new IllegalArgumentException("FILTER cannot be null");
	
	return where(filter);
    }

    public QueryBuilder filter(String varName, String pattern)
    {
	return filter(SPINFactory.createVariable(getModel(), varName), pattern, "i");
    }

    public QueryBuilder filter(Variable var, String pattern)
    {
	return filter(var, pattern, "i");
    }

    public QueryBuilder filter(String varName, String pattern, String flags)
    {
	return filter(SPINFactory.createVariable(getModel(), varName), pattern, flags);
    }

    public QueryBuilder filter(Variable var, String pattern, String flags)
    {
	if (var == null) throw new IllegalArgumentException("FILTER variable name cannot be null");
	if (pattern == null) throw new IllegalArgumentException("regex() match string cannot be null");

	if (!isWhereVariable(var))
	{
	    if (log.isErrorEnabled()) log.error("Variable var: {} not in the WHERE pattern", var);
	    throw new IllegalArgumentException("Cannot ORDER BY variable '" + var + "' that is not specified in the WHERE pattern");
	}
        
	if (log.isTraceEnabled()) log.trace("Setting FILTER regex(str({}), \"{}\")", var, pattern); // flags?
	
	Resource strExpr = getModel().createResource().
		addProperty(RDF.type, SP.getArgProperty("str")).
		addProperty(SP.arg1, var);
	
	Resource regexExpr = getModel().createResource().
		addProperty(RDF.type, SP.regex).
		addProperty(SP.arg1, strExpr).
		addLiteral(SP.arg2, getModel().createLiteral(pattern)).
		addLiteral(SP.arg3, getModel().createLiteral(flags));

	return filter(SPINFactory.createFilter(getModel(), regexExpr));
    }

    // method taken from com.sun.org.apache.xerces.internal.impl.xpath.regex.REUtil
    public String quoteRegexMeta(String literal)
    {
        int len = literal.length();
        StringBuffer buffer = null;
        for (int i = 0;  i < len;  i ++) {
            int ch = literal.charAt(i);
            if (".*+?{[()|\\^$".indexOf(ch) >= 0) {
                if (buffer == null) {
                    buffer = new StringBuffer(i+(len-i)*2);
                    if (i > 0)  buffer.append(literal.substring(0, i));
                }
                buffer.append((char)'\\');
                buffer.append((char)ch);
            } else if (buffer != null)
                buffer.append((char)ch);
        }
        return buffer != null ? buffer.toString() : literal;
    }

    public QueryBuilder filter(Variable var, Locale locale)
    {
	if (var == null) throw new IllegalArgumentException("FILTER variable name cannot be null");
	if (locale == null) throw new IllegalArgumentException("Locale cannot be null");
	
	if (log.isTraceEnabled()) log.trace("Setting FILTER (LANG({}) = \"{}\")", var, locale.toLanguageTag());
	
	Resource langExpr = getModel().createResource().
		addProperty(RDF.type, SP.getArgProperty("lang")).
		addProperty(SP.arg1, var);
	
	Resource eqExpr = getModel().createResource().
		addProperty(RDF.type, SP.eq).
		addProperty(SP.arg1, langExpr).
		addLiteral(SP.arg2, getModel().createLiteral(locale.toLanguageTag()));

	return filter(SPINFactory.createFilter(getModel(), eqExpr));
    }

    public QueryBuilder filter(String varName, Locale locale)
    {
	if (varName == null) throw new IllegalArgumentException("FILTER variable name cannot be null");
	if (locale == null) throw new IllegalArgumentException("Locale cannot be null");
	
	return filter(SPINFactory.createVariable(getModel(), varName), locale);
    }

    public QueryBuilder filter(Variable var, RDFList resources)
    {
	if (var == null) throw new IllegalArgumentException("FILTER variable cannot be null");
	if (resources == null) throw new IllegalArgumentException("FILTER resource list cannot be null");

	if (log.isTraceEnabled()) log.trace("Setting FILTER param: {}", resources.getModel());
	
	return filter(SPINFactory.createFilter(getModel(), createFilterExpression(var, resources)));
    }

    private Resource createFilterExpression(Variable var, RDFList resources)
    {
	Resource eqExpr = getModel().createResource().
		addProperty(RDF.type, SP.eq).
		addProperty(SP.arg1, var).
		addProperty(SP.arg2, resources.getHead());

	if (resources.getTail().isEmpty()) // no more resources in list
	    return eqExpr;
	else
	    // more resources follow - join recursively with current value using || (or)
	    return getModel().createResource().
		addProperty(RDF.type, SP.getArgProperty("or")).
		addProperty(SP.arg1, eqExpr).
		addProperty(SP.arg2, createFilterExpression(var, resources.getTail()));
    }
    
    public QueryBuilder filter(String varName, RDFList resources)
    {
	if (varName == null) throw new IllegalArgumentException("FILTER variable name cannot be null");
	
	return filter(SPINFactory.createVariable(getModel(), varName), resources);
    }

    public QueryBuilder replaceFilter(String varName, RDFList resources)
    {
	if (log.isTraceEnabled()) log.trace("Replacing FILTER");

	while (findFilter(getWhere()) != null) // iterator.remove() doesn't work
	{
	    RDFNode filter = findFilter(getWhere());
	    if (log.isTraceEnabled()) log.trace("Removing FILTER: {}", filter);
	    getWhere().remove(filter);
	}
	
	return filter(varName, resources);
    }

    private RDFNode findFilter(ElementList list)
    {
	Iterator<RDFNode> it = list.iterator();

	while (it.hasNext())
	{
	    RDFNode node = it.next();
	    // if (SPINFactory.asExpression(node) instanceof Filter)
	    if (node.isResource() && node.asResource().hasProperty(RDF.type, SP.Filter))
		return node;
	}
	
	return null;
    }
    
    public QueryBuilder replaceVar(String varName, String uri)
    {
	if (varName == null) throw new IllegalArgumentException("Variable name cannot be null");
	if (uri == null) throw new IllegalArgumentException("URI value cannot be null");

	if (log.isTraceEnabled()) log.trace("Replacing variable ?{} with URI: {}", varName, uri);
	    
	Resource var = getVarByName(varName);
	var.removeAll(SP.varName);
	ResourceUtils.renameResource(var, uri);

	return this;
    }
    
    public QueryBuilder replaceVar(String varName, Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
	
	return replaceVar(varName, resource.getURI());
    }

    protected Resource getVarByName(String name)
    {
	ResIterator it = getModel().listResourcesWithProperty(SP.varName, name);
	
	try
	{
	    if (it.hasNext()) return it.nextResource();
	    return null;
	}
	finally
	{
	    it.close();
	}
    }
    
    public Query build()
    {
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(getQuery());
	
	// generate SPARQL query string
	removeAll(SP.text)
	    .addLiteral(SP.text, getModel().createTypedLiteral(arqQuery.toString()));
	
	return arqQuery;
    }

    /*
    protected boolean isWhereVariable(Variable var)
    {
	return new ContainsVarChecker(var).checkDepth(this) != null;
    }
    */
    
    protected boolean isWhereVariable(Variable var)
    {
	ResIterator it = ResourceUtils.reachableClosure(getWhere()).
	    listSubjectsWithProperty(SP.varName, var.getName());
	
	try
	{
	    return it.hasNext();
	}
	finally
	{
	    it.close();
	}
    }

    protected Select getSubSelect()
    {
	return subSelect;
    }

    protected void setSubSelect(Select subSelect)
    {
	this.subSelect = subSelect;
    }

    public ElementVisitor getElementVisitor()
    {
	return elementVisitor;
    }

    @Override
    public List<String> getFrom()
    {
	return getQuery().getFrom();
    }

    @Override
    public List<String> getFromNamed()
    {
	return getQuery().getFromNamed();
    }

    @Override
    public List<Element> getWhereElements()
    {
	return getQuery().getWhereElements();
    }

    @Override
    public ElementList getWhere()
    {
	return getQuery().getWhere();
    }

    @Override
    public String getComment()
    {
	return getQuery().getComment();
    }

    @Override
    public void print(PrintContext pc)
    {
	getQuery().print(pc);
    }

    @Override
    public AnonId getId()
    {
	return getQuery().getId();
    }

    @Override
    public Resource inModel(Model model)
    {
	return getQuery().inModel(model);
    }

    @Override
    public boolean hasURI(String string)
    {
	return getQuery().hasURI(string);
    }

    @Override
    public String getURI()
    {
	return getQuery().getURI();
    }

    @Override
    public String getNameSpace()
    {
	return getQuery().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
	return getQuery().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property prprt)
    {
	return getQuery().getRequiredProperty(prprt);
    }

    @Override
    public Statement getProperty(Property prprt)
    {
	return getQuery().getProperty(prprt);
    }

    @Override
    public StmtIterator listProperties(Property prprt)
    {
	return getQuery().listProperties(prprt);
    }

    @Override
    public StmtIterator listProperties()
    {
	return getQuery().listProperties();
    }

    @Override
    public Resource addLiteral(Property prprt, boolean bln)
    {
	return getQuery().addLiteral(prprt, bln);
    }

    @Override
    public Resource addLiteral(Property prprt, long l)
    {
	return getQuery().addLiteral(prprt, l);
    }

    @Override
    public Resource addLiteral(Property prprt, char c)
    {
	return getQuery().addLiteral(prprt, c);
    }

    @Override
    public Resource addLiteral(Property prprt, double d)
    {
	return getQuery().addLiteral(prprt, d);
    }

    @Override
    public Resource addLiteral(Property prprt, float f)
    {
	return getQuery().addLiteral(prprt, f);
    }

    @Override
    public Resource addLiteral(Property prprt, Object o)
    {
	return getQuery().addLiteral(prprt, o);
    }

    @Override
    public Resource addLiteral(Property prprt, Literal ltrl)
    {
	return getQuery().addLiteral(prprt, ltrl);
    }

    @Override
    public Resource addProperty(Property prprt, String string)
    {
	return getQuery().addProperty(prprt, string);
    }

    @Override
    public Resource addProperty(Property prprt, String string, String string1)
    {
	return getQuery().addProperty(prprt, string, string1);
    }

    @Override
    public Resource addProperty(Property prprt, String string, RDFDatatype rdfd)
    {
	return getQuery().addProperty(prprt, prprt);
    }

    @Override
    public Resource addProperty(Property prprt, RDFNode rdfn)
    {
	return getQuery().addProperty(prprt, rdfn);
    }

    @Override
    public boolean hasProperty(Property prprt)
    {
	return getQuery().hasProperty(prprt);
    }

    @Override
    public boolean hasLiteral(Property prprt, boolean bln)
    {
	return getQuery().hasLiteral(prprt, bln);
    }

    @Override
    public boolean hasLiteral(Property prprt, long l)
    {
	return getQuery().hasLiteral(prprt, l);
    }

    @Override
    public boolean hasLiteral(Property prprt, char c)
    {
	return getQuery().hasLiteral(prprt, c);
    }

    @Override
    public boolean hasLiteral(Property prprt, double d)
    {
	return getQuery().hasLiteral(prprt, d);
    }

    @Override
    public boolean hasLiteral(Property prprt, float f)
    {
	return getQuery().hasLiteral(prprt, f);
    }

    @Override
    public boolean hasLiteral(Property prprt, Object o)
    {
	return getQuery().hasLiteral(prprt, o);
    }

    @Override
    public boolean hasProperty(Property prprt, String string)
    {
	return getQuery().hasProperty(prprt, string);
    }

    @Override
    public boolean hasProperty(Property prprt, String string, String string1)
    {
	return getQuery().hasProperty(prprt, string, string1);
    }

    @Override
    public boolean hasProperty(Property prprt, RDFNode rdfn)
    {
	return getQuery().hasLiteral(prprt, rdfn);
    }

    @Override
    public Resource removeProperties()
    {
	return getQuery().removeProperties();
    }

    @Override
    public Resource removeAll(Property prprt)
    {
	return getQuery().removeAll(prprt);
    }

    @Override
    public Resource begin()
    {
	return getQuery().begin();
    }

    @Override
    public Resource abort()
    {
	return getQuery().abort();
    }

    @Override
    public Resource commit()
    {
	return getQuery().commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property prprt)
    {
	return getQuery().getPropertyResourceValue(prprt);
    }

    @Override
    public boolean isAnon()
    {
	return getQuery().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
	return getQuery().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
	return getQuery().isURIResource();
    }

    @Override
    public boolean isResource()
    {
	return getQuery().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> type)
    {
	return getQuery().as(type);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> type)
    {
	return getQuery().canAs(type);
    }

    @Override
    public Model getModel()
    {
	return getQuery().getModel();
    }

    @Override
    public Object visitWith(RDFVisitor rdfv)
    {
	return getQuery().visitWith(rdfv);
    }

    @Override
    public Resource asResource()
    {
	return getQuery().asResource();
    }

    @Override
    public Literal asLiteral()
    {
	return getQuery().asLiteral();
    }

    @Override
    public Node asNode()
    {
	return getQuery().asNode();
    }

    @Override
    public String toString()
    {
	return getQuery().toString();
    }

    @Override
    public Values getValues()
    {
        return getQuery().getValues();
    }
    
}