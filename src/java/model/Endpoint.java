/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import frontend.controller.resource.endpoint.EndpointResource;
import java.net.URI;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Transient;
import thewebsemantic.binding.RdfBean;
import static model.vocabulary.DublinCore.*;

/**
 *
 * @author Pumba
 */

@Namespace("http://www.semantic-web.dk/ontologies/semantic-reports/")
public class Endpoint extends RdfBean<Endpoint>
{
    private EndpointResource resource = null;
    private URI uri = null;
    private String title = null;

    public Endpoint()
    {
    }

    public Endpoint(URI uri)
    {
	setURI(uri);
    }

    @Id
    public URI getURI()
    {
	return uri;
    }

    public void setURI(URI uri)
    {
	this.uri = uri;
    }

    @Transient
    public EndpointResource getFrontEndResource()
    {
	return resource;
    }

    public void setFrontEndResource(EndpointResource resource)
    {
	this.resource = resource;
    }

    @RdfProperty(TITLE)
    public String getTitle()
    {
	return title;
    }

    public void setTitle(String title)
    {
	this.title = title;
    }

}
