/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity;

import java.io.OutputStream;

/**
 *
 * @author Pumba
 */
public interface RDFResource extends Resource, com.hp.hpl.jena.rdf.model.Resource
{
    public OutputStream describe(); // return RDF/XML
}
