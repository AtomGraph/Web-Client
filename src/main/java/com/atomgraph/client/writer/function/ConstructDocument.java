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
package com.atomgraph.client.writer.function;

import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.writer.DatasetXSLTWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

/**
 * <code>ac:construct-doc()</code> XSLT function that constructs default instance(s) for given class(es).
 * Plugs into Saxon processor.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see <a href="http://www.saxonica.com/documentation/#!extensibility/integratedfunctions">Integrated extension functions</a>
 */
public class ConstructDocument implements ExtensionFunction
{
    
    private final Processor processor;
    
    public ConstructDocument(Processor processor)
    {
        this.processor = processor;
    }
    
    @Override
    public QName getName()
    {
        return new QName(AC.NS, "construct-doc");
    }

    @Override
    public SequenceType getResultType()
    {
        return SequenceType.makeSequenceType(ItemType.DOCUMENT_NODE, OccurrenceIndicator.ZERO_OR_MORE);
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]
        {
            SequenceType.makeSequenceType(ItemType.ANY_URI, OccurrenceIndicator.ONE),
            SequenceType.makeSequenceType(ItemType.ANY_URI, OccurrenceIndicator.ZERO_OR_MORE),
            SequenceType.makeSequenceType(ItemType.ANY_URI, OccurrenceIndicator.ONE),
        };
    }

    @Override
    public XdmValue call(XdmValue[] arguments) throws SaxonApiException
    {
        try
        {
            URI ontology = new URI(arguments[0].itemAt(0).getStringValue());

            List<URI> classes = new ArrayList<>();
            Iterator<XdmItem> it = arguments[1].iterator();
            while (it.hasNext())
            {
                XdmItem item = it.next();
                classes.add(new URI(item.getStringValue()));
            }
            
            URI base = new URI(arguments[2].itemAt(0).getStringValue());
            
            return getProcessor().newDocumentBuilder().build(DatasetXSLTWriter.getConstructedSource(ontology, classes, base));
        }
        catch (URISyntaxException | IOException ex)
        {
            throw new SaxonApiException(ex);
        }
    }

    public Processor getProcessor()
    {
        return processor;
    }
    
}
