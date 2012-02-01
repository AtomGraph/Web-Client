/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.graphity.MediaType;
import org.graphity.provider.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pumba
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

    private static final Logger log = LoggerFactory.getLogger(DataManager.class);
    //sstatic DataManager instance = null;
    
    @Override
    public Model loadModel(String filenameOrURI)
    {
	// http://blogs.oracle.com/enterprisetechtips/entry/consuming_restful_web_services_with#regp
	
	ClientConfig config = new DefaultClientConfig();
	config.getClasses().add(ModelProvider.class);

	return Client.create(config).
		resource(filenameOrURI).
		header("Accept", getAcceptHeader()).
		get(Model.class);
    }
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);

	Model model = loadModel(href, base, null);
	log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	model.write(stream);
	
	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
    }

    public String getAcceptHeader()
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

}
