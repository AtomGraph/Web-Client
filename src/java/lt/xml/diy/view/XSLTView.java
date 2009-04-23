/*
 * XSLTView.java
 *
 * Created on Sekmadienis, 2007, Kovo 11, 22.51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.view;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lt.xml.diy.controller.Resource;

/**
 *
 * @author Pumba
 */
public class XSLTView extends View
{
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    //protected StreamSource template = null;
    private Transformer transformer = null;
    //private File styleSheetFile = null;
    private Source doc = null;
    private Source styleSheet = null;
    private ArgURIResolver resolver = new ArgURIResolver();

    /** Creates a new instance of XSLTView */
    public XSLTView(Resource resource)
    {
	super(resource);
	System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");
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

    /*
    protected void init() throws TransformerConfigurationException
    {
        transformer = tFactory.newTransformer(styleSheet);
    }
    */
    
    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	//if (resource != null) doc = new StreamSource(new StringReader(QueryXML.query(Ontology.getRepository(), QueryBuilder.build(servlet.getServletConfig().getServletContext().getRealPath("/sparql/type.rq"), resource.getURI()))));

	transformer.setURIResolver(resolver);
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	Result result = new StreamResult(response.getOutputStream());
	transformer.transform(doc, result);
	//transformer.transform(doc, new StreamResult(System.out));

    }
    
}
