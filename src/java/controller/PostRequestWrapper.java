/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import frontend.controller.form.RDFForm;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Pumba
 */
public class PostRequestWrapper extends HttpServletRequestWrapper
{
    private String queryString = null;
    Map paramMap = new HashMap<String, String>();
    
    public PostRequestWrapper(HttpServletRequest request)
    {
        super(request);
        setQueryString(getRequestBody());
        setParameterMap(processParameters(getQueryString()));
    }

    @Override
    public String getQueryString()
    {
        return queryString;
    }

    private void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

    private String getRequestBody()
    {
        StringWriter writer = new StringWriter();
        String body = null;
        try
        {
            IOUtils.copy(getReader(), writer);
            body = writer.toString();
        } catch (IOException ex) {
            Logger.getLogger(PostRequestWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return body;
    }

    private Map processParameters(String queryString)
    {
	String[] params = queryString.split("&");
        HashMap<String, String> map = new HashMap<String, String>();

	for (String param : params)
	{
	    System.out.println(param);
	    String[] array = param.split("=");
	    String key = ResourceMapping.urlDecode(array[0]);
	    String value = null;
	    if (array.length > 1) value = ResourceMapping.urlDecode(array[1]);
            map.put(key, value);
	}
        return map;
    }

    @Override
    public Map getParameterMap()
    {
        return paramMap;
    }

    private void setParameterMap(Map paramMap)
    {
        this.paramMap = paramMap;
    }

    @Override
    public String getParameter(String name)
    {
        return (String)getParameterMap().get(name);
    }
}
