/*
 * Copyright (C) 2014 Martynas
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

package org.graphity.processor.provider;

import com.hp.hpl.jena.query.Dataset;
import javax.ws.rs.ext.ContextResolver;
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.model.SPARQLEndpointOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class SPARQLEndpointProvider extends org.graphity.server.provider.SPARQLEndpointProvider
{
    
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointProvider.class);

    public Dataset getDataset()
    {
	ContextResolver<Dataset> cr = getProviders().getContextResolver(Dataset.class, null);
	return cr.getContext(Dataset.class);
    }

    @Override
    public SPARQLEndpoint getSPARQLEndpoint()
    {
        SPARQLEndpointOrigin origin = getSPARQLEndpointOrigin();
        if (origin != null) // use proxy for remote endpoint
        {
            return super.getSPARQLEndpoint();
        }
        else // use local endpoint
        {
            return SPARQLEndpointFactory.create(getRequest(), getServletContext(), getDataset(), getDataManager());
        }
    }

}
