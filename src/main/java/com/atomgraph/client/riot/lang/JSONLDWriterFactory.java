/*
 * Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atomgraph.client.riot.lang;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import com.atomgraph.client.riot.JSONLDWriter;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class JSONLDWriterFactory implements WriterGraphRIOTFactory
{
    private final Templates templates;
    
    public JSONLDWriterFactory(Source stylesheet) throws TransformerConfigurationException
    {
        this(((SAXTransformerFactory)TransformerFactory.newInstance()).newTemplates(stylesheet));
    }
    
    public JSONLDWriterFactory(Templates templates)
    {
        this.templates = templates;
    }
    
    public Templates getTemplates()
    {
        return templates;
    }
    
    @Override
    public WriterGraphRIOT create(RDFFormat syntaxForm)
    {
        return new JSONLDWriter(getTemplates());
    }
    
}
