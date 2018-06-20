/*
 * Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>.
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

import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import net.sf.saxon.Controller;
import net.sf.saxon.trans.UnparsedTextURIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class XSLTBuilder extends com.atomgraph.client.util.XSLTBuilder
{
    
    private static final Logger log = LoggerFactory.getLogger(XSLTBuilder.class) ;

    private UnparsedTextURIResolver textResolver;
        
    protected XSLTBuilder(SAXTransformerFactory factory)
    {
        super(factory);
    }
    
    public static XSLTBuilder newInstance(SAXTransformerFactory factory)
    {
	return new XSLTBuilder(factory);
    }
    
    public XSLTBuilder resolver(UnparsedTextURIResolver textResolver)
    {
	if (log.isTraceEnabled()) log.trace("Setting UnparsedTextURIResolver: {}", textResolver);
        this.textResolver = textResolver;
	return this;
    }
    
    protected Transformer getTransformer(Templates templates, Result result, URIResolver uriResolver, Map<String, Object> parameters, Map<String, String> outputProperties, UnparsedTextURIResolver textResolver) throws TransformerConfigurationException
    {
        Transformer transformer = super.getTransformer(templates, result, uriResolver, parameters, outputProperties);
        Controller controller = (Controller)transformer;
        controller.setUnparsedTextURIResolver(textResolver);
        return transformer;
    }
   
    @Override
    protected Transformer getTransformer() throws TransformerConfigurationException
    {
        return getTransformer(getTemplates(), getResult(), getURIResolver(), getParameters(), getOutputProperties(), getUnparsedTextURIResolver());
    }
       
    protected UnparsedTextURIResolver getUnparsedTextURIResolver()
    {
        return textResolver;
    }
    
}
