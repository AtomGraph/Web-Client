/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.Locator;
import com.hp.hpl.jena.util.TypedStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
public class DataManager extends FileManager implements URIResolver
{
    private static final Logger log = LoggerFactory.getLogger(DataManager.class) ;
    
    static DataManager instance = null ;

    public DataManager(LocationMapper _mapper)
    {
	super(_mapper);
        addLocator( new LocatorLinkedData()) ;
    }

    public DataManager(FileManager filemanager)
    {
	super(filemanager);
        addLocator(new LocatorLinkedData()) ;
    }

    public DataManager()
    {
	super();
	log.debug("DataManager() constructor");
        addLocator(new LocatorLinkedData());
	removeLocatorURL();
    }
    
    public static DataManager get()
    {
        // Singleton pattern adopted in case we later have several file managers.
        if ( instance == null )
            //instance = makeGlobal() ;
	    instance = new DataManager();
        return instance ;
    }
    
    @Override
    public Model loadModel(String filenameOrURI)
    {
	TypedStream stream = openNoMapOrNull(filenameOrURI);
	
	log.debug("Opened filename or URI {} with media type {}", filenameOrURI, stream.getMimeType());
	
	return super.loadModel(filenameOrURI);
    }
    
    /** Add a Linked Data locator */
    public void addLocatorLinkedData()
    {
        Locator loc = new LocatorLinkedData() ;
        addLocator(loc) ;
    }

    private void removeLocatorURL()
    {
	Iterator<Locator> it = locators();
	while (it.hasNext())
	{
	    Locator loc = it.next();
	    if (loc.getName().equals("LocatorURL"))
	    {
		log.debug("Removing Locator: {}", loc);
		remove(loc);
	    }
	}
    }
    
    /*
    public static void setStdLocators(FileManager fMgr)
    {
	// super.setStdLocators(fMgr); // cannot call this because LocatorURL needs to be removed
	//fMgr.handlers.clear() ;
        fMgr.addLocatorFile() ;
        //fMgr.addLocatorURL() ;
        fMgr.addLocatorClassLoader(fMgr.getClass().getClassLoader()) ;
        fMgr.addLocator(new LocatorLinkedData());
    }
    
    public static DataManager makeGlobal()
    {
        DataManager dMgr = new DataManager(LocationMapper.get());
        setStdLocators(dMgr) ;
        return dMgr ;
    }
    */

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
	log.debug("Resolving URI: {} against base URI: {}", href, base);

	Model model = loadModel(href, base, null);
	log.debug("Number of Model stmts read: {}", model.size());
	
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); // byte buffer - possible to avoid?
	model.write(stream);
	
	log.debug("RDF/XML bytes written: {}", stream.toByteArray().length);
	
	return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
    }
    
}
