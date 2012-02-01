/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity;

import java.util.Map;

/**
 *
 * @author Pumba
 */
public class MediaType extends javax.ws.rs.core.MediaType
{
    /** "application/rdf+xml" */
    public final static String APPLICATION_RDF_XML = "application/rdf+xml";
    /** "application/rdf+xml" */
    public final static MediaType APPLICATION_RDF_XML_TYPE = new MediaType("application","rdf+xml");

    /** "text/turtle" */
    public final static String TEXT_TURTLE = "text/turtle";
    /** "text/turtle" */
    public final static MediaType TEXT_TURTLE_TYPE = new MediaType("text","turtle");
    
    public MediaType(String type, String subtype, Map<String, String> parameters)
    {
	super(type, subtype, parameters);
    }

    public MediaType(String type, String subtype)
    {
        super(type,subtype);
    }

    public MediaType()
    {
        super();
    }

}
