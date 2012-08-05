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
package org.graphity.ldp.model;

import com.hp.hpl.jena.rdf.model.Model;
import javax.ws.rs.*;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import org.graphity.MediaType;
import org.graphity.model.LinkedDataResource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Consumes({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
@Produces({MediaType.APPLICATION_RDF_XML + "; charset=UTF-8", MediaType.TEXT_TURTLE + "; charset=UTF-8"})
public interface Resource extends LinkedDataResource
{
    @GET Response getResponse();
    
    @POST Response post(Model model);

    @PUT Response put(Model model);
    
    @DELETE Response delete();
    
    EntityTag getEntityTag();
    
}
