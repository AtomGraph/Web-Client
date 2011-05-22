/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.endpoint;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.vocabulary.RDF;
import dk.semantic_web.rdf_editor.frontend.view.FrontEndView;
import dk.semantic_web.rdf_editor.view.QueryResult;
import dk.semantic_web.rdf_editor.view.QueryStringBuilder;
import dk.semantic_web.rdf_editor.view.XMLSerializer;
import dk.semantic_web.sem_rep.frontend.controller.resource.endpoint.EndpointListResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import dk.semantic_web.sem_rep.model.vocabulary.Reports;

/**
 *
 * @author Pumba
 */
public class EndpointListView extends FrontEndView
{

    public EndpointListView(EndpointListResource resource) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
	super(resource);
    }

    @Override
    protected String getStyleSheetPath() {
        return XSLT_BASE + "endpoint/" + getClass().getSimpleName() + ".xsl";
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
        setEndpoints(QueryResult.select(dk.semantic_web.rdf_editor.model.Model.getInstance().getDataset(), QueryStringBuilder.build(getController().getServletContext().getResourceAsStream("/WEB-INF/sparql/endpoint/list/endpoints.rq"))));

        int count = dk.semantic_web.rdf_editor.model.Model.getInstance().getData().listResourcesWithProperty(RDF.type,
		dk.semantic_web.rdf_editor.model.Model.getInstance().getData().createResource(Reports.Report)).toList().size();

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
