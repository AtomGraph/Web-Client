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

package org.graphity.model;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import org.graphity.model.impl.LinkedDataResourceImpl;
import org.graphity.model.impl.QueriedResourceImpl;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ResourceFactory
{
    public static Resource getResource(String uri)
    {
	return new LinkedDataResourceImpl(uri);
    }
    
    public static Resource getResource(String endpointUri, Query query)
    {
	return new QueriedResourceImpl(endpointUri, query);
    }

    public static Resource getResource(String endpointUri, String uri)
    {
	return new QueriedResourceImpl(endpointUri, uri);
    }

    public static Resource getResource(Model model, Query query)
    {
	return new QueriedResourceImpl(model, query);
    }

    public static Resource getResource(Model model, String uri)
    {
	return new QueriedResourceImpl(model, uri);
    }

}
