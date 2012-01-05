package org.graphity.analytics;



import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.graphity.Resource;

public class View extends Response.ResponseBuilder
{
    private Resource resource = null;
    
    public View(Resource resource)
    {
	this.resource = resource;
	Response.
    }

    @Override
    public Object getEntity()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getStatus()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
