/*
 * Copyright 2013 Martynas Jusevičius <martynas@graphity.org>.
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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import java.util.List;
import org.topbraid.spin.constraints.ConstraintViolation;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ConstraintViolationException extends RuntimeException
{
    private final List<ConstraintViolation> cvs;
    private final Model model;
    private final OntClass matchedOntClass;
    
    public ConstraintViolationException(List<ConstraintViolation> cvs, Model model, OntClass matchedOntClass)
    {
	this.cvs = cvs;
	this.model = model;
        this.matchedOntClass = matchedOntClass;
    }

    public List<ConstraintViolation> getConstraintViolations()
    {
	return cvs;
    }

    public Model getModel()
    {
	return model;
    }
    
    public OntClass getMatchedOntClass()
    {
        return matchedOntClass;
    }
    
}
