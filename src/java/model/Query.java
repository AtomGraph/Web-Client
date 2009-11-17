/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.RdfType;
import thewebsemantic.binding.RdfBean;

/**
 *
 * @author Pumba
 */
@Namespace("http://spinrdf.org/sp#")
@RdfType("Select")
public class Query extends RdfBean<Query>
{
    private String queryString = null;
    private URI endpoint = null;

    @RdfProperty("http://spinrdf.org/sp#from")
    public URI getEndpoint()
    {
	return endpoint;
    }

    public void setEndpoint(URI endpoint)
    {
	this.endpoint = endpoint;
    }

    @RdfProperty("http://spinrdf.org/sp#text")
    public String getQueryString()
    {
	return queryString;
    }

    public void setQueryString(String queryString)
    {
	this.queryString = queryString;
    }

    @Id
    public URI getURI()
    {
	try
	{
	    String uri = "http://temp.com/query/123";

	    return new URI(uri);
	} catch (URISyntaxException ex)
	{
	    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
}
