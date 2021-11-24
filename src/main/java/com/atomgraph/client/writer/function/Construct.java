/*
 * Copyright 2021 Martynas Jusevičius <martynas@atomgraph.com>.
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
import static com.atomgraph.client.writer.ModelXSLTWriterBase.getSource;
import java.io.IOException;
import java.net.URISyntaxException;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmValue;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;

/**
 * <code>ac:construct()</code> XSLT function that constructs an instance from a given <code>CONSTRUCT</code> query.
 * Plugs into the Saxon processor.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see <a href="http://www.saxonica.com/documentation/#!extensibility/integratedfunctions">Integrated extension functions</a>
 */
public class Construct implements ExtensionFunction
{

    private final Processor processor;
    
    public Construct(Processor processor)
    {
        this.processor = processor;
    }
    
    @Override
    public QName getName()
    {
        return new QName(AC.NS, "construct");
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
            SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
        };
    }

    @Override
    public XdmValue call(XdmValue[] arguments) throws SaxonApiException
    {
        try
        {
            return getProcessor().newDocumentBuilder().build(getSource(getConstructedInstances(QueryFactory.create(arguments[0].itemAt(0).getStringValue()))));
        }
        catch (URISyntaxException | IOException ex)
        {
            throw new SaxonApiException(ex);
        }
    }
    
    public Model getConstructedInstances(Query constructor) throws URISyntaxException, IOException
    {
        try (QueryExecution qex = QueryExecutionFactory.create(constructor))
        {
            return qex.execConstruct();
        }
    }
    
    public Processor getProcessor()
    {
        return processor;
    }
    
}
