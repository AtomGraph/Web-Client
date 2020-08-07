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
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

/**
 * <code>ac:uuid()</code> XSLT function that generates a unique UUID string.
 * Plugs into Saxon processor.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 * @see <a href="http://www.saxonica.com/documentation/#!extensibility/integratedfunctions">Integrated extension functions</a>
 */
public class UUID implements ExtensionFunction
{
    
    @Override
    public QName getName()
    {
        return new QName(AC.NS, "uuid");
    }

    @Override
    public SequenceType getResultType()
    {
        return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
    }

    @Override
    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{};
    }

    @Override
    public XdmValue call(XdmValue[] arguments) throws SaxonApiException
    {
        return new XdmAtomicValue(java.util.UUID.randomUUID().toString());
    }

}
