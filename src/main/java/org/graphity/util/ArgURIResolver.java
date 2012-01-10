package org.graphity.util;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import java.util.*;

import java.io.*;

/**
 * Resolves calls to documents with <code>arg://</code> URI from XSLT stylesheets by passing dynamically formed strings as an XML documents.
 */
public class ArgURIResolver
{
    private Map<String, Source> args = new Hashtable<String, Source>();
    private static final String ARG_PROTOCOL = "arg://";
    
    public Source resolve(String href, String base)
    {
        //System.out.print("href: " + href + " base : " + base);
        Source source = null;
        if (href.startsWith(ARG_PROTOCOL))
        {
            String argName = href.substring(ARG_PROTOCOL.length());
	    source = args.get(argName);
        }
        return source;
    }
   
    public void setArgument(String name, String arg)
    {
        setArgument(name, new StreamSource(new StringReader(arg)));
    }
    
    public void setArgument(String name, Source arg)
    {
        args.put(name, arg);
    }
}