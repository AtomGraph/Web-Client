/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client.locator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom mapper implementation that maps URI prefixes to file names
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.client.util.DataManager
 */
public class PrefixMapper extends LocationMapper
{
    private static final Logger log = LoggerFactory.getLogger(PrefixMapper.class) ;
    
    private final Map<String, String> altPrefixLocations = new HashMap<>() ;

    public PrefixMapper()
    {
	if (log.isDebugEnabled()) log.debug("PrefixMapper()");
    }

    public PrefixMapper(String config)
    {
        initFromPath(config, true) ;
    }
    
    public PrefixMapper(LocationMapper locMapper)
    {
	super(locMapper);
    }

    public PrefixMapper(PrefixMapper prefixMapper)
    {
	super(prefixMapper);
        altPrefixLocations.putAll(prefixMapper.altPrefixLocations);
    }
    
    public void addAltPrefixEntry(String uriPrefix, String alt)
    {
	if (log.isDebugEnabled()) log.debug("PrefixMapper.addAltPrefixEntry({}, {})", uriPrefix, alt);
        altPrefixLocations.put(uriPrefix, alt);
    }

    public String getPrefixAltEntry(String uriPrefix) 
    {
        return altPrefixLocations.get(uriPrefix) ;
    }

    public Iterator<String> listAltPrefixEntries()  { return altPrefixLocations.keySet().iterator() ; } 

    public String getPrefix(String uri)
    {
	String prefix = null;
	
        for (Iterator<String> iter = listAltPrefixEntries(); iter.hasNext();)
        {
            String candPrefix = iter.next() ;
	    // make sure we get the longest matching prefix
            if (uri.startsWith(candPrefix) &&
		    (prefix == null || candPrefix.length() > prefix.length()))
		prefix = candPrefix;
        }
	
	return prefix;
    }
    
    @Override
    public String altMapping(String uri, String otherwise)
    {
	if (log.isDebugEnabled()) log.debug("PrefixMapper.altMapping({}, {})", uri, otherwise);

	String altEntry = getAltEntry(uri);
        if (altEntry != null) 
	{
	    if (log.isDebugEnabled()) log.debug("Returning existing altName mapping: {} for URI: {}", altEntry, uri);
            return altEntry;
	}
	
	String prefix = getPrefix(uri);
	String prefixAltEntry = getPrefixAltEntry(prefix);
	if (prefix != null && prefixAltEntry != null)
	{
	    if (log.isDebugEnabled()) log.debug("Returning existing altName mapping: {} for prefix: {}", prefixAltEntry, prefix);
	    return getPrefixAltEntry(prefix);
	}
        
        return super.altMapping(uri, otherwise);
    }

    @Override
    public void toModel(Model model)
    {
        
        for ( Iterator<String> iter = listAltEntries() ; iter.hasNext() ; )
        {
            Resource r = model.createResource() ;
            Resource e = model.createResource() ;
            model.add(r, LocationMappingVocab.mapping, e) ;
            
            String k = iter.next() ;
            String v = getAltEntry(k) ;
            model.add(e, LocationMappingVocab.name, k) ;
            model.add(e, LocationMappingVocab.altName, v) ;
        }

        for ( Iterator<String> iter = listAltPrefixes() ; iter.hasNext() ; )
        {
            Resource r = model.createResource() ;
            Resource e = model.createResource() ;
            model.add(r, LocationMappingVocab.mapping, e) ;
            String k = iter.next() ;
            String v = getAltPrefix(k) ;
            model.add(e, LocationMappingVocab.prefix, k) ;
            model.add(e, LocationMappingVocab.altPrefix, v) ;
        }
	
        for ( Iterator<String> iter = listAltPrefixEntries() ; iter.hasNext() ; )
        {
            Resource r = model.createResource() ;
            Resource e = model.createResource() ;
            model.add(r, LocationMappingVocab.mapping, e) ;
            String k = iter.next() ;
            String v = getPrefixAltEntry(k) ;
            model.add(e, LocationMappingVocab.prefix, k) ;
            model.add(e, LocationMappingVocab.altName, v) ;
        }

    }
    
