package org.graphity.processor.util;

import java.net.URI;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is based on <pre>ex09_1</pre> from the "RESTful Java with JAX-RS" book.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://shop.oreilly.com/product/9780596158057.do">RESTful Java with JAX-RS</a>
 */
public class Link
{
    private static Pattern PATTERN = Pattern.compile("<(.+)>\\s*;\\s*(.+)");
    
    private URI href = null;
    private String rel = null;
    private String type = null;

    private Link()
    {
    }
    
    public Link(URI href, String rel, String type)
    {
	if (href == null) throw new IllegalArgumentException("Link value cannot be null");
	if (rel == null) throw new IllegalArgumentException("Link relationship cannot be null");
        
        this.href = href;
        this.rel = rel;
        this.type = type;
    }

    public String getRel()
    {
        return rel;
    }
    
    public void setRel(String rel)
    {
        this.rel = rel;
    }

    public URI getHref()
    {
        return href;
    }
    
    public void setHref(URI href)
    {
        this.href = href;
    }

    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }

    /**
    * To write as link header
    *
    * @return
    */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("<");
        builder.append(getHref()).append(">; rel=").append(getRel());
        if (getType() != null) builder.append("; type=").append(getType());
        return builder.toString();
    }
    
    /**
    * For unmarshalling Link Headers.
    * Its not an efficient or perfect algorithm
    * and does make a few assumptions
    *
    * @param val
    * @return
    */
    public static Link valueOf(String val)
    {
	if (val == null) throw new IllegalArgumentException("Link value cannot be null");
        
        Matcher matcher = PATTERN.matcher(val);
        if (!matcher.matches())
            throw new RuntimeException("Failed to parse link: " + val);
        
        Link link = new Link();
        link.setHref(URI.create(matcher.group(1)));
        String[] props = matcher.group(2).split(";");
        HashMap<String, String> map = new HashMap();
        for (String prop : props)
        {
            String[] split = prop.split("=");
            map.put(split[0].trim(), split[1].trim());
        }
        if (map.containsKey("rel")) link.setRel(map.get("rel"));
        if (map.containsKey("type")) link.setType(map.get("type"));

        return link;
    }

}