/*
 * XSLTView.java
 *
 * Created on Sekmadienis, 2007, Kovo 11, 22.51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.graphity.util;

import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Pumba
 */
public class XSLTBuilder
{
    private Source source = null;
    //private Source stylesheet = null;
    private SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();    
    private Templates templates = null;
    private TransformerHandler handler = null;
    private Transformer transformer = null; 

    protected static XSLTBuilder newInstance()
    {
	return new XSLTBuilder();
    }
    
    public static XSLTBuilder fromDocument(Source doc)
    {
	return newInstance().document(doc);
    }

    public static XSLTBuilder fromDocument(Node n)
    {
	return newInstance().document(n);
    }

    public static XSLTBuilder fromDocument(Node n, String systemId)
    {
	return newInstance().document(n, systemId);
    }

    public static XSLTBuilder fromDocument(File file)
    {
	return newInstance().document(file);
    }

    public static XSLTBuilder fromDocument(InputStream is)
    {
	return newInstance().document(is);
    }

    public static XSLTBuilder fromDocument(InputStream is, String systemId)
    {
	return newInstance().document(is, systemId);
    }

    public static XSLTBuilder fromDocument(Reader reader)
    {
	return newInstance().document(reader);
    }

    public static XSLTBuilder fromDocument(Reader reader, String systemId)
    {
	return newInstance().document(reader, systemId);
    }

    public static XSLTBuilder fromDocument(String systemId)
    {
	return newInstance().document(systemId);
    }

    public static XSLTBuilder fromStylesheet(Source doc) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(doc);
    }

    public static XSLTBuilder fromStylesheet(Node n) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(n);
    }

    public static XSLTBuilder fromStylesheet(Node n, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(n, systemId);
    }

    public static XSLTBuilder fromStylesheet(File file) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(file);
    }

    public static XSLTBuilder fromStylesheet(InputStream is) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(is);
    }

    public static XSLTBuilder fromStylesheet(InputStream is, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(is, systemId);
    }

    public static XSLTBuilder fromStylesheet(Reader reader) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(reader);
    }

    public static XSLTBuilder fromStylesheet(Reader reader, String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(reader, systemId);
    }

    public static XSLTBuilder fromStylesheet(String systemId) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(systemId);
    }

    public XSLTBuilder document(Source doc)
    {
	this.source = doc;
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

    // http://xml.apache.org/xalan-j/usagepatterns.html#outasin
    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
        //transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
	templates = TransformerFactory.newInstance().newTemplates(stylesheet);
	handler = factory.newTransformerHandler(templates);
	//transformer = templates.newTransformer();
	transformer = factory.newTransformer();
	return this;
    }

    public XSLTBuilder stylesheet(Node n) throws TransformerConfigurationException
    {
        return stylesheet(new DOMSource(n));
    }

    public XSLTBuilder stylesheet(Node n, String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new DOMSource(n, systemId));
    }

    public XSLTBuilder stylesheet(File file) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(file));
    }

    public XSLTBuilder stylesheet(InputStream is) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(is));
    }

    public XSLTBuilder stylesheet(InputStream is, String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(is, systemId));
    }

    public XSLTBuilder stylesheet(Reader reader) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(reader));
    }

    public XSLTBuilder stylesheet(Reader reader, String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(reader, systemId));
    }

    public XSLTBuilder stylesheet(String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(systemId));
    }

    public XSLTBuilder parameter(String name, Object value)
    {
	//transformer.setParameter(name, o);
	handler.getTransformer().setParameter(name, value);
	return this;
    }
    
    public XSLTBuilder resolver(URIResolver resolver)
    {
	//transformer.setURIResolver(resolver);
	handler.getTransformer().setURIResolver(resolver);
	return this;
    }

    public XSLTBuilder outputProperty(String name, String value)
    {
	//transformer.setOutputProperty(name, value);
	handler.getTransformer().setOutputProperty(name, value);
	return this;
    }
    
    public void transform(Result result) throws TransformerException
    {
	//transformer.transform(doc, result);
	handler.setResult(result);
	transformer.transform(source, new SAXResult(handler));
	//handler.getTransformer().transform(source, new SAXResult(handler));
    }

    public Document transform() throws TransformerException, ParserConfigurationException
    {
	Document resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	
	transform(new DOMResult(resDoc));
	
	return resDoc;
    }

    public void transform(OutputStream out) throws TransformerException
    {
	transform(new StreamResult(out));
    }

    public void transform(XSLTBuilder builder) // for chaining stylesheets
    {
	
    }
    
}