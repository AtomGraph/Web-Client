/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.report;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.vocabulary.RDF;
import frontend.controller.form.PaginationForm;
import frontend.controller.resource.report.ReportListResource;
import frontend.view.FrontEndView;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.sdb.SDB;
import model.vocabulary.Reports;
import view.QueryStringBuilder;
import view.QueryResult;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class ReportListView extends FrontEndView
{
    public static final int ITEMS_PER_PAGE = 10;
    public static enum SortableVariable
    { CREATED("dateCreated"), MODIFIED("dateModified"), CREATOR("creator"), ENDPOINT("endpoint");

        private final String name;

        SortableVariable(String name)
        {
            this.name = name;
        }

        public final String getName()
        {
            return name;
        }

        @Override
        public final String toString()
        {
            return getName();
        }
    }
    
    private Integer offset = 0;
    private Integer limit = ITEMS_PER_PAGE;
    private Boolean desc = true;
    private SortableVariable orderBy = SortableVariable.CREATED;

    public ReportListView(ReportListResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
        setStyleSheet(getController().getServletContext().getResource(XSLT_PATH + "report/" + getClass().getSimpleName() + ".xsl").toURI().toString());
    }
    
    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        applyPagination(new PaginationForm(request));
        	
	String queryString = QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/list/reports.rq"), getOrderBy().toString(), getOffset(), getLimit());
	setReports(QueryResult.select(SDB.getDataset(), queryString));

        int count = SDB.getInstanceModel().listResourcesWithProperty(RDF.type, SDB.getInstanceModel().createResource(Reports.Report)).toList().size();

        getTransformer().setParameter("total-item-count", count); //    SDB.getReportClass().listInstances().toList().size()
        getTransformer().setParameter("offset", getOffset());
        getTransformer().setParameter("limit", getLimit());
        getTransformer().setParameter("order-by", getOrderBy().toString()); // getOrderBy().toString().toLowerCase()
        getTransformer().setParameter("desc-default", true);
        getTransformer().setParameter("desc", getDesc());

//        setQueryObjects(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/list/objects.rq"))));
        setQueryUris(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/report/list/uris.rq"))));
        setEndpoints(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/list/endpoints.rq"))));

	super.display(request, response);
    }

    protected void applyPagination(PaginationForm form)
    {
        if (form.getOffset() != null) setOffset(form.getOffset());
        if (form.getLimit() != null) setLimit(form.getLimit());
        // desc!!!
        //if (form.getOrderBy() != null) setOrderBy(form.getOrderBy());
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

    protected void setReports(ResultSetRewindable reports)
    {
	setDocument(XMLSerializer.serialize(reports));

	getResolver().setArgument("reports", XMLSerializer.serialize(reports));
    }

    protected void setQueryObjects(ResultSetRewindable objects)
    {
	getResolver().setArgument("query-objects", XMLSerializer.serialize(objects));
    }

    protected void setQueryUris(ResultSetRewindable uris)
    {
	getResolver().setArgument("query-uris", XMLSerializer.serialize(uris));
    }

    protected void setEndpoints(ResultSetRewindable endpoints)
    {
	getResolver().setArgument("endpoints", XMLSerializer.serialize(endpoints));
    }

}