/*
 * ResourceMapping.java
 *
 * Created on Antradienis, 2007, Balandžio 17, 22.12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package controller;

import dk.semantic_web.diy.controller.Resource;
import frontend.controller.resource.report.ReportListResource;
import frontend.controller.resource.report.ReportResource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import model.Report;
import thewebsemantic.RDF2Bean;

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
	for (int i = 0; i < relativeUris.length; i++)
	    try
	    {
		relativeUris[i] = URLDecoder.decode(relativeUris[i], "UTF-8");
	    }
	    catch (UnsupportedEncodingException ex)
	    {
	
	    }
	
	//if (relativeUris.length == 0) return ReportResource.getInstance();
	
	if (relativeUris.length == 1)
	{
	    if (relativeUris[0].equals(ReportListResource.RELATIVE_URI))
	    {
		//RDF2Bean reader = new RDF2Bean(myModel);
		resource = ReportListResource.getInstance();
		if (relativeUris.length >= 2)
		{
		    Report report = null; //ConceptSchemePeer.retrieveByID(relativeUris[1]);
		    if (report != null) return new ReportResource(report, (ReportListResource)resource);
		    return null;
		}
		return resource;
	    
	}

        return resource;
    }

}
