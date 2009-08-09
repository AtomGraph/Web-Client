/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import thewebsemantic.Namespace;
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
    private String endpoint = null;

    public String getEndpoint()
    {
	return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
	this.endpoint = endpoint;
    }

    public String getQueryString()
    {
	return queryString;
    }

    public void setQueryString(String queryString)
    {
	this.queryString = queryString;
    }
    
}
