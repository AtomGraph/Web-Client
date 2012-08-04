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

package org.graphity.ldp.provider;

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
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Produces({MediaType.APPLICATION_RDF_XML, MediaType.TEXT_TURTLE, MediaType.TEXT_PLAIN})
@Consumes({MediaType.APPLICATION_RDF_XML, MediaType.TEXT_TURTLE, MediaType.TEXT_PLAIN})
public class ModelProvider implements MessageBodyReader<Model>, MessageBodyWriter<Model>
{
    //private @Context UriInfo uriInfo = null;
    
    public static final Map<String, Lang> LANGS = new HashMap<String, Lang>();
    static
    {
        LANGS.put(MediaType.APPLICATION_RDF_XML, Lang.RDFXML);
        LANGS.put(MediaType.TEXT_TURTLE, Lang.TURTLE);
        LANGS.put(MediaType.TEXT_PLAIN, Lang.TURTLE);
    }    
    private static final Logger log = LoggerFactory.getLogger(ModelProvider.class);

    // READER
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType)
    {
        return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, javax.ws.rs.core.MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	log.trace("Reading model with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);
	//log.debug("Request URI: {}", uriInfo.getRequestUri());
	
	Model model = ModelFactory.createDefaultModel();
	
	String syntax = null;
	Lang lang = langFromMediaType(mediaType);
	if (lang != null) syntax = lang.getName();
	log.debug("Syntax used to read Model: {}", syntax);

	// extract base URI from httpHeaders?
	return model.read(entityStream, null, syntax);
    }
    
    public static Lang langFromMediaType(javax.ws.rs.core.MediaType mediaType)
    { 
        if (mediaType == null) return null;
	log.trace("langFromMediaType({}): {}", mediaType.getType() + "/" + mediaType.getSubtype(), LANGS.get(mediaType.getType() + "/" + mediaType.getSubtype()));
        return LANGS.get(mediaType.getType() + "/" + mediaType.getSubtype());
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
	log.trace("Writing Model with HTTP headers: {} MediaType: {}", httpHeaders, mediaType);
	String syntax = null;
	Lang lang = langFromMediaType(mediaType);
	if (lang != null) syntax = lang.getName();
	log.debug("Syntax used to load Model: {}", syntax);

	model.write(entityStream, syntax);
    }
    
}