    @Override
    public void processConfig(Model m)
    {
        StmtIterator mappings =
            m.listStatements(null, LocationMappingVocab.mapping, (RDFNode)null) ;

        for (; mappings.hasNext();)
        {
            Statement s = mappings.nextStatement() ;
            Resource mapping =  s.getResource() ;
            
            if ( mapping.hasProperty(LocationMappingVocab.name) )
            {
                try 
                {
                    String name = mapping.getRequiredProperty(LocationMappingVocab.name)
                                        .getString() ;
                    String altName = mapping.getRequiredProperty(LocationMappingVocab.altName)
                                        .getString() ;
                    addAltEntry(name, altName) ;
                    if (log.isDebugEnabled()) log.debug("Mapping: "+name+" => "+altName) ;
                } catch (JenaException ex)
                {
                    if (log.isWarnEnabled()) log.warn("Error processing name mapping: "+ex.getMessage()) ;
                    return ;
                }
                
            }
            
            if ( mapping.hasProperty(LocationMappingVocab.prefix) )
            {
                try 
                {
                    String prefix = mapping.getRequiredProperty(LocationMappingVocab.prefix)
                                        .getString() ;
		    
		    if (mapping.hasProperty(LocationMappingVocab.altPrefix))
		    {
			String altPrefix = mapping.getRequiredProperty(LocationMappingVocab.altPrefix)
					    .getString() ;
			addAltPrefix(prefix, altPrefix) ;
			if (log.isDebugEnabled()) log.debug("Prefix mapping: "+prefix+" => "+altPrefix) ;
		    }
		    
		    if (mapping.hasProperty(LocationMappingVocab.altName))
		    {
			String altName = mapping.getRequiredProperty(LocationMappingVocab.altName)
					    .getString() ;
			addAltPrefixEntry(prefix, altName) ;
			log.debug("Prefix/name mapping: "+prefix+" => "+altName) ;			
		    }
                } catch (JenaException ex)
                {
                    if (log.isWarnEnabled()) log.warn("Error processing prefix mapping: "+ex.getMessage()) ;
                    return ;
                }
            }
        }
    }

    private void initFromPath(String configPath, boolean configMustExist)
    {
        if ( configPath == null || configPath.length() == 0 )
        {
            if (log.isWarnEnabled()) log.warn("Null configuration") ;
            return ;
        }
        
        // Make a file manager to look for the location mapping file
        FileManager fm = new FileManager() ;
        fm.addLocatorFile() ;
        fm.addLocatorClassLoader(fm.getClass().getClassLoader()) ;
        
        try {
            String uriConfig = null ; 
            InputStream in = null ;
            
            StringTokenizer pathElems = new StringTokenizer( configPath, FileManager.PATH_DELIMITER );
            while (pathElems.hasMoreTokens()) {
                String uri = pathElems.nextToken();
                if ( uri == null || uri.length() == 0 )
                    break ;
                
                in = fm.openNoMap(uri) ;
                if ( in != null )
                {
                    uriConfig = uri ;
                    break ;
                }
            }

            if ( in == null )
            {
                if ( ! configMustExist )
                    if (log.isDebugEnabled()) log.debug("Failed to find configuration: "+configPath) ;
                return ;
            }
            String syntax = FileUtils.guessLang(uriConfig) ;
            Model model = ModelFactory.createDefaultModel() ;
            model.read(in, uriConfig, syntax) ;
            processConfig(model) ;
        } catch (JenaException ex)
        {
            LoggerFactory.getLogger(LocationMapper.class).warn("Error in configuration file: "+ex.getMessage()) ;
        }
    }
    
    @Override
    public String toString()
    {
	String s = super.toString();
	
        for ( Iterator<String> iter = listAltPrefixEntries() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            String v = getPrefixAltEntry(k) ;
            s = s+"(Prefix:"+k+"=>"+v+") " ;
        }
	
        return s ;
    }
}
