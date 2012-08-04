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
 * Reads RDF/POST encoding http://www.lsrn.org/semweb/rdfpost.html
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RDFPostReader implements MessageBodyReader<Model>
{
    private static final Logger log = LoggerFactory.getLogger(RDFPostReader.class) ;
    
    @Context private HttpContext hrc;
    private List<String> keys = null; //new ArrayList<String>();
    private List<String> values = null; // new ArrayList<String>();

    protected void initKeysValues(String body, String charsetName)
    {
	keys = new ArrayList<String>();
	values = new ArrayList<String>();

	String[] params = body.split("&");

	for (String param : params)
	{
	    log.trace("Parameter: {}", param);
	    
	    String[] array = param.split("=");
	    String key = null;
	    String value = null;

	    try
	    {
		key = URLDecoder.decode(array[0], charsetName);
		if (array.length > 1) value = URLDecoder.decode(array[1], charsetName);
	    } catch (UnsupportedEncodingException ex)
	    {
		log.warn("Unsupported encoding", ex);
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
	// hrc.getRequest().getFormParameters() returns same info, but ordering is lost	
	initKeysValues(hrc.getRequest().getEntity(String.class), ReaderWriter.getCharset(mediaType).name());
	Model model = ModelFactory.createDefaultModel();

	Resource subject = null;
	Property property = null;
	RDFNode object = null;

	for (int i = 0; i < keys.size(); i++)
	{
	    if (keys.get(i).equals("v")) model.setNsPrefix("", values.get(i)); // default namespace
	    if (keys.get(i).equals("n") && keys.get(i + 1).equals("v"))
            {
                model.setNsPrefix(values.get(i), values.get(i + 1));
                i++;
            } // namespace with prefix
	    if (keys.get(i).equals("sb") || keys.get(i).equals("su") || keys.get(i).equals("sv") || keys.get(i).equals("sn"))
	    {
		property = null; object = null;
		if (keys.get(i).equals("sb")) subject = model.createResource(new AnonId(values.get(i))); // blank node
		if (keys.get(i).equals("su")) subject = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("sv")) subject = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("sn") && keys.get(i + 1).equals("sv"))
                {
                    subject = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
	    }
	    if (keys.get(i).equals("pu") || keys.get(i).equals("pv") || keys.get(i).equals("pn") || keys.get(i).equals("sn"))
	    {
		object = null;
		if (keys.get(i).equals("pu")) property = model.createProperty(values.get(i));
		if (keys.get(i).equals("pv")) property = model.createProperty(model.getNsPrefixURI(""), values.get(i));
		if (keys.get(i).equals("pn") && keys.get(i + 1).equals("pv"))
                {
                    property = model.createProperty(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
	    }
	    if (keys.get(i).equals("ob") || keys.get(i).equals("ou") || keys.get(i).equals("ov") || keys.get(i).equals("on") || keys.get(i).equals("ol"))
	    {
		if (keys.get(i).equals("ob")) object = model.createResource(new AnonId(values.get(i))); // blank node
		if (keys.get(i).equals("ou")) object = model.createResource(values.get(i)); // full URI
		if (keys.get(i).equals("ov")) object = model.createResource(model.getNsPrefixURI("") + values.get(i)); // default namespace
		if (keys.get(i).equals("on") && keys.get(i + 1).equals("ov"))
                {
                    object = model.createResource(model.getNsPrefixURI(values.get(i)) + values.get(i + 1)); // ns prefix + local name
                    i++;
                }
		if (keys.get(i).equals("ol")) object = model.createTypedLiteral(values.get(i)); // literal

	    }

	    if (subject != null && property != null && object != null)
	    {
		model.add(model.createStatement(subject, property, object));
	    }
	}

	return model;
    }
    
}
