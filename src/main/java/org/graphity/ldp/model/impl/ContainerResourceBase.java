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

import javax.ws.rs.core.UriInfo;
import org.graphity.ldp.model.ContainerResource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class ContainerResourceBase extends ResourceBase implements ContainerResource
{
    private Long limit = null;
    private Long offset = null;
    private String orderBy = null;
    private Boolean desc = true;
    
    public ContainerResourceBase(UriInfo uriInfo,
	Long limit, Long offset, String orderBy, Boolean desc)
    {
	super(uriInfo);
	this.limit = limit;
	this.offset = offset;
	this.orderBy = orderBy;
	this.desc = desc;
    }

    @Override
    public Long getLimit()
    {
	return this.limit;
    }

    @Override
    public Long getOffset()
    {
	return this.offset;
    }

    @Override
    public String getOrderBy()
    {
	return this.orderBy;
    }

    @Override
    public boolean getDesc()
    {
	return this.desc;
    }

}
