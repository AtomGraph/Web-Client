/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.provider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.graphity.MediaType;
import org.openjena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
@Provider
@Produces({MediaType.APPLICATION_RDF_XML, MediaType.TEXT_TURTLE})
@Consumes({MediaType.APPLICATION_RDF_XML, MediaType.TEXT_TURTLE, MediaType.TEXT_PLAIN})
public class ModelProvider implements MessageBodyReader<Model>, MessageBodyWriter<Model>
{
    public static final Map<javax.ws.rs.core.MediaType, Lang> LANGS = new HashMap<javax.ws.rs.core.MediaType, Lang>();
    static
    {
        LANGS.put(MediaType.APPLICATION_RDF_XML_TYPE, Lang.RDFXML);
        LANGS.put(MediaType.TEXT_TURTLE_TYPE, Lang.TURTLE);
        LANGS.put(MediaType.TEXT_PLAIN_TYPE, Lang.TURTLE);
    }    
    private static final Logger log = LoggerFactory.getLogger(ModelProvider.class) ;

    // READER
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType)
    {
        return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	log.trace("HTTP Headers: {}", httpHeaders);
	
	Model model = ModelFactory.createDefaultModel();
	
	String syntax = null;
	Lang lang = langFromContentType(mediaType);
	if (lang != null) syntax = lang.getName();
	log.debug("Syntax used to read Model: {}", syntax);

	// extract base URI from httpHeaders?
	return model.read(entityStream, null, syntax);
    }
    
    public static Lang langFromContentType(javax.ws.rs.core.MediaType mediaType)
    { 
        if (mediaType == null) return null;
	
        return LANGS.get(mediaType) ;
    }

    // WRITER
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType)
    {
        return Model.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Model model, Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType)
    {
	return -1;
    }

    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
	String syntax = null;
	Lang lang = langFromContentType(mediaType);
	if (lang != null) syntax = lang.getName();
	log.debug("Syntax used to load Model: {}", syntax);

	model.write(entityStream, syntax);
    }
    
}
