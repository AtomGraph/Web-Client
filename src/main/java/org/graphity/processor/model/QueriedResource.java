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

package org.graphity.processor.model;

import org.graphity.processor.query.QueryBuilder;

/**
 * RDF resource, representation of which was queried from a SPARQL endpoint.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.query.QueryBuilder
 */
public interface QueriedResource extends org.graphity.core.model.QueriedResource
{
    
    /**
     * Returns query builder, which is used to build SPARQL query to retrieve RDF description of this resource.
     * 
     * @return query builder
     */
    public QueryBuilder getQueryBuilder();

}
