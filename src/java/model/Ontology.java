/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 *
 * @author Pumba
 */
public class Ontology
{
    private static OntModel model = ModelFactory.createOntologyModel();
    
    public static void init()
    {

    }

    public static OntModel getModel()
    {
	return model;
    }
    
    
}
