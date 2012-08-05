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

package org.graphity.model.impl;

import com.hp.hpl.jena.rdf.model.Model;
import org.graphity.model.LinkedDataResource;
import org.graphity.util.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class LinkedDataResourceImpl implements LinkedDataResource
{
    private static final Logger log = LoggerFactory.getLogger(LinkedDataResourceImpl.class);

    private String uri = null;
    private Model model = null;
    
    public LinkedDataResourceImpl(String uri)
    {
	if (uri == null) throw new IllegalArgumentException("Linked Data URI must be not null");
	this.uri = uri;
	log.debug("URI: {}", uri);
    }

    @Override
    public Model getModel()
    {
	if (model == null)
	{
	    log.debug("Loading Model from URI: {}", getURI());
	    model = DataManager.get().loadModel(getURI());

	    log.debug("Number of Model stmts read: {}", model.size());
	}

	return model;
    }

    @Override
    public String getURI()
    {
	return uri;
    }

}
