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

package org.graphity.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
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
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class XSLTBuilder
{
    private static final Logger log = LoggerFactory.getLogger(XSLTBuilder.class) ;

    private Source doc = null;
    private SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();    
    private TransformerHandler handler = null;
    private Transformer transformer = null;
    private Result result = null; 

    protected static XSLTBuilder newInstance()
    {
	return new XSLTBuilder();
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
	log.trace("Loading document Source with system ID: {}", doc.getSystemId());
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

    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
	log.trace("Loading stylesheet Source with system ID: {}", stylesheet.getSystemId());
	handler = factory.newTransformerHandler(stylesheet);
	transformer = handler.getTransformer();
	return this;
    }

    public XSLTBuilder parameter(String name, Object value)
    {
	log.trace("Setting transformer parameter {} with value {}", name, value);
	transformer.setParameter(name, value);
	//handler.getTransformer().setParameter(name, value);
	return this;
    }
    
    public XSLTBuilder resolver(URIResolver resolver)
    {
	log.trace("Setting URIResolver: {}", resolver);
	transformer.setURIResolver(resolver);
	//handler.getTransformer().setURIResolver(resolver);
	return this;
    }

    public XSLTBuilder outputProperty(String name, String value)
    {
	log.trace("Setting transformer OutputProperty {} with value {}", name, value);
	transformer.setOutputProperty(name, value);
	//handler.getTransformer().setOutputProperty(name, value);
	return this;
    }
    
    public void transform() throws TransformerException
    {
	log.trace("TransformerHandler: {}", handler);
	log.trace("Transformer: {}", transformer);
	log.trace("Transformer: {}", handler.getTransformer());
	log.trace("Document: {}", doc);
	log.trace("Result: {}", result);
	transformer.transform(doc, result);
	//handler.getTransformer().transform(doc, result);
    }

    public XSLTBuilder result(Result result) throws TransformerConfigurationException
    {
	this.result = result;
	//handler = factory.newTransformerHandler(stylesheet); // TransformerHandler not reusable in Saxon
	handler.setResult(result);
	return this;
    }

    public Transformer getTransformer()
    {
	return transformer;
    }
    
    public TransformerHandler getHandler()
    {
	return handler;
    }
    
    // http://xml.apache.org/xalan-j/usagepatterns.html#outasin
    public XSLTBuilder result(XSLTBuilder next) throws TransformerException // for chaining stylesheets
    {
	return result(new SAXResult(next.getHandler()));
    }
    
}