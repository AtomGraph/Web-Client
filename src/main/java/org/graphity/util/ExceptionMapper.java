/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util;

import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerConfigurationException;

/**
 *
 * @author Pumba
 */
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<TransformerConfigurationException>
{

    @Override
    public Response toResponse(TransformerConfigurationException exception)
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
