package org.graphity;



import org.graphity.Resource;
import javax.ws.rs.core.Response;

public abstract class View extends Response
{
    private Resource resource = null;
    
    public View(Resource resource)
    {
	this.resource = resource;
    }

    public Resource getResource()
    {
	return resource;
    }

    public void setResource(Resource resource)
    {
	this.resource = resource;
    }

    //public abstract void display(HttpServletRequest request, OutputStream out) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException;

    /*
    @Override
    public Object getEntity()
    {
	return getResponse().getEntity();
    }

    @Override
    public int getStatus()
    {
	return getResponse().getStatus();
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
	return getResponse().getMetadata();
    }
    */
}
