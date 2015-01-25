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

package org.graphity.client.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Utility class that simplifies building of XSLT transformations.
 * Uses builder pattern.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class XSLTBuilder
{
    private static final Logger log = LoggerFactory.getLogger(XSLTBuilder.class) ;

    private Source doc = null;
    private final SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();    
    private TransformerHandler handler = null;
    private Transformer transformer = null;
    private Result result = null; 
    private final boolean cachingTemplates;
    private final Map<String, Templates> templatesCache = new ConcurrentHashMap<>();
    
    protected XSLTBuilder(boolean cachingTemplates)
    {
        this.cachingTemplates = cachingTemplates;
    }
    
    public static XSLTBuilder newInstance(boolean cachingTemplates)
    {
	return new XSLTBuilder(cachingTemplates);
    }

    public static XSLTBuilder newInstance()
    {
	return new XSLTBuilder(true); // cache by default
    }

    public static XSLTBuilder fromStylesheet(Source xslt) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(xslt);
    }

    public static XSLTBuilder fromStylesheet(Node n) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new DOMSource(n));
    }

    public static XSLTBuilder fromStylesheet(Node n, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new DOMSource(n, systemId));
    }

    public static XSLTBuilder fromStylesheet(File file) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(file));
    }

    public static XSLTBuilder fromStylesheet(InputStream is) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(is));
    }

    public static XSLTBuilder fromStylesheet(InputStream is, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(is, systemId));
    }

    public static XSLTBuilder fromStylesheet(Reader reader) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(reader));
    }

    public static XSLTBuilder fromStylesheet(Reader reader, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(reader, systemId));
    }

    public static XSLTBuilder fromStylesheet(String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(new StreamSource(systemId));
    }

    public XSLTBuilder document(Source doc)
    {
	if (log.isTraceEnabled()) log.trace("Loading document Source with system ID: {}", doc.getSystemId());
	this.doc = doc;
	return this;
    }

    public XSLTBuilder document(Node n)
    {
	document(new DOMSource(n));
	return this;
    }

    public XSLTBuilder document(Node n, String systemId)
    {
	document(new DOMSource(n, systemId));
	return this;
    }

    public XSLTBuilder document(File file)
    {
	document(new StreamSource(file));
	return this;
    }

    public XSLTBuilder document(InputStream is)
    {
	document(new StreamSource(is));
	return this;
    }

    public XSLTBuilder document(InputStream is, String systemId)
    {
	document(new StreamSource(is, systemId));
	return this;
    }

    public XSLTBuilder document(Reader reader)
    {
	document(new StreamSource(reader));
	return this;
    }

    public XSLTBuilder document(Reader reader, String systemId)
    {
	document(new StreamSource(reader, systemId));
	return this;
    }

    public XSLTBuilder document(String systemId)
    {
	document(new StreamSource(systemId));
	return this;
    }

    public XSLTBuilder stylesheet(File stylesheet) throws TransformerConfigurationException
    {
        //long lastModified = stylesheet.lastModified();
        return stylesheet(new StreamSource(stylesheet));
    }
    
    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
	if (log.isTraceEnabled()) log.trace("Loading stylesheet Source with system ID: {}", stylesheet.getSystemId());

        // return Templates if they are already compiled and cached for this Source
        if (isCachingTemplates() && templatesCache.containsKey(stylesheet.getSystemId()))
            return stylesheet(templatesCache.get(stylesheet.getSystemId()));

        Templates templates = factory.newTemplates(stylesheet);
        if (isCachingTemplates()) templatesCache.put(stylesheet.getSystemId(), templates);
        return stylesheet(templates);
    }

    public XSLTBuilder stylesheet(Templates stylesheet) throws TransformerConfigurationException            
    {
        handler = factory.newTransformerHandler(stylesheet);
	transformer = handler.getTransformer();
	return this;
    }

    public XSLTBuilder parameter(String name, Object value)
    {
	if (log.isTraceEnabled()) log.trace("Setting transformer parameter {} with value {}", name, value);
	getTransformer().setParameter(name, value);
	//handler.getTransformer().setParameter(name, value);
	return this;
    }
    
    public XSLTBuilder resolver(URIResolver resolver)
    {
	if (log.isTraceEnabled()) log.trace("Setting URIResolver: {}", resolver);
	getTransformer().setURIResolver(resolver);
	//handler.getTransformer().setURIResolver(resolver);
	return this;
    }

    public XSLTBuilder outputProperty(String name, String value)
    {
	if (log.isTraceEnabled()) log.trace("Setting transformer OutputProperty {} with value {}", name, value);
	getTransformer().setOutputProperty(name, value);
	//handler.getTransformer().setOutputProperty(name, value);
	return this;
    }
    
    public void transform() throws TransformerException
    {
	if (log.isTraceEnabled())
	{
	    log.trace("TransformerHandler: {}", getHandler());
	    log.trace("Transformer: {}", getTransformer());
	    log.trace("Transformer: {}", getHandler().getTransformer());
	    log.trace("Document: {}", getDocument());
	    log.trace("Result: {}", getResult());
	}
	getTransformer().transform(getDocument(), getResult());
	//handler.getTransformer().transform(doc, result);
    }

    public XSLTBuilder result(Result result) throws TransformerConfigurationException
    {
	this.result = result;
	//handler = factory.newTransformerHandler(stylesheet); // TransformerHandler not reusable in Saxon
	getHandler().setResult(result);
	return this;
    }

    public Source getDocument()
    {
        return doc;
    }

    public Transformer getTransformer()
    {
	return transformer;
    }
    
    public TransformerHandler getHandler()
    {
	return handler;
    }

    public Result getResult()
    {
        return result;
    }
 
    public boolean isCachingTemplates()
    {
        return cachingTemplates;
    }
    // http://xml.apache.org/xalan-j/usagepatterns.html#outasin
    public XSLTBuilder result(XSLTBuilder next) throws TransformerException // for chaining stylesheets
    {
	return result(new SAXResult(next.getHandler()));
    }
    
}