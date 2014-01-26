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

package org.graphity.processor.util;

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 *
 * @author Martynas
 */
public class OntModelUtils
{
    /**
     * Given an ontology class and ontology property, returns value of <code>owl:hasValue</code> restriction,
     * if one is present.
     * The ontology class must be a subclass of the restriction, and the property must be used as
     * <code>owl:onProperty</code>.
     * 
     * @param ontClass ontology class
     * @param property ontology property
     * @return RDF node or null, if not present
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/ontology/HasValueRestriction.html">Jena HasValueRestriction</a>
     */
    public static RDFNode getRestrictionHasValue(OntClass ontClass, Property property)
    {
	if (ontClass == null) throw new IllegalArgumentException("OntClass cannot be null");
	if (property == null) throw new IllegalArgumentException("OntProperty cannot be null");
	
	ExtendedIterator<OntClass> it = ontClass.listSuperClasses(true);
	
	try
	{
	    while (it.hasNext())
	    {
		OntClass superClass = it.next();
		if (superClass.canAs(HasValueRestriction.class))
		{
		    HasValueRestriction restriction = superClass.asRestriction().asHasValueRestriction();
		    if (restriction.getOnProperty().equals(property))
			return restriction.getHasValue();
		}
	    }
	    
	    return null;
	}
	finally
	{
	    it.close();
	}
    }

}
