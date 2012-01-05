/*
 * Form.java
 *
 * Created on Pirmadienis, 2007, Sausio 29, 13.34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.graphity;

import java.util.List;

/**
 * Represents form data submitted with a HTTP request.
 * @author Pumba
 */
public interface Form
{        
    /** Validates the form data (submitted with the request) against constraints
    @return The list of errors (constraints not met)
    */
    public abstract List<Exception> validate();
}
