/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.client.writer;

import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import org.graphity.client.util.DataManager;
import org.graphity.processor.exception.ConstraintViolationException;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ConstraintViolationExceptionURIResolver implements URIResolver
{
    
    private final ConstraintViolationException cve;
    private final DataManager dataManager;
    
    public ConstraintViolationExceptionURIResolver(ConstraintViolationException cve, DataManager dataManager)
    {
        this.cve = cve;
        this.dataManager = dataManager;
    }
    
    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
        URI uri = URI.create(base).resolve(href);        
        if (uri.equals(URI.create("gc://" + getConstraintViolationException().hashCode())))
        {
            return getDataManager().getSource(getConstraintViolationException().getModel(), null);
        }
        
        return getDataManager().resolve(href, base);
    }

    public ConstraintViolationException getConstraintViolationException()
    {
        return cve;
    }
    
    public DataManager getDataManager()
    {
        return dataManager;
    }
    
}
