/*
 * ResourceMapping.java
 *
 * Created on Antradienis, 2007, Balandžio 17, 22.12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.controller;

import dk.semantic_web.diy.controller.Resource;
import dk.semantic_web.sem_rep.frontend.controller.resource.FrontPageResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.PageResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.SPARQLResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.endpoint.EndpointListResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.endpoint.EndpointResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.report.ReportListResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.report.ReportResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.visualization.VisualizationEmbedResource;
import dk.semantic_web.sem_rep.frontend.controller.resource.visualization.VisualizationListResource;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import dk.semantic_web.sem_rep.model.Endpoint;
import dk.semantic_web.sem_rep.model.Page;
import dk.semantic_web.sem_rep.model.PagePeer;
import dk.semantic_web.sem_rep.model.Report;
import dk.semantic_web.sem_rep.model.sdb.SDB;
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
	String[] relativeUris = uri.split("/");
	
	//if (relativeUris.length == 0) return ReportResource.getInstance();
	
	if (relativeUris.length >= 1)
	{
	    if (relativeUris[0].equals(ReportListResource.getInstance().getPath()))
	    {
		resource = ReportListResource.getInstance();
		if (relativeUris.length >= 2)
		{
		    String fullUri = getHost() + resource.getAbsolutePath() + relativeUris[1];
                    RDF2Bean reader = new RDF2Bean(SDB.getInstanceModel());
                    reader.bindAll("model");
		    Report report = reader.load(Report.class, fullUri);

		    if (report != null)
                    {
                        report.setId(relativeUris[1]);
                        return new ReportResource(report, (ReportListResource)resource);
                    }
		    return null;
		}
		return resource;
	    }
	    if (relativeUris[0].equals(EndpointListResource.getInstance().getPath()))
	    {
		resource = EndpointListResource.getInstance();
		if (relativeUris.length >= 2)
		{
		    String fullUri = urlDecode(relativeUris[1]);
                    RDF2Bean reader = new RDF2Bean(SDB.getInstanceModel());
                    reader.bindAll("model");
		    Endpoint endpoint = reader.load(Endpoint.class, fullUri);

		    if (endpoint != null)
                    {
			try
			{
			    endpoint.setURI(new URI(fullUri));
			} catch (URISyntaxException ex)
			{
			    Logger.getLogger(ResourceMapping.class.getName()).log(Level.SEVERE, null, ex);
			}
                        return new EndpointResource(endpoint, (EndpointListResource)resource);
                    }
		    return null;
		}
		return resource;
	    }
	    if (relativeUris[0].equals(VisualizationListResource.getInstance().getPath()))
	    {
		resource = VisualizationListResource.getInstance();
		if (relativeUris.length >= 2)
		{
		    if (relativeUris[1].equals(VisualizationEmbedResource.getInstance().getPath())) return VisualizationEmbedResource.getInstance();

		    return null;
		}
		return resource;
	    }
	    if (relativeUris[0].equals(SPARQLResource.getInstance().getPath())) return SPARQLResource.getInstance();

            Page page = PagePeer.doSelectByName(relativeUris[0]);
            //System.out.println(page.getName()); // page can be null => null pointer exception
            if (page != null) return new PageResource(page, FrontPageResource.getInstance());
	}

        return null;
    }

    public static String urlDecode(String url)
    {
	try
	{
	    return URLDecoder.decode(url, "UTF-8");
	}
	catch (UnsupportedEncodingException ex)
	{
	    ex.printStackTrace(System.out);
	}
	return url;
    }

}
