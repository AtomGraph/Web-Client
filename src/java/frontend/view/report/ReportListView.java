/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import frontend.controller.form.PaginationForm;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import view.QueryStringBuilder;
import view.QueryXMLResult;

/**
 *
 * @author Pumba
 */
public class ReportListView extends FrontEndView
{
    public static final int ITEMS_PER_PAGE = 15;
    //public static final List<String> sortableVariables = new ArrayList<String>();
    public static enum SortableVariable { DATE, CREATOR, ENDPOINT }
    
    private Integer offset = 0;
    private Integer limit = ITEMS_PER_PAGE;
    private Boolean desc = true;
    private SortableVariable orderBy = SortableVariable.DATE;

    public ReportListView(ReportListResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        applyPagination(new PaginationForm(request));
        
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportListView.xsl")));
	
	String queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/list/reports.rq"), getOrderBy().toString().toLowerCase(), getOffset(), getLimit());
	String results = QueryXMLResult.select(SDB.getDataset(), queryString);

	setDocument(results);
	
	getResolver().setArgument("reports", results);

        getTransformer().setParameter("total-item-count", 2); //    SDB.getReportClass().listInstances().toList().size()
        getTransformer().setParameter("offset", getOffset());
        getTransformer().setParameter("limit", getLimit());
        getTransformer().setParameter("order-by", getOrderBy().toString().toLowerCase());
        getTransformer().setParameter("desc-default", true);
        getTransformer().setParameter("desc", getDesc());

        setQueryObjects(request, response);
        
	super.display(request, response);
    }

    protected void applyPagination(PaginationForm form)
    {
        if (form.getOffset() != null) offset = form.getOffset();
        if (form.getLimit() != null) limit = form.getLimit();
        // desc!!!
        //if (form.getOrderBy() != null) orderBy = form.getOrderBy();
            //&& Arrays.asList(SortableVariables.values()).contains(SortableVariables.valueOf(form.getOrderBy()))
    }

    public Boolean getDesc()
    {
        return desc;
    }

    public void setDesc(Boolean desc)
    {
        this.desc = desc;
    }

    public Integer getLimit()
    {
        return limit;
    }

    public void setLimit(Integer limit)
    {
        this.limit = limit;
    }

    public Integer getOffset()
    {
        return offset;
    }

    public void setOffset(Integer offset)
    {
        this.offset = offset;
    }

    public SortableVariable getOrderBy()
    {
        return orderBy;
    }

    public void setOrderBy(SortableVariable orderBy)
    {
        this.orderBy = orderBy;
    }

    protected void setQueryObjects(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
	String objects = QueryXMLResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/list/objects.rq")));

	getResolver().setArgument("query-objects", objects);
    }
}
