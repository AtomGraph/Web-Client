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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 *
 * @author Pumba
 */
public class XSLTBuilder
{
    private Transformer transformer = null;
    private Source doc = null;
    private Source stylesheet = null;
    private URIResolver resolver = new URIResolver();

    public XSLTBuilder document(Source doc)
    {
	this.doc = doc;
	return this;
    }

    public XSLTBuilder document(File file)
    {
	document(new StreamSource(file));
	return this;
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

    public XSLTBuilder stylesheet(Source stylesheet) throws TransformerConfigurationException
    {
	this.stylesheet = stylesheet;
        transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
	return this;
    }

    public URIResolver getResolver()
    {
	return resolver;
    }

    public void setResolver(URIResolver resolver)
    {
	this.resolver = resolver;
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
