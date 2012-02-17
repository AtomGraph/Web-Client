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
package org.graphity.util.locator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class PrefixMapper extends LocationMapper
{
    private static final Logger log = LoggerFactory.getLogger(PrefixMapper.class) ;
    
    private Map<String, String> altPrefixLocations = new HashMap<String, String>() ;

    public PrefixMapper()
    {
	log.debug("PrefixMapper()");
    }

    public PrefixMapper(String config)
    {
        super(config);
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
	log.debug("altPrefixLocations: {}", altPrefixLocations);
	log.debug("PrefixMapper.addAltPrefixEntry({}, {})", uriPrefix, alt);
        altPrefixLocations.put(uriPrefix, alt);
    }

    public String getAltPrefixEntry(String uriPrefix) 
    {
        return altPrefixLocations.get(uriPrefix) ;
    }

    public Iterator<String> listAltPrefixEntries()  { return altPrefixLocations.keySet().iterator() ; } 

    @Override
    public String altMapping(String uri, String otherwise)
    {
        if (getAltEntry(uri) != null) 
            return getAltEntry(uri) ;
	
        String newStart = null ;
        String oldStart = null ;
        for ( Iterator<String> iter = listAltPrefixes() ; iter.hasNext() ;)
        {
            String prefix = iter.next() ;
            if ( uri.startsWith(prefix) )
            {
		if (getAltPrefixEntry(prefix) != null)
		    return getAltPrefixEntry(prefix);
		
                String s = getAltPrefix(prefix) ;
                if ( newStart == null || newStart.length() < s.length() )
                {
                    oldStart = prefix ;
                    newStart = s ;
                }
            }
        }
        
        if ( newStart != null )
            return newStart+uri.substring(oldStart.length()) ;
        
        return otherwise ;
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
            String v = getAltPrefixEntry(k) ;
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
                    log.debug("Mapping: "+name+" => "+altName) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing name mapping: "+ex.getMessage()) ;
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
			log.debug("Prefix mapping: "+prefix+" => "+altPrefix) ;
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
                    log.warn("Error processing prefix mapping: "+ex.getMessage()) ;
                    return ;
                }
            }
        }
    }
}
