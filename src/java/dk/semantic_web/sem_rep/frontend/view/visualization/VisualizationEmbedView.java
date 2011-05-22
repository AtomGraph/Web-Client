/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.visualization;

import com.hp.hpl.jena.query.ResultSetRewindable;
import dk.semantic_web.rdf_editor.frontend.controller.FrontEndResource;
import dk.semantic_web.rdf_editor.frontend.view.FrontEndView;
import dk.semantic_web.rdf_editor.view.QueryResult;
import dk.semantic_web.rdf_editor.view.XMLSerializer;
import dk.semantic_web.sem_rep.frontend.controller.form.EmbedForm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Pumba
 */
public class VisualizationEmbedView extends FrontEndView
{
    private ResultSetRewindable queryResults = null;
    private EmbedForm form = null;

    public VisualizationEmbedView(FrontEndResource resource) throws MalformedURLException, TransformerConfigurationException, URISyntaxException
    {
	super(resource);
    }

    @Override
    protected String getStyleSheetPath() {
        return XSLT_BASE + "visualization/" + getClass().getSimpleName() + ".xsl";
    }

    public ResultSetRewindable getQueryResults()
    {
        return queryResults;
    }

    public void setQueryResults(ResultSetRewindable queryResults)
    {
        this.queryResults = queryResults;
    }

    @Override
    public void display(HttpServletRequest request, HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException
    {
	setQueryResults(QueryResult.selectRemote(getEmbedForm().getEndpointUri(), getEmbedForm().getQueryString(), 50));

        getResolver().setArgument("results", XMLSerializer.serialize(getQueryResults()));

	super.display(request, response);

	response.setStatus(HttpServletResponse.SC_OK);
    }

    public void setEmbedForm(EmbedForm form)
    {
	this.form = form;
    }

    public EmbedForm getEmbedForm()
    {
	return form;
    }
}
