/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.view.endpoint;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.vocabulary.RDF;
import frontend.controller.FrontEndResource;
import frontend.view.FrontEndView;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import model.SDB;
import model.vocabulary.Reports;
import view.QueryResult;
import view.QueryStringBuilder;
import view.XMLSerializer;

/**
 *
 * @author Pumba
 */
public class EndpointListView extends FrontEndView
{

    public EndpointListView(FrontEndResource resource) throws TransformerConfigurationException
    {
	super(resource);
	setStyleSheet(new File(getController().getServletConfig().getServletContext().getRealPath("/xslt/endpoint/EndpointListView.xsl")));
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setEndpoints(QueryResult.select(SDB.getDataset(), QueryStringBuilder.build(getController().getServletConfig().getServletContext().getRealPath("/sparql/endpoint/list/endpoints.rq"))));

        int count = SDB.getInstanceModel().listResourcesWithProperty(RDF.type, SDB.getInstanceModel().createResource(Reports.Report)).toList().size();

        getTransformer().setParameter("total-item-count", count); //    SDB.getReportClass().listInstances().toList().size()
	/*
        getTransformer().setParameter("offset", getOffset());
        getTransformer().setParameter("limit", getLimit());
        getTransformer().setParameter("order-by", getOrderBy().toString()); // getOrderBy().toString().toLowerCase()
        getTransformer().setParameter("desc-default", true);
        getTransformer().setParameter("desc", getDesc());
	*/

	super.display(request, response);
    }

    protected void setEndpoints(ResultSetRewindable endpoints)
    {
	setDocument(XMLSerializer.serialize(endpoints));
	//getResolver().setArgument("endpoints", XMLSerializer.serialize(endpoints));
    }

}
