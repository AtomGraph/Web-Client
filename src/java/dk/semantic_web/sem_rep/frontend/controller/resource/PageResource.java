/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource;

import dk.semantic_web.sem_rep.controller.LeafResource;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.sem_rep.frontend.controller.FrontEndResource;
import dk.semantic_web.sem_rep.frontend.view.PageView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.sem_rep.model.Page;

/**
 *
 * @author Pumba
 */
public class PageResource extends FrontEndResource implements LeafResource
{
    private Page page = null;

    public PageResource(Page page, FrontPageResource parent)
    {
        super(parent);
        this.page = page;
    }

    @Override
    public String getRelativeURI()
    {
	try
	{
	    return URLEncoder.encode(page.getName(), "UTF-8");
	} catch (UnsupportedEncodingException ex)
	{
	    Logger.getLogger(FrontPageResource.class.getName()).log(Level.SEVERE, null, ex);
	}
	return page.getName();
    }

    public Page getPage()
    {
        return page;
    }

    @Override
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

        return new PageView(this);
    }

}
