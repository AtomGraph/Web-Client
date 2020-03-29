/*
 * Copyright 2020 Martynas Jusevičius <martynas@atomgraph.com>.
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
package com.atomgraph.client.util.saxon;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Controller;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class SaxonXSLTBuilder
{
    
    private static final Logger log = LoggerFactory.getLogger(SaxonXSLTBuilder.class) ;

    private final XsltCompiler compiler;
    private XsltExecutable executable = null;
    private Serializer serializer;
    private final Map<QName, XdmValue> parameters = new HashMap<>();
    private final Map<Serializer.Property, String> outputProperties = new HashMap<>();
    private URIResolver uriResolver = null;
    private UnparsedTextURIResolver textResolver;
    private Source source = null;

    protected SaxonXSLTBuilder(Processor processor)
    {
        this(processor.newXsltCompiler());
    }
    
    protected SaxonXSLTBuilder(XsltCompiler compiler)
    {
        if (compiler == null) throw new IllegalStateException("XsltCompiler cannot be null");

        this.compiler = compiler;
        this.serializer = compiler.getProcessor().newSerializer();
    }
    
    public static SaxonXSLTBuilder newInstance(Processor processor)
    {
        return new SaxonXSLTBuilder(processor);
    }

    public static SaxonXSLTBuilder newInstance(XsltCompiler compiler)
    {
        return new SaxonXSLTBuilder(compiler);
    }

    public SaxonXSLTBuilder document(Source doc)
    {
        if (log.isTraceEnabled()) log.trace("Loading document Source with system ID: {}", doc.getSystemId());
        this.source = doc;
        return this;
    }

    public SaxonXSLTBuilder document(Node n)
    {
        document(new DOMSource(n));
        return this;
    }

    public SaxonXSLTBuilder document(Node n, String systemId)
    {
        document(new DOMSource(n, systemId));
        return this;
    }

    public SaxonXSLTBuilder document(File file)
    {
        document(new StreamSource(file));
        return this;
    }

    public SaxonXSLTBuilder document(InputStream is)
    {
        document(new StreamSource(is));
        return this;
    }

    public SaxonXSLTBuilder document(InputStream is, String systemId)
    {
        document(new StreamSource(is, systemId));
        return this;
    }

    public SaxonXSLTBuilder document(Reader reader)
    {
        document(new StreamSource(reader));
        return this;
    }

    public SaxonXSLTBuilder document(Reader reader, String systemId)
    {
        document(new StreamSource(reader, systemId));
        return this;
    }

    public SaxonXSLTBuilder document(String systemId)
    {
        document(new StreamSource(systemId));
        return this;
    }

    public SaxonXSLTBuilder stylesheet(File stylesheet) throws SaxonApiException
    {
        return stylesheet(new StreamSource(stylesheet));
    }
    
    public SaxonXSLTBuilder stylesheet(Source stylesheet) throws SaxonApiException
    {
        return stylesheet(compiler.compile(stylesheet));
    }

    public SaxonXSLTBuilder stylesheet(XsltExecutable executable)
    {
        this.executable = executable;
        return this;
    }

    public SaxonXSLTBuilder parameter(QName name, XdmValue value)
    {
        if (log.isTraceEnabled()) log.trace("Setting transformer parameter {} with value {}", name, value);
        parameters.put(name, value);
        return this;
    }

    public SaxonXSLTBuilder destination(OutputStream stream)
    {
        serializer.setOutputStream(stream);
        return this;
    }

    public SaxonXSLTBuilder destination(Writer writer)
    {
        serializer.setOutputWriter(writer);
        return this;
    }
    
    public SaxonXSLTBuilder resolver(URIResolver uriResolver)
    {
        if (log.isTraceEnabled()) log.trace("Setting URIResolver: {}", uriResolver);
        this.uriResolver = uriResolver;
        return this;
    }

    public SaxonXSLTBuilder resolver(UnparsedTextURIResolver textResolver)
    {
        if (log.isTraceEnabled()) log.trace("Setting UnparsedTextURIResolver: {}", textResolver);
        this.textResolver = textResolver;
        return this;
    }
    
    public SaxonXSLTBuilder outputProperty(Serializer.Property name, String value)
    {
        if (log.isTraceEnabled()) log.trace("Setting transformer OutputProperty {} with value {}", name, value);
        outputProperties.put(name, value);
        return this;
    }
    
    public void transform() throws SaxonApiException
    {
        Xslt30Transformer transformer = executable.load30();
        
        transformer.setURIResolver(uriResolver);
        transformer.setStylesheetParameters(parameters);
        
        transformer.getUnderlyingController().setUnparsedTextURIResolver(textResolver);
        
        Iterator<Map.Entry<Serializer.Property, String>> propertyIt = outputProperties.entrySet().iterator();
        while (propertyIt.hasNext())
        {
            Map.Entry<Serializer.Property, String> outputProperty = propertyIt.next();
            serializer.setOutputProperty(outputProperty.getKey(), outputProperty.getValue());
        }
        
        transformer.transform(source, serializer);
    }
    
}
