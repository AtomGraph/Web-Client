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
package org.graphity.client.locator.grddl;

import com.sun.jersey.api.uri.UriTemplate;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import org.graphity.client.locator.LocatorGRDDL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Atom-specific GRDDL Locator
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LocatorAtom extends LocatorGRDDL
{
    private static final Logger log = LoggerFactory.getLogger(LocatorAtom.class);

    public LocatorAtom(String uriTemplate, Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	super(uriTemplate, stylesheet, resolver);
    }

    public LocatorAtom(UriTemplate uriTemplate, Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	super(uriTemplate, stylesheet, resolver);
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
