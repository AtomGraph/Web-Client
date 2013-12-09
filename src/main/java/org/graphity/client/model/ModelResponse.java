/*
 * Copyright (C) 2013 Martynas
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

package org.graphity.client.model;

import java.net.URI;
import java.util.List;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas
 */
public class ModelResponse extends org.graphity.server.model.ModelResponse
{
    private static final Logger log = LoggerFactory.getLogger(ModelResponse.class);

    @Context @QueryParam("mode") URI mode;

    protected ModelResponse(Request request)
    {
        super(request);
    }

    public static ModelResponse fromRequest(Request request)
    {
	return new ModelResponse(request);
    }

    public URI getMode()
    {
        return mode;
    }

    @Override
    public List<Variant> getVariants()
    {
        // workaround for Saxon-CE - it currently seems to run only in HTML mode (not XHTML)
        // https://saxonica.plan.io/issues/1447
	if (getMode() != null)
	{
	    if (log.isDebugEnabled()) log.debug("Mode is {}, returning 'text/html' media type as Saxon-CE workaround", getMode());
	    List<Variant> list = super.getVariants();
            list.add(0, new Variant(MediaType.TEXT_HTML_TYPE, null, null));
            return list;
	}

        List<Variant> list = super.getVariants();
        list.add(0, new Variant(MediaType.APPLICATION_XHTML_XML_TYPE, null, null));
        return list;
    }
    
}
