/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.util;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.ByteArrayOutputStream;
import org.openjena.riot.WebContent;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class SPARULAdapter // implements org.openjena.fuseki.DatasetAccessor
{
    private String endpoint = null;
    
    public SPARULAdapter(String endpoint)
    {
	this.endpoint = endpoint;
    }
    
    public void add(Model data)
    {
	
    }
    
    public void add(String graphUri, Model data)
    {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	data.write(baos, WebContent.langNTriples);

	//UpdateDataInsert;
	// http://www.w3.org/TR/sparql11-update/#insertData
	UpdateRequest request = UpdateFactory.create("CREATE GRAPH <" + graphUri + ">", Syntax.syntaxSPARQL_11).
	   add("INSERT DATA { GRAPH <" + graphUri + "> {"
	    + baos.toString() +
	    "} }");

	UpdateProcessRemote process = new UpdateProcessRemote(request, endpoint);
	//process.setBasicAuthentication(getServiceApiKey(), "X".toCharArray());
	process.execute();
    }
    
    public boolean containsModel(String graphURI)
    {
	return false;
    }
    
}
