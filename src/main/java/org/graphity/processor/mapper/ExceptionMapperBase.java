/*
 * Copyright 2014 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.processor.mapper;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import javax.ws.rs.core.Response;
import org.graphity.processor.vocabulary.HTTP;

/**
 * Abstract base class for ExceptionMappers that build responses with exceptions as RDF resources.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
abstract public class ExceptionMapperBase
{

    public Resource toResource(Exception ex, Response.Status status, Resource statusResource)
    {
	if (ex == null) throw new IllegalArgumentException("Exception cannot be null");
	if (status == null) throw new IllegalArgumentException("Response.Status cannot be null");
	//if (statusResource == null) throw new IllegalArgumentException("Status Resource cannot be null");

        Resource resource = ModelFactory.createDefaultModel().createResource().
                addProperty(RDF.type, HTTP.Response).
                addLiteral(HTTP.statusCodeValue, status.getStatusCode()).
                addLiteral(HTTP.reasonPhrase, status.getReasonPhrase());
                //addLiteral(ResourceFactory.createProperty("http://graphity.org/gm#message"), ex.getStackTrace());

        if (statusResource != null) resource.addProperty(HTTP.sc, statusResource);
        if (ex.getMessage() != null) resource.addLiteral(DCTerms.title, ex.getMessage());
        
        return resource;
    }

}