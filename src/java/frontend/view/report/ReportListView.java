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
import java.util.Arrays;
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
    public static final int ITEMS_PER_PAGE = 20;
    //public static final List<String> sortableVariables = new ArrayList<String>();
    public static enum SortableVariable { date, creator, endpoint }
    
    private Integer offset = 0;
    private Integer limit = ITEMS_PER_PAGE;
    private Boolean desc = true;
    private SortableVariable orderBy = SortableVariable.date;

    public ReportListView(ReportListResource resource)
    {
	super(resource);
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        applyPagination(new PaginationForm(request));
        
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/report/ReportListView.xsl")));
	
	String queryString = QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/report/reports.rq"), orderBy, offset, limit);
	String results = QueryXMLResult.select(SDB.getDataset(), queryString);

	setDocument(results);
	
	getResolver().setArgument("reports", results);
	
	super.display(request, response);
    }

    protected void applyPagination(PaginationForm form)
    {
        if (form.getOffset() != null) offset = form.getOffset();
        if (form.getLimit() != null) limit = form.getLimit();
        // desc!!!
        if (form.getOrderBy() != null
            //&& Arrays.asList(SortableVariables.values()).contains(SortableVariables.valueOf(form.getOrderBy()))
                ) orderBy = form.getOrderBy();
    }
}
