/*
 * Copyright (C) 2014 Martynas
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

package org.graphity.processor.model;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.graphity.processor.vocabulary.GP;

/**
 *
 * @author Martynas
 */
public class ApplicationBase implements Application
{
    private final Resource resource;
    
    public ApplicationBase(Resource resource)
    {
	if (resource == null) throw new IllegalArgumentException("Resource cannot be null");
        this.resource = resource;
    }

    @Override
    public Resource getBase()
    {
        return getPropertyResourceValue(GP.base);
    }

    @Override
    public Resource getService()
    {
        return getPropertyResourceValue(GP.service);
    }

    public Resource getResource()
    {
        return resource;
    }

    @Override
    public AnonId getId()
    {
        return getResource().getId();
    }

    @Override
    public Resource inModel(Model model)
    {
        return getResource().inModel(model);
    }

    @Override
    public boolean hasURI(String string)
    {
        return getResource().hasURI(string);
    }

    @Override
    public String getURI()
    {
        return getResource().getURI();
    }

    @Override
    public String getNameSpace()
    {
        return getResource().getNameSpace();
    }

    @Override
    public String getLocalName()
    {
        return getResource().getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property prprt) 
    {
        return getResource().getRequiredProperty(prprt);
    }

    @Override
    public Statement getProperty(Property prprt)
    {
        return getResource().getProperty(prprt);
    }

    @Override
    public StmtIterator listProperties(Property prprt)
    {
        return getResource().listProperties(prprt);
    }

    @Override
    public StmtIterator listProperties()
    {
        return getResource().listProperties();
    }

    @Override
    public Resource addLiteral(Property prprt, boolean bln)
    {
        return getResource().addLiteral(prprt, bln);
    }

    @Override
    public Resource addLiteral(Property prprt, long l)
    {
        return getResource().addLiteral(prprt, l);
    }

    @Override
    public Resource addLiteral(Property prprt, char c)
    {
        return getResource().addLiteral(prprt, c);
    }

    @Override
    public Resource addLiteral(Property prprt, double d)
    {
        return getResource().addLiteral(prprt, d);
    }

    @Override
    public Resource addLiteral(Property prprt, float f)
    {
        return getResource().addLiteral(prprt, f);
    }

    @Override
    public Resource addLiteral(Property prprt, Object o)
    {
        return getResource().addLiteral(prprt, o);
    }

    @Override
    public Resource addLiteral(Property prprt, Literal ltrl)
    {
        return getResource().addLiteral(prprt, ltrl);
    }

    @Override
    public Resource addProperty(Property prprt, String string)
    {
        return getResource().addProperty(prprt, string);
    }

    @Override
    public Resource addProperty(Property prprt, String string, String string1)
    {
        return getResource().addProperty(prprt, string, string1);
    }

    @Override
    public Resource addProperty(Property prprt, String string, RDFDatatype rdfd)
    {
        return getResource().addProperty(prprt, string, rdfd);
    }

    @Override
    public Resource addProperty(Property prprt, RDFNode rdfn)
    {
        return getResource().addProperty(prprt, rdfn);
    }

    @Override
    public boolean hasProperty(Property prprt)
    {
        return getResource().hasProperty(prprt);
    }

    @Override
    public boolean hasLiteral(Property prprt, boolean bln)
    {
        return getResource().hasLiteral(prprt, bln);
    }

    @Override
    public boolean hasLiteral(Property prprt, long l)
    {
        return getResource().hasLiteral(prprt, l);
    }

    @Override
    public boolean hasLiteral(Property prprt, char c)
    {
        return getResource().hasLiteral(prprt, c);
    }

    @Override
    public boolean hasLiteral(Property prprt, double d)
    {
        return getResource().hasLiteral(prprt, d);
    }

    @Override
    public boolean hasLiteral(Property prprt, float f)
    {
        return getResource().hasLiteral(prprt, f);
    }

    @Override
    public boolean hasLiteral(Property prprt, Object o)
    {
        return getResource().hasLiteral(prprt, o);
    }

    @Override
    public boolean hasProperty(Property prprt, String string)
    {
        return getResource().hasProperty(prprt, string);
    }

    @Override
    public boolean hasProperty(Property prprt, String string, String string1)
    {
        return getResource().hasProperty(prprt, string, string1);
    }

    @Override
    public boolean hasProperty(Property prprt, RDFNode rdfn)
    {
        return getResource().hasProperty(prprt, rdfn);
    }

    @Override
    public Resource removeProperties()
    {
        return getResource().removeProperties();
    }

    @Override
    public Resource removeAll(Property prprt)
    {
        return getResource().removeAll(prprt);
    }

    @Override
    public Resource begin()
    {
        return getResource().begin();
    }

    @Override
    public Resource abort()
    {
        return getResource().abort();
    }

    @Override
    public Resource commit()
    {
        return getResource().commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property prprt)
    {
        return getResource().getPropertyResourceValue(prprt);
    }

    @Override
    public boolean isAnon()
    {
        return getResource().isAnon();
    }

    @Override
    public boolean isLiteral()
    {
        return getResource().isLiteral();
    }

    @Override
    public boolean isURIResource()
    {
        return getResource().isURIResource();
    }

    @Override
    public boolean isResource()
    {
        return getResource().isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> type)
    {
        return getResource().as(type);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> type)
    {
        return getResource().canAs(type);
    }

    @Override
    public Model getModel()
    {
        return getResource().getModel();
    }

    @Override
    public Object visitWith(RDFVisitor rdfv)
    {
        return getResource().visitWith(rdfv);
    }

    @Override
    public Resource asResource()
    {
        return getResource().asResource();
    }

    @Override
    public Literal asLiteral()
    {
        return getResource().asLiteral();
    }

    @Override
    public Node asNode()
    {
        return getResource().asNode();
    }
    
}