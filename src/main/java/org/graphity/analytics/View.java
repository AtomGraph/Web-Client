package org.graphity.analytics;



import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.RDFResource;

public class View extends Response
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
    
    //private Resource resource = null;
    private Response response = null;
    
    public View(RDFResource resource) throws TransformerConfigurationException
    {
	response = Response.ok(resource.getURI()).
		entity(resource.getModel()).
		build();
    }

    @Override
    public Object getEntity()
    {
	return response.getEntity();
    }

    @Override
    public int getStatus()
    {
	return response.getStatus();
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
	return response.getMetadata();
    }

}