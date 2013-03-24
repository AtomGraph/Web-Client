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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.vocabulary.SP;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
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
	    throw new IllegalArgumentException("ModifyBuilder Resource must be a SPIN INSERT/DELETE Query");

	return fromModify((Modify)update);
    }

    /*
    public static ModifyBuilder newInstance()
    {
	return fromModify(ModelFactory.createDefaultModel());
    }
    */
    
    public static ModifyBuilder fromModify(Model model)
    {
	return fromResource(model.createResource().
	    addProperty(RDF.type, SP.Modify));
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