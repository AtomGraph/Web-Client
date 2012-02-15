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
package org.graphity.util.manager;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class OntDataManager extends OntDocumentManager implements URIResolver
{
    private static OntDataManager s_instance = null;
    private static final Logger log = LoggerFactory.getLogger(OntDataManager.class);

    public static final List<String> IGNORED_EXT = new ArrayList<String>();
    static
    {
	IGNORED_EXT.add("html"); IGNORED_EXT.add("htm"); // GRDDL or <link> inspection could be used to analyzed HTML
	IGNORED_EXT.add("jpg");	IGNORED_EXT.add("gif");	IGNORED_EXT.add("png"); // binary image formats
	IGNORED_EXT.add("avi"); IGNORED_EXT.add("mpg"); IGNORED_EXT.add("wmv"); // binary video formats
	IGNORED_EXT.add("mp3"); IGNORED_EXT.add("wav"); // binary sound files
	IGNORED_EXT.add("zip"); IGNORED_EXT.add("rar"); // binary archives
	IGNORED_EXT.add("pdf"); IGNORED_EXT.add("ps"); IGNORED_EXT.add("doc"); // binary documents
	IGNORED_EXT.add("exe"); // binary executables
    }
    
    protected boolean resolvingUncached = true;
    
    public static OntDataManager getInstance()
    {
        if (s_instance == null) {
            s_instance = new OntDataManager();
        }
        return s_instance;
    }
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);
	String uri = URI.create(base).resolve(href).toString();
	//log.debug("CacheModels: {}", getCacheModels());
	
	if (isIgnored(uri))
	{
	    log.debug("URI ignored by file extension: {}", uri);
	    return getDefaultSource();
	}

	Model model = getModel(uri); // first look for a cached match
	if (model == null) // URI not cached, 
	{
	    log.debug("No cached Model for URI: {}", uri);
	    
	    String docURI = findDocumentURI(uri); // try to find and resolve its ontology
	    if (docURI != null)
		return resolve(docURI, base);
	    else
	    {
		if (resolvingUncached) // if true, can significantly slow down the transformation
		    try
		    {
			log.debug("Getting Ontology for URI: {}", uri);
			return getSource(getOntology(uri, OntModelSpec.OWL_MEM_RDFS_INF)); // load from web
		    }
		    catch (Exception ex)
		    {
			log.debug("Syntax error reading Model from URI: {}", uri, ex);
			return getDefaultSource(); // return empty Model
			//return null;
		    }
		else
		{
		    log.debug("Defaulting to empty Model for URI: {}", uri);
		    return getDefaultSource(); // return empty Model
		}
	    }
	}
	else
	{
	    log.debug("Cached Model for URI: {}", uri);
	    return getSource(model);
	}
    }

    protected Source getDefaultSource()
    {
	return getSource(ModelFactory.createDefaultModel());
    }
    
    protected Source getSource(Model model)
    {
	log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	model.write(stream);

	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));	
    }
    
    public boolean isIgnored(String filenameOrURI)
    {
	return IGNORED_EXT.contains(FileUtils.getFilenameExt(filenameOrURI));
    }
    
    public static String removeFragmentId(String uri)
    {
	return UriBuilder.fromUri(uri).fragment(null).build().toString();
    }

    public String findDocumentURI(String uri)
    {
	Iterator<String> it = listDocuments();
	while (it.hasNext())
	{
	    String docURI = it.next();
	    if (uri.startsWith(removeFragmentId(docURI)))
	    {
		log.debug("Found Document URI: {} for URI: {}", docURI, uri);
		return docURI;
	    }
	}
	
	return null;
    }
}
