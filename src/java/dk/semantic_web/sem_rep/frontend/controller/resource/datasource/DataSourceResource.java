/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.datasource;

import dk.semantic_web.diy.controller.Resource;
import dk.semantic_web.diy.controller.Singleton;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.sem_rep.frontend.controller.resource.FrontPageResource;
import dk.semantic_web.sem_rep.frontend.view.datasource.DataSourceView;

/**
 *
 * @author Pumba
 */
public class DataSourceResource extends FrontEndResource implements Singleton
{
    public static final String RELATIVE_URI = "sparql-xml2google-wire";
    private static final DataSourceResource INSTANCE = new DataSourceResource(FrontPageResource.getInstance());
    
    private View view = null;
    
    private DataSourceResource(FrontPageResource parent)
    {
	super(parent);
    }

    public static Resource getInstance()
    {
	return INSTANCE;
    }
    
    @Override
    public String getRelativeURI()
    {
	return RELATIVE_URI;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) view = parent;
	else view = new DataSourceView(this);

	return view;
    }

}
