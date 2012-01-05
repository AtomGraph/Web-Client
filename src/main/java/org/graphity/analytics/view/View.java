package org.graphity.analytics.view;



import javax.ws.rs.core.Response;
import org.graphity.Resource;

public abstract class View extends Response
{
    private Resource resource = null;
    
    public View(Resource resource)
    {
	this.resource = resource;
    }
}
