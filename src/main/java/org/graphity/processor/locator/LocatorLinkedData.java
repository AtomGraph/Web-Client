/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.processor.locator;

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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jena-compatible Locator that can be used to load RDF from Linked Data
 * 
 * Uses portions of Jena code
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * @see com.hp.hpl.jena.util.LocatorURL
 * {@link http://openjena.org}
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.util.DataManager
 */
public class LocatorLinkedData implements Locator
{
    private static final Logger log = LoggerFactory.getLogger(LocatorLinkedData.class);
    
    static final String[] schemeNames = { "http:" , "https:" } ;
    public static final Map<String, Double> QUALIFIED_TYPES;
    static
    {
	Map<String, Double> typeMap = new HashMap<>();

        for (String type : Lang.RDFXML.getAltContentTypes())
            typeMap.put(type, null);
	
        Iterator<Lang> it = RDFLanguages.getRegisteredLanguages().iterator();
        while (it.hasNext())
        {
            Lang lang = it.next();
            if (!lang.equals(Lang.RDFNULL) && !lang.equals(Lang.RDFXML))
            {
                typeMap.put(lang.getContentType().getContentType(), 0.9);
                
                Iterator<String> typeIt = lang.getAltContentTypes().iterator();
                while (typeIt.hasNext())
                {
                    String type = typeIt.next();
                    if (!type.equals(lang.getContentType().getContentType()))
                        typeMap.put(type, 0.8);
                }
            }
        }
        
	QUALIFIED_TYPES = Collections.unmodifiableMap(typeMap);
    }

    private final URLConnection conn = null;
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
	if (log.isDebugEnabled()) log.debug("Request Accept header: {}", getAcceptHeader());

        try
        {
	    URLConnection c = getURLConnection(filenameOrURI);
            c.setRequestProperty("Accept", getAcceptHeader()) ;
            c.setRequestProperty("Accept-Charset", "utf-8,*") ;
            c.setDoInput(true) ;
            c.setDoOutput(false) ;
            // Default is true.  See javadoc for HttpURLConnection
            //((HttpURLConnection)conn).setInstanceFollowRedirects(true) ;
            c.connect() ;
            InputStream in = new BufferedInputStream(c.getInputStream());
	    String contentType = getContentType(c);
	    
            if (log.isTraceEnabled())
	    {
		log.trace("Found: {}", filenameOrURI);
		log.trace("MIME type: {} Charset: {}", contentType, getCharset(c));
	    }

	    if (!getQualifiedTypes().containsKey(contentType))
	    {
		if (log.isDebugEnabled()) log.debug("Returned content type {} is not acceptable - TypedStream will not be read", contentType);
		return null;
	    }
		
            return new TypedStream(in, contentType, getCharset(c)); 
        }
        catch (java.io.FileNotFoundException ex) 
        {
            if (log.isTraceEnabled())
		log.trace("LocatorLinkedData: not found: {}", filenameOrURI);
	    
            return null ;
        }
        catch (MalformedURLException ex)
        {
            if (log.isWarnEnabled()) log.warn("Malformed URL: {}", filenameOrURI);
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
            if (log.isWarnEnabled()) log.warn("I/O Exception opening URL: " + filenameOrURI+"  "+ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String getName()
    {
	return "LocatorLinkedData";
    }

    public URLConnection getURLConnection(String filenameOrURI) throws MalformedURLException, IOException
    {
	if (conn == null)
	{
	    URL url = new URL(filenameOrURI);
	    return url.openConnection();
	}
	else
	    return conn;
    }
    
    public  Map<String, Double> getQualifiedTypes()
    {
	return QUALIFIED_TYPES;
    }
    
    public String getAcceptHeader()
    {
	String header = null;

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

    public String getContentType(String filenameOrURI)
    {
	try
	{
	    return getContentType(getURLConnection(filenameOrURI));
	} catch (MalformedURLException ex)
	{
	    log.error("Malformed URL", ex);
	} catch (IOException ex)
	{
	    log.error("IO exception", ex);
	}
	
	return null;
    }
    
    public String getContentType(URLConnection conn)
    {
	//ContentType type = org.openjena.riot.ContentType.parse(conn.getContentType());
	String contentType;
	String x = conn.getContentType(); 

	if (x != null && x.contains(";") )
	{
	    String[] xx = x.split("\\s*;\\s*") ;
	    contentType = xx[0] ;
	}
	else
	    contentType = x ;

	if ( contentType != null )
	    contentType = contentType.toLowerCase() ;

	return contentType;
    }

    public String getCharset(String filenameOrURI)
    {
	try
	{
	    return getCharset(getURLConnection(filenameOrURI));
	} catch (MalformedURLException ex)
	{
	    log.error("Malformed URL", ex);
	} catch (IOException ex)
	{
	    log.error("IO exception", ex);
	}
	
	return null;
    }
    
    public String getCharset(URLConnection conn)
    {
	String charset = null ;
	String x = conn.getContentType(); 
	if ( x.contains(";") )
	{
	    String[] xx = x.split("\\s*;\\s*") ;
	    charset = xx[1] ;
	}

	if ( charset != null )
	{
	    int i = charset.indexOf("charset=") ;
	    if ( i == 0 )
		charset = charset.substring("charset=".length()) ;
	}
	//Charset cs = Charset.forName(charset) ;

	return charset;
    }
    
}