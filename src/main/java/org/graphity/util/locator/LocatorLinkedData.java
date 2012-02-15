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

package org.graphity.util.locator;

import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.openjena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementing Linked Data publishing patterns
 * {@link http://linkeddatabook.com/editions/1.0/#htoc65}
 * 
 * Uses portions of Jena code
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * @see com.hp.hpl.jena.util.LocatorURL
 * @see org.openjena.riot.system.ContentNeg
 * {@link http://openjena.org}
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LocatorLinkedData implements Locator
{
    private static Logger log = LoggerFactory.getLogger(LocatorLinkedData.class) ;
    //static final String acceptHeader = "application/rdf+xml,application/xml;q=0.9,*/*;q=0.5" ;
    static final String[] schemeNames = { "http:" , "https:" } ;
    public static final Map<String, Double> QUALIFIED_TYPES;    
    static
    {
	Map<String, Double> typeMap = new HashMap<String, Double>();
	
	typeMap.put(WebContent.contentTypeRDFXML, null);

	typeMap.put(WebContent.contentTypeTurtle1, 0.9);
	typeMap.put(WebContent.contentTypeTurtle2, 0.9);
	typeMap.put(WebContent.contentTypeTurtle3, 0.9);

	typeMap.put(WebContent.contentTypeNTriples, 0.9);
	typeMap.put(WebContent.contentTypeNTriplesAlt, 0.9);

	typeMap.put(WebContent.contentTypeN3, 0.9);
	typeMap.put(WebContent.contentTypeN3Alt1, 0.9);
	typeMap.put(WebContent.contentTypeN3Alt2, 0.9);	


	typeMap.put(WebContent.contentTypeTriG, 0.9);
	typeMap.put(WebContent.contentTypeNQuads, 0.9);
	typeMap.put(WebContent.contentTypeTriGAlt, 0.9);
	typeMap.put(WebContent.contentTypeNQuadsAlt, 0.9);
	
	typeMap.put(WebContent.contentTypeTextPlain, 0.7);
	
	typeMap.put(WebContent.contentTypeXML, 0.5);
	
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
	/*
        if ( ! acceptByScheme(filenameOrURI) )
        {
            if (log.isTraceEnabled() )
                log.trace("Not found : "+filenameOrURI) ; 
            return null;
        }
        */
	log.trace("Request Accept header: {}", getAcceptHeader());

        try
        {
            URL url = new URL(filenameOrURI);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Accept", getAcceptHeader()) ;
            conn.setRequestProperty("Accept-Charset", "utf-8,*") ;
            conn.setDoInput(true) ;
            conn.setDoOutput(false) ;
            // Default is true.  See javadoc for HttpURLConnection
            //((HttpURLConnection)conn).setInstanceFollowRedirects(true) ;
            conn.connect() ;
            InputStream in = new BufferedInputStream(conn.getInputStream());
            
	    //ContentType type = org.openjena.riot.ContentType.parse(conn.getContentType());
	    String x = conn.getContentType() ; 
	    String contentType = null ;
	    String charset = null ;

	    if ( x.contains(";") )
	    {
		String[] xx = x.split("\\s*;\\s*") ;
		contentType = xx[0] ;
		charset = xx[1] ;
	    }
	    else
		contentType = x ;

	    if ( charset != null )
	    {
		int i = charset.indexOf("charset=") ;
		if ( i == 0 )
		    charset = charset.substring("charset=".length()) ;
	    }
	    //Charset cs = Charset.forName(charset) ;

	    if ( contentType != null )
		contentType = contentType.toLowerCase() ;
	
            if (log.isTraceEnabled())
	    {
		log.trace("Found: {}", filenameOrURI);
		log.trace("MIME type: {} Charset: {}", contentType, charset);
	    }

	    //return new TypedStream(in, type.getContentType(), type.getCharset());
            return new TypedStream(in, contentType, charset); 
        }
        catch (java.io.FileNotFoundException ex) 
        {
            if (log.isTraceEnabled())
		log.trace("LocatorLinkedData: not found: {}", filenameOrURI);
	    
            return null ;
        }
        catch (MalformedURLException ex)
        {
            log.warn("Malformed URL: {}", filenameOrURI);
            return null;
        }
        // IOExceptions that occur sometimes.
        catch (java.net.UnknownHostException ex)
        {
            if (log.isTraceEnabled() )
                log.trace("LocatorLinkedData: not found (UnknownHostException): {}", filenameOrURI);

            return null ;
        }
        catch (java.net.ConnectException ex)
        { 
            if (log.isTraceEnabled() )
                log.trace("LocatorLinkedData: not found (ConnectException): {}", filenameOrURI);
            return null ;
        }
        catch (java.net.SocketException ex)
        {
            if (log.isTraceEnabled() )
                log.trace("LocatorLinkedData: not found (SocketException): {}", filenameOrURI);
            return null ;
        }
        // And IOExceptions we don't expect
        catch (IOException ex)
        {
            log.warn("I/O Exception opening URL: " + filenameOrURI+"  "+ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String getName() { return "LocatorLinkedData" ; }

    public  Map<String, Double> getQualifiedTypes()
    {
	return QUALIFIED_TYPES;
    }
    
    public String getAcceptHeader()
    {
	String header = null;

	//for (Map.Entry<String, Double> type : getQualifiedTypes().entrySet())
	Iterator <Entry<String, Double>> it = getQualifiedTypes().entrySet().iterator();
	while (it.hasNext())
	{
	    Entry<String, Double> type = it.next();
	    if (header == null) header = "";
	    
	    header += type.getKey();
	    if (type.getValue() != null) header += ";q=" + type.getValue();
	    
	    if (it.hasNext()) header += ",";
	}
	
	return header;
    }
    
}
