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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Pumba
 */
public class XSLTBuilder
{
    private Transformer transformer = null;
    private Source doc = null;
    //private Source stylesheet = null;

    protected XSLTBuilder newInstance()
    {
	return new XSLTBuilder();
    }
    
    public XSLTBuilder fromDocument(Source doc)
    {
	return newInstance().document(doc);
    }

    public XSLTBuilder fromDocument(Document doc)
    {
	return newInstance().document(doc);
    }

    public XSLTBuilder fromDocument(File file)
    {
	return newInstance().document(file);
    }

    public XSLTBuilder fromStylesheet(Source doc) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(doc);
    }

    public XSLTBuilder fromStylesheet(Document doc) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(doc);
    }

    public XSLTBuilder fromStylesheet(File file) throws TransformerConfigurationException
    {
	return newInstance().stylesheet(file);
    }

    public XSLTBuilder document(Source doc)
    {
	this.doc = doc;
	return this;
    }

    public XSLTBuilder document(Document doc)
    {
	document(new DOMSource(doc));
	return this;
    }

    public XSLTBuilder document(File file)
    {
	document(new StreamSource(file));
	return this;
    }

    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
        transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
	return this;
    }

    public XSLTBuilder stylesheet(Document doc) throws TransformerConfigurationException
    {
        return stylesheet(new DOMSource(doc));
    }

    public XSLTBuilder stylesheet(File file) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(file));
    }

    public XSLTBuilder stylesheet(InputStream stream, String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(stream, systemId));
    }

    public XSLTBuilder stylesheet(String systemId) throws TransformerConfigurationException
    {
        return stylesheet(new StreamSource(systemId));
    }

    public XSLTBuilder parameter(String name, Object o)
    {
	transformer.setParameter(name, o);
	return this;
    }
    
    public XSLTBuilder resolver(URIResolver resolver)
    {
	transformer.setURIResolver(resolver);
	return this;
    }

    public void transform(Result result) throws TransformerException
    {
	transformer.transform(doc, result);
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
    
    /*
    public void display(HttpServletRequest request, OutputStream out) throws IOException, TransformerException, ParserConfigurationException
    {
	getTransformer().setURIResolver(resolver);
	getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        //getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");

	getTransformer().transform(doc, new StreamResult(out));
    }
     */

}
