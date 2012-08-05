/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.util.locator.grddl;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.util.locator.LocatorGRDDL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LocatorAtom extends LocatorGRDDL
{
    private static final Logger log = LoggerFactory.getLogger(LocatorAtom.class);

    public LocatorAtom(Source stylesheet) throws TransformerConfigurationException
    {
	super(stylesheet);
    }

    @Override
    public String getName()
    {
	return "LocatorAtom";
    }

    @Override
    public Map<String, Double> getQualifiedTypes()
    {
	Map<String, Double> xmlType = new HashMap<String, Double>();
	xmlType.put("application/atom+xml", null);
	return xmlType;
    }

}
