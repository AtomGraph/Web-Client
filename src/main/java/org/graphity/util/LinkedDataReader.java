/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 *
 * @author Pumba
 */
public class LinkedDataReader implements com.hp.hpl.jena.rdf.model.RDFReader, URIResolver
{

    @Override
    public void read(Model model, Reader r, String base)
    {

//FileManager.get().rea
	
//String x = WebContent.langTriG;
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void read(Model model, InputStream r, String base)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void read(Model model, String url)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object setProperty(String propName, Object propValue)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Source resolve(String string, String string1) throws TransformerException
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
