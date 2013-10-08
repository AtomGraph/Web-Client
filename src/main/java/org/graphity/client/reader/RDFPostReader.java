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

package org.graphity.client.reader;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.util.ReaderWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads RDF from RDF/POST-encoded request body
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://www.lsrn.org/semweb/rdfpost.html">RDF/POST Encoding for RDF</a>
 * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html">Model</a>
 * @see <a href="http://jsr311.java.net/nonav/javadoc/javax/ws/rs/ext/MessageBodyReader.html">MessageBodyReader</a>
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RDFPostReader implements MessageBodyReader<Model>
{
    private static final Logger log = LoggerFactory.getLogger(RDFPostReader.class);
    
    @Context private HttpContext httpContext;
    private List<String> keys = null, values = null;

    protected void initKeysValues(String body, String charsetName)
    {
	keys = new ArrayList<String>();
	values = new ArrayList<String>();

	String[] params = body.split("&");

	for (String param : params)
	{
	    if (log.isTraceEnabled()) log.trace("Parameter: {}", param);
	    
	    String[] array = param.split("=");
	    String key = null;
	    String value = null;

	    try
	    {
		key = URLDecoder.decode(array[0], charsetName);
		if (array.length > 1) value = URLDecoder.decode(array[1], charsetName);
	    } catch (UnsupportedEncodingException ex)
	    {
		if (log.isWarnEnabled()) log.warn("Unsupported encoding", ex);
	    }

            if (value != null) // && key != null
            {
                keys.add(key);
                values.add(value);
            }
	}
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
	return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
	// getHttpContext().getRequest().getFormParameters() returns same info, but ordering is lost	
	return parse(getHttpContext().getRequest().getEntity(String.class), ReaderWriter.getCharset(mediaType).name());
    }

    public Model parse(String request, String encoding)
    {
	initKeysValues(request, encoding);
	return parse(getKeys(), getValues());
    }
    
    public Model parse(List<String> k, List<String> v)
    {
	Model model = ModelFactory.createDefaultModel();

	Resource subject = null;
	Property property = null;
	RDFNode object = null;

	for (int i = 0; i < k.size(); i++)
	{
	    switch (k.get(i))
	    {
		case "v":
		    model.setNsPrefix("", v.get(i)); // default namespace
		    break;
		case "n":
		    if (i + 1 < k.size() && k.get(i + 1).equals("v")) // if followed by "v" (if not out of bounds)
		    {
			model.setNsPrefix(v.get(i), v.get(i + 1)); // namespace with prefix
			i++; // skip the following "v"
		    }
		    break;
		    
		case "sb":
		    subject = model.createResource(new AnonId(v.get(i))); // blank node
		    property = null;
		    object = null;
		    break;
		case "su":
		    subject = model.createResource(v.get(i)); // full URI
		    property = null;
		    object = null;
		    break;
		case "sv":
		    subject = model.createResource(model.getNsPrefixURI("") + v.get(i)); // default namespace
		    property = null;
		    object = null;
		    break;
		case "sn":
		    if (i + 1 < k.size() && k.get(i + 1).equals("sv")) // if followed by "sv" (if not out of bounds)
		    {
			subject = model.createResource(model.getNsPrefixURI(v.get(i)) + v.get(i + 1)); // ns prefix + local name
			property = null;
			object = null;
			i++; // skip the following "sv"
		    }
		    break;

		case "pu":
		    property = model.createProperty(v.get(i));
		    object = null;
		    break;
		case "pv":
		    property = model.createProperty(model.getNsPrefixURI(""), v.get(i));
		    object = null;
		    break;
		case "pn":
		    if (i + 1 < k.size() && k.get(i + 1).equals("pv")) // followed by "pv" (if not out of bounds)
		    {
			property = model.createProperty(model.getNsPrefixURI(v.get(i)) + v.get(i + 1)); // ns prefix + local name
			object = null;
			i++; // skip the following "pv"
		    }
		    break;

		case "ob":
		    object = model.createResource(new AnonId(v.get(i))); // blank node
		    break;
		case "ou":
		    object = model.createResource(v.get(i)); // full URI
		    break;
		case "ov":
		    object = model.createResource(model.getNsPrefixURI("") + v.get(i)); // default namespace
		    break;
		case "on":
		    if (i + 1 < k.size() && k.get(i + 1).equals("ov")) // followed by "ov" (if not out of bounds)
		    {
			object = model.createResource(model.getNsPrefixURI(v.get(i)) + v.get(i + 1)); // ns prefix + local name
			i++; // skip the following "ov"
		    }
		    break;
		case "ol":
		    if (i + 1 < k.size()) // check if not out of bounds
			switch (k.get(i + 1))
			{
			    case "lt":
				object = model.createTypedLiteral(v.get(i), new BaseDatatype(v.get(i + 1))); // typed literal (value+datatype)
				i++; // skip the following "lt"
				break;
			    case "ll":
				object = model.createLiteral(v.get(i), v.get(i + 1)); // literal with language (value+lang)
				i++; // skip the following "ll"
				break;
			    default:
				object = model.createLiteral(v.get(i)); // plain literal (if not followed by lang or datatype)
				break;
			}
		    else
			object = model.createLiteral(v.get(i)); // plain literal
		    break;
		case "lt":
		    if (i + 1 < k.size() && k.get(i + 1).equals("ol")) // followed by "ol" (if not out of bounds)
		    {
			object = model.createTypedLiteral(v.get(i + 1), new BaseDatatype(v.get(i))); // typed literal (datatype+value)
			i++; // skip the following "ol"
		    }
		    break;
		case "ll":
		    if (i + 1 < k.size() && k.get(i + 1).equals("ol")) // followed by "ol" (if not out of bounds)
		    {
			model.createLiteral(v.get(i + 1), v.get(i)); // literal with language (lang+value)
			i++; // skip the following "ol"
		    }
		    break;
	    }

	    if (subject != null && property != null && object != null)
		model.add(model.createStatement(subject, property, object));
	}

	return model;
    }

    public HttpContext getHttpContext()
    {
	return httpContext;
    }

    public List<String> getKeys()
    {
	return keys;
    }

    public List<String> getValues()
    {
	return values;
    }
    
}