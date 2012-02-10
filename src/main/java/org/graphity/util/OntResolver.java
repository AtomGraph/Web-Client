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

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Iterator;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class OntResolver implements URIResolver
{
    private static final Logger log = LoggerFactory.getLogger(OntResolver.class);
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);
	String uri = URI.create(base).resolve(href).toString();
	//log.debug("Resolved absolute URI: {}", uri);
	
	Model model = OntDocumentManager.getInstance().getModel(uri);
	if (model == null)
	{
	    // first stripping the URI to find ontology in the cache
	    Iterator<String> it = OntDocumentManager.getInstance().listDocuments();
	    while (it.hasNext())
	    {
		String docURI = it.next();
		log.debug("URI listed in OntDocumentManager: {}", docURI);
		if (uri.startsWith(removeFragmentId(docURI)))
		{
		    log.debug("Found Document URI: {} for URI: {}", docURI, uri);
		    return resolve(docURI, base);
		}
	    }
		    
	    log.debug("Could not resolve URI: {}", uri);
	    //return null;
	    model = ModelFactory.createDefaultModel();
	}
	//else
	{
	    log.debug("Number of Model stmts read: {} from URI: {}", model.size(), uri);

	    ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	    model.write(stream);

	    log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	    return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
	}
    }

    public static String removeFragmentId(String uri)
    {
	return UriBuilder.fromUri(uri).fragment(null).build().toString();
    }
}
