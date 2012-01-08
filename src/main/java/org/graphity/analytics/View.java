package org.graphity.analytics;



import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;
import javax.xml.transform.TransformerConfigurationException;
import org.graphity.RDFResource;

public class View extends ResponseBuilder
{
    public static final String XSLT_BASE = "/WEB-INF/xsl/";
    
    //private Resource resource = null;
    private ResponseBuilder builder = null;
    
    public View(RDFResource resource) throws TransformerConfigurationException
    {
	builder = Response.ok(resource.getURI()).entity(resource.getModel());
    }

    @Override
    public Response build()
    {
	return builder.build();
    }

    @Override
    public ResponseBuilder clone()
    {
	return builder.clone();
    }

    @Override
    public ResponseBuilder status(int status)
    {
	return builder.status(status);
    }

    @Override
    public ResponseBuilder entity(Object entity)
    {
	return builder.entity(entity);
    }

    @Override
    public ResponseBuilder type(MediaType type)
    {
	return builder.type(type);
    }

    @Override
    public ResponseBuilder type(String type)
    {
	return builder.type(type);
    }

    @Override
    public ResponseBuilder variant(Variant variant)
    {
	return builder.variant(variant);
    }

    @Override
    public ResponseBuilder variants(List<Variant> variants)
    {
	return builder.variants(variants);
    }

    @Override
    public ResponseBuilder language(String language)
    {
	return builder.language(language);
    }

    @Override
    public ResponseBuilder language(Locale language)
    {
	return builder.language(language);
    }

    @Override
    public ResponseBuilder location(URI location)
    {
	return builder.location(location);
    }

    @Override
    public ResponseBuilder contentLocation(URI location)
    {
	return builder.contentLocation(location);
    }

    @Override
    public ResponseBuilder tag(EntityTag tag)
    {
	return builder.tag(tag);
    }

    @Override
    public ResponseBuilder tag(String tag)
    {
	return builder.tag(tag);
    }

    @Override
    public ResponseBuilder lastModified(Date lastModified)
    {
	return builder.lastModified(lastModified);
    }

    @Override
    public ResponseBuilder cacheControl(CacheControl cacheControl)
    {
	return builder.cacheControl(cacheControl);
    }

    @Override
    public ResponseBuilder expires(Date expires)
    {
	return builder.expires(expires);
    }

    @Override
    public ResponseBuilder header(String name, Object value)
    {
	return builder.header(name, value);
    }

    @Override
    public ResponseBuilder cookie(NewCookie... cookies)
    {
	return builder.cookie(cookies);
    }
}