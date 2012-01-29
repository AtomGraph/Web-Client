/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

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
 * http://linkeddatabook.com/editions/1.0/#htoc65
 * 
 * @author Pumba
 */
public class LocatorLinkedData implements Locator
{
    private static Logger log = LoggerFactory.getLogger(LocatorLinkedData.class) ;
    //static final String acceptHeader = "application/rdf+xml,application/xml;q=0.9,*/*;q=0.5" ;
    static final String[] schemeNames = { "http:" , "https:" } ;
    public static final Map<String, Double> TYPE_MAP;
    
    static
    {
	Map<String, Double> typeMap = new HashMap<String, Double>();
	
	typeMap.put(WebContent.langRDFXML, null);
	
	typeMap.put(WebContent.contentTypeTurtle1, 0.9);
	typeMap.put(WebContent.contentTypeTurtle2, 0.9);
	typeMap.put(WebContent.contentTypeTurtle3, 0.9);
	
	TYPE_MAP = Collections.unmodifiableMap(typeMap);
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
log.trace("Accept header: {}", getAcceptHeader());

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
            
            if (log.isTraceEnabled() ) log.trace("Found: {}", filenameOrURI) ;
	    
            return new TypedStream(in, conn.getContentType()) ; 
        }
        catch (java.io.FileNotFoundException ex) 
        {
            if (log.isTraceEnabled())
		log.trace("LocatorURL: not found: {}", filenameOrURI);
	    
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
                log.trace("LocatorURL: not found (SocketException): {}", filenameOrURI);
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

    protected Map<String, Double> getQualifiedTypes()
    {
	return TYPE_MAP;
    }
    
    private String getAcceptHeader()
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
