/*
 * ResourceMapping.java
 *
 * Created on Antradienis, 2007, Balandžio 17, 22.12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package controller;

import com.hp.hpl.jena.rdf.model.ResIterator;
import dk.semantic_web.diy.controller.Resource;
import frontend.controller.resource.datasource.DataSourceResource;
import frontend.controller.resource.report.ReportResource;

/**
 *
 * @author Pumba
 */
public class ResourceMapping extends dk.semantic_web.diy.controller.ResourceMapping
{    
    @Override
    public Resource findByURI(String uri)
    {
	Resource resource = null;
	//Individual instance = Ontology.getJointOntology().getIndividual(URI);
	String[] relativeUris = uri.split("/");
	// urldecode all URIs
	ResIterator iter = null;
	
	//if (relativeUris.length == 0) return FrontPageResource.getInstance();
	
	if (relativeUris.length == 1)
	{
	    if (relativeUris[0].equals(DataSourceResource.RELATIVE_URI))
	    {
		resource = DataSourceResource.getInstance();
		return resource;
	    }
	    if (relativeUris[0].equals(ReportResource.RELATIVE_URI))
	    {
		resource = ReportResource.getInstance();
		return resource;
	    }
	    
	}

        return resource;
    }
    
}
