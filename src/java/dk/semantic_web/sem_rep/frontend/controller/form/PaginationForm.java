/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.form;

import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.diy.controller.Form;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pumba
 */
public class PaginationForm extends Form
{
    private Integer offset = null;
    private Integer limit = null;
    private Boolean asc = null;
    private Boolean desc = null;
    private String orderBy = null;

    public PaginationForm(HttpServletRequest request)
    {
        super(request);
        if (request.getParameter("offset") != null) offset = Integer.parseInt(request.getParameter("offset"));
        if (request.getParameter("limit") != null) limit = Integer.parseInt(request.getParameter("limit"));
        if (request.getParameter("asc") != null) asc = Boolean.parseBoolean(request.getParameter("asc"));
        if (request.getParameter("desc") != null) desc = Boolean.parseBoolean(request.getParameter("desc"));
        orderBy = request.getParameter("order-by");
    }

    public Boolean getAsc()
    {
        return asc;
    }

    public Boolean getDesc()
    {
        return desc;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public Integer getOffset()
    {
        return offset;
    }

    public String getOrderBy()
    {
        return orderBy;
    }

    @Override
    public List<Error> validate()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
