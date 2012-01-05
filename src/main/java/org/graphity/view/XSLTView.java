/*
 * XSLTView.java
 *
 * Created on Sekmadienis, 2007, Kovo 11, 22.51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.graphity.view;

import org.graphity.View;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.graphity.Resource;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Pumba
 */
public class XSLTView extends View
{
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private Transformer transformer = null;
    private Source doc = null;
    private Source styleSheet = null;
    private ArgURIResolver resolver = new ArgURIResolver();

    public XSLTView(Resource resource)
    {
	super(resource);
    }

    public Source getDocument()
    {
	return doc;
    }

    public void setDocument(Source doc)
    {
	this.doc = doc;
    }

    public void setDocument(String string)
    {
	setDocument(new StreamSource(new StringReader(string)));
    }

    public void setDocument(File file)
    {
	setDocument(new StreamSource(file));
    }
	
    public Transformer getTransformer()
    {
	return transformer;
    }

    public Source getStyleSheet()
    {
	return styleSheet;
    }

    public void setStyleSheet(File file) throws TransformerConfigurationException
    {
        setStyleSheet(new StreamSource(file));
    }

    public void setStyleSheet(InputStream stream, String systemId) throws TransformerConfigurationException
    {
        setStyleSheet(new StreamSource(stream, systemId));
    }

    public void setStyleSheet(String systemId) throws TransformerConfigurationException
    {
        setStyleSheet(new StreamSource(systemId));
    }

    public void setStyleSheet(Source styleSheet) throws TransformerConfigurationException
    {
	this.styleSheet = styleSheet;
        transformer = tFactory.newTransformer(styleSheet);
    }

    public ArgURIResolver getResolver()
    {
	return resolver;
    }

    public void setResolver(ArgURIResolver resolver)
    {
	this.resolver = resolver;
    }

    @Override
    public Object getEntity()
    {
	return getDocument();
    }

    public void display(HttpServletRequest request, OutputStream out) throws IOException, TransformerException, ParserConfigurationException
    {
	getTransformer().setURIResolver(resolver);
	getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        //getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");

	getTransformer().transform(doc, new StreamResult(out));
    }

    @Override
    public int getStatus()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
