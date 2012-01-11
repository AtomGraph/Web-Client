package org.graphity.analytics;



import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.graphity.RDFResource;

public class View extends Response
{    
    //private Resource resource = null;
    private Response response = null;
    
    public View(RDFResource resource)
    {
	response = Response.ok(resource).build();
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