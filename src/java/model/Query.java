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
    private String lastResult = null;

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

    @RdfProperty("http://www.semantic-web.dk/ontologies/semantic-reports/lastResult")
    public String getLastResult()
    {
	return lastResult;
    }

    public void setLastResult(String lastResult)
    {
	this.lastResult = lastResult;
    }

    @Id
    public URI getURI()
    {
	try
	{
	    String uri = "http://temp.com/query/123"; // QUIRK!!!

	    return new URI(uri);
	} catch (URISyntaxException ex)
	{
	    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
}
