/**
 *  Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.client.riot;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.util.Context;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.system.PrefixMap;
import com.atomgraph.client.util.XSLTBuilder;
import com.atomgraph.core.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Writes Model as JSON-LD.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see <a href="http://json-ld.org">JSON-LD - JSON for Linking Data</a>
 */
public class JSONLDWriter implements WriterGraphRIOT
{
    private static final Logger log = LoggerFactory.getLogger(JSONLDWriter.class);
    
    private final Templates templates;
    
    public JSONLDWriter(Source stylesheet) throws TransformerConfigurationException
    {
        this(((SAXTransformerFactory)TransformerFactory.newInstance()).newTemplates(stylesheet));
    }
    
    public JSONLDWriter(Templates templates)
    {
	if (templates == null) throw new IllegalArgumentException("Templates cannot be null");
	this.templates = templates;
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ModelFactory.createModelForGraph(graph).
                write(baos, org.apache.jena.riot.RDFLanguages.RDFXML.getName(), baseURI);
        
        try
        {
            XSLTBuilder.fromStylesheet(getTemplates()).
                document(new ByteArrayInputStream(baos.toByteArray())).
                result(new StreamResult(out)).
                transform();                    
        }
        catch (TransformerException ex)
        {
            if (log.isDebugEnabled()) log.debug("Writing JSON-LD stream failed: {}", ex);            
            throw new RiotException(ex);
        }
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        throw new UnsupportedOperationException("Use OutputStream instead of Writer");
    }

    @Override
    public Lang getLang()
    {
        return RDFLanguages.JSONLD;
    }

    public Templates getTemplates()
    {
        return templates;
    }
    
}