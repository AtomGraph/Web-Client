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

package org.graphity.manager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.MediaType;
import org.graphity.provider.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class DataManager extends FileManager implements URIResolver
{
    public static final Map<javax.ws.rs.core.MediaType, Double> QUALIFIED_TYPES;    
    static
    {
	Map<javax.ws.rs.core.MediaType, Double> typeMap = new HashMap<javax.ws.rs.core.MediaType, Double>();
	
	typeMap.put(MediaType.APPLICATION_RDF_XML_TYPE, null);
	typeMap.put(MediaType.TEXT_TURTLE_TYPE, 0.9);
	typeMap.put(MediaType.TEXT_PLAIN_TYPE, 0.7);
	typeMap.put(MediaType.APPLICATION_XML_TYPE, 0.5);
	
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }    

    private static DataManager s_instance = null;

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);

    private ClientConfig config = new DefaultClientConfig();

    public DataManager(LocationMapper _mapper)
    {
	super(_mapper);
	config.getClasses().add(ModelProvider.class);
    }

    public DataManager(FileManager filemanager)
    {
	super(filemanager);
	config.getClasses().add(ModelProvider.class);
    }

    public DataManager()
    {
	config.getClasses().add(ModelProvider.class);
    }
    
    public static DataManager get() {
        if (s_instance == null) {
            s_instance = new DataManager(FileManager.get());
	    log.debug("new DataManager({}): {}", FileManager.get(), s_instance);
        }
        return s_instance;
    }

    @Override
    public Model loadModel(String filenameOrURI)
    {
	// http://blogs.oracle.com/enterprisetechtips/entry/consuming_restful_web_services_with#regp
	if (hasCachedModel(filenameOrURI))
	{
	    log.debug("Model cache hit: " + filenameOrURI);
	    return getFromCache(filenameOrURI);
	}
	// Make sure we load from HTTP as the last resort, otherwise handle with FileManager
	if (!mapURI(filenameOrURI).equals(filenameOrURI))
	{
	    log.debug("loadModel({}) mapped to {} handled by FileManager", filenameOrURI, mapURI(filenameOrURI));
	    return super.loadModel(filenameOrURI);
	}

	log.trace("Loading Model from URI: {} with Accept header: {}", filenameOrURI, getAcceptHeader());
	Model m = Client.create(config).
		resource(filenameOrURI).
		header("Accept", getAcceptHeader()).
		get(Model.class);
	
	addCacheModel(filenameOrURI, m);
	
	return m;
    }
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);
	String uri = URI.create(base).resolve(href).toString();
	//log.debug("Resolved absolute URI: {}", uri);
	Model model = loadModel(uri);

	log.debug("Number of Model stmts read: {} from URI: {}", model.size(), uri);

	ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	model.write(stream);
	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);

	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
    }

    public static String removeFragmentId(String uri)
    {
	return UriBuilder.fromUri(uri).fragment(null).build().toString();
    }

    public static String getAcceptHeader()
    {
	String header = null;

	//for (Map.Entry<String, Double> type : getQualifiedTypes().entrySet())
	Iterator <Entry<javax.ws.rs.core.MediaType, Double>> it = QUALIFIED_TYPES.entrySet().iterator();
	while (it.hasNext())
	{
	    Entry<javax.ws.rs.core.MediaType, Double> type = it.next();
	    if (header == null) header = "";
	    
	    header += type.getKey();
	    if (type.getValue() != null) header += ";q=" + type.getValue();
	    
	    if (it.hasNext()) header += ",";
	}
	
	return header;
    }

    /*
    @Override
    public Model getFromCache(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return null; 
        return super.getFromCache(filenameOrURI) ;
    }
    
    @Override
    public boolean hasCachedModel(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return false ; 
        return super.hasCachedModel(filenameOrURI) ;
    }
    
    @Override
    // http://linuxsoftwareblog.com/?p=843
    public void addCacheModel(String uri, Model m)
    { 
        if ( getCachingModels() )
            super.addCacheModel(uri, m) ;
	
	
	Dataset ds = DatasetFactory.create();
	//ds.getNamedModel(uri).wr
		
	GraphStore graphStore = GraphStoreFactory.create(ds);
	//DatasetFactory.
	//UpdateFactory.
	//ds.getNamedModel(uri)
	//graphStore.
	//graphStore.ad
	//UpdateFactory.create().
	//Update data = new UpdateDataInsert();
	//ds.getNamedModel(uri)
    }
    */
    
}
