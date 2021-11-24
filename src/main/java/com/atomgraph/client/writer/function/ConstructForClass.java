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

import com.atomgraph.client.util.Constructor;
import com.atomgraph.client.vocabulary.AC;
import static com.atomgraph.client.writer.ModelXSLTWriter.checkURI;
import static com.atomgraph.client.writer.ModelXSLTWriter.getSource;
import java.io.IOException;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmValue;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * <code>ac:construct()</code> XSLT function that constructs instances for given classes from their constructors.
 * Plugs into the Saxon processor.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 * @see <a href="http://www.saxonica.com/documentation/#!extensibility/integratedfunctions">Integrated extension functions</a>
 */
public class ConstructForClass implements ExtensionFunction
{
    
    private final Processor processor;
    private final OntDocumentManager odm;
    
    public ConstructForClass(Processor processor)
    {
        this(processor, OntDocumentManager.getInstance());
    }
    
    public ConstructForClass(Processor processor, OntDocumentManager odm)
    {
        this.processor = processor;
        this.odm = odm;
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
            String ontology = arguments[0].itemAt(0).getStringValue();
            String base = arguments[2].itemAt(0).getStringValue();
            
            Model instances = ModelFactory.createDefaultModel();
            OntModel ontModel = getOntDocumentManager().getOntology(ontology, OntModelSpec.OWL_MEM);
            
            arguments[1].stream().
                map(clazz -> ontModel.getOntClass(checkURI(clazz.getStringValue()).toString())).
                forEach(forClass -> new Constructor().construct(forClass, instances, base));
            
            return getProcessor().newDocumentBuilder().build(getSource(instances));
        }
        catch (IOException ex)
        {
            throw new SaxonApiException(ex);
        }
    }
    
    public Processor getProcessor()
    {
        return processor;
    }
    
    public OntDocumentManager getOntDocumentManager()
    {
        return odm;
    }
    
}
