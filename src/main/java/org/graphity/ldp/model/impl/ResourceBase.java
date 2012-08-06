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
package org.graphity.ldp.model.impl;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.graphity.ldp.model.Resource;
import org.graphity.util.ModelUtils;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class ResourceBase implements Resource
{
    private UriInfo uriInfo = null;

    public ResourceBase(UriInfo uriInfo)
    {
	this.uriInfo = uriInfo;
    }
    
    @Override
    public final String getURI()
    {
	return uriInfo.getAbsolutePath().toString();
    }

    @Override
    public Response getResponse()
    {
	return Response.ok(getModel()).tag(getEntityTag()).build(); // uses ModelProvider
    }

    @Override
    public EntityTag getEntityTag()
    {
	return new EntityTag(Long.toHexString(ModelUtils.hashModel(getModel())));
    }

}
