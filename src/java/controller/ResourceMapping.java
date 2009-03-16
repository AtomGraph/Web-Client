/*
 * ResourceMapping.java
 *
 * Created on Antradienis, 2007, Balandžio 17, 22.12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package controller;

import frontend.controller.resource.chart.ChartResource;
import lt.xml.diy.controller.ResourceImpl;

/**
 *
 * @author Pumba
 */
public class ResourceMapping extends lt.xml.diy.controller.ResourceMapping
{    
    @Override
    public ResourceImpl findByURI(String uri)
    {
        ResourceImpl resource = null;
        System.out.println(uri);

	//if (uri.equals("")) resource = new ChartResource(null);
resource = new ChartResource(null);
        /*
	if (uri.matches("^emneplan/$")) resource = new MainGroupListResource(uri);
        if (uri.matches("^facetter/(?:[A-Z]|%C3%86|%C3%98|%C3%85)/$"))	
        if (uri.matches("^emneplan/\\d+/$"))
	if (uri.matches("^s%C3%B8gning$")) resource = new SearchResource(uri);
        if (uri.matches("^admin$")) resource = new AdminResource(uri);
	*/

        return resource;
    }
    
}
