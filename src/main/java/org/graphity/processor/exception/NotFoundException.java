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

package org.graphity.processor.exception;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class NotFoundException extends RuntimeException // extends javax.ws.rs.WebApplicationException
{

    public NotFoundException(String message)
    {
        super(message); // super(Response.Status.NOT_FOUND);
    }
    
}