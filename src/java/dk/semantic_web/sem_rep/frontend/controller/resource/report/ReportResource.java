/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.controller.resource.report;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.spi.resource.PerRequest;
import dk.semantic_web.sem_rep.frontend.view.report.ReportReadView;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dk.semantic_web.diy.view.View;
import dk.semantic_web.diy.controller.Error;
import dk.semantic_web.rdf_editor.frontend.controller.resource.instance.InstanceResource;
import dk.semantic_web.sem_rep.frontend.controller.exception.InvalidFormException;
import dk.semantic_web.sem_rep.frontend.controller.exception.NoResultsException;
import dk.semantic_web.sem_rep.frontend.controller.form.CommentRDFForm;
import dk.semantic_web.sem_rep.frontend.controller.form.ReportRDFForm;
import dk.semantic_web.sem_rep.frontend.view.report.ReportUpdateView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.TransformerConfigurationException;
import dk.semantic_web.sem_rep.model.vocabulary.DublinCore;
import dk.semantic_web.sem_rep.model.Report;
import dk.semantic_web.sem_rep.model.vocabulary.Sioc;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import thewebsemantic.Bean2RDF;
import dk.semantic_web.sem_rep.view.QueryResult;
import dk.semantic_web.sem_rep.view.XMLSerializer;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */

@PerRequest
public class ReportResource extends InstanceResource
{
    //public static final UriBuilder URI_BUILDER = ReportListResource.URI_BUILDER.clone().path("{report}");

    static final Logger logger = LoggerFactory.getLogger(ReportResource.class);

    private Report report = null;
    //private View view = null;
    public static final long RESULTS_LIMIT = 50;
    public static List<Class> REFERRING_CLASSES = new ArrayList<Class>();
    
    public ReportResource(ReportListResource parent, @Context UriInfo uriInfo) // Report report
    {
	super(parent, uriInfo);
	//setReport(report);
	REFERRING_CLASSES.add(this.getClass());
	REFERRING_CLASSES.add(ReportListResource.class);
    }
    
    @Override
    public String getPath() {
	return InstanceResource.getIndividualPath(getTopicIndividual());
    }

    public Individual getTopicIndividual() {
	return getTopicResource().as(Individual.class);
    }

    public Report getReport()
    {
	return report;
    }

    public void setReport(Report report)
    {
	this.report = report;
    }

    @Override
    @GET
    public View doGet(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, Exception
    {
	View parent = super.doGet(request, response);
	if (parent != null) return parent;

//if (REFERRING_CLASSES.contains(getReferrer(request).getClass()));

        if (isUpdateView(request))
        {
            ReportUpdateView updateView = new ReportUpdateView(this);
            updateView.setQueryResults(QueryResult.selectRemote(getReport().getQuery().getEndpoint().toString(), getReport().getQuery().getQueryString(), RESULTS_LIMIT));
            return updateView;
        }

	long limit = RESULTS_LIMIT;
	if (request.getParameter("limit") != null) limit = Integer.parseInt(request.getParameter("limit"));
	ReportReadView readView = new ReportReadView(this);
	ResultSetRewindable results = QueryResult.selectRemote(getReport().getQuery().getEndpoint().toString(), getReport().getQuery().getQueryString(), limit);

	readView.setQueryResults(results);
getReport().getQuery().setLastResult(XMLSerializer.serialize(results));
Bean2RDF writer = new Bean2RDF(dk.semantic_web.rdf_editor.model.Model.getInstance().getData());
writer.save(getReport().getQuery());

        return readView;
    }

    @Override
    @POST
    public View doPost(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
	View parent = super.doPost(request, response);
	if (parent != null) return parent;

        if (isQueryAction(request)) return query(request, response);
        if (isUpdateAction(request)) return update(request, response);
        if (isCommentAction(request)) comment(request, response);

        if (isUpdateView(request)) return new ReportUpdateView(this);
        
	return read(request, response);
    }

    private ReportReadView read(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, MalformedURLException, URISyntaxException, IOException
    {
	ReportReadView view = new ReportReadView(this);
        view.setQueryResults(QueryResult.selectRemote(getReport().getQuery().getEndpoint().toString(), getReport().getQuery().getQueryString(), RESULTS_LIMIT));
        return view;
    }

    private ReportUpdateView query(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, MalformedURLException, URISyntaxException
    {
        ReportUpdateView view = new ReportUpdateView(this);
	ReportRDFForm form = new ReportRDFForm(request);
        List<Error> errors = form.validate();
        view.setForm(form);
        view.setModel(form.getModel());

	try
	{
            if (!errors.isEmpty()) throw new InvalidFormException();

	    ResultSetRewindable queryResults = QueryResult.selectRemote(form.getEndpoint().getURI(), form.getQueryString(), RESULTS_LIMIT);
            int count = ResultSetFormatter.consume(ResultSetFactory.copyResults(queryResults));
            if (count == 0) throw new NoResultsException();

            view.setQueryResults(queryResults);
            view.setResult(true);
	}
        catch (InvalidFormException ex)
	{
            view.setErrors(errors);
            view.setResult(false);
	}
        catch (NoResultsException ex)
	{
            errors.add(new Error("noResults"));

            view.setErrors(errors);
            view.setResult(false);
	}
        catch (IOException ex)
	{
            errors.add(new Error("ioError"));

            view.setErrors(errors);
            view.setResult(false);

	    logger.error(ex.toString());
	}
        catch (QueryException ex)
	{
            errors.add(new Error("invalidQuery", ex.getMessage()));

            view.setErrors(errors);
            view.setResult(false);

	    logger.error(ex.toString());
	}

        return view;
    }

    private ReportUpdateView update(HttpServletRequest request, HttpServletResponse response) throws TransformerConfigurationException, IOException, URISyntaxException
    {
	ReportUpdateView view = new ReportUpdateView(this);
	ReportRDFForm form = new ReportRDFForm(request);
	List<Error> errors = form.validateWithTitle();
	view.setForm(form);
	view.setModel(form.getModel());
	try
	{
	    if (!errors.isEmpty()) throw new InvalidFormException();

	    view.setQueryResults(QueryResult.selectRemote(getReport().getQuery().getEndpoint().toString(), getReport().getQuery().getQueryString(), RESULTS_LIMIT));

	    updateModel(form);

	    view.setResult(true);
	    response.sendRedirect(form.getReport().getURI());
	}
	catch (InvalidFormException ex)
	{
	    view.setErrors(errors);
	    view.setResult(false);
	}
        catch (IOException ex)
	{
            errors.add(new Error("ioError"));

            view.setErrors(errors);
            view.setResult(false);

	    logger.error(ex.toString());
	}
	return view;
    }

    // should be possible to refactor more
    private void updateModel(ReportRDFForm form)
    {
	//ReportRDFForm form = new ReportRDFForm(request);

        SPINModuleRegistry.get().init();
	com.hp.hpl.jena.query.Query arqQuery = ARQFactory.get().createQuery(form.getModel(), form.getQueryString());
	ARQ2SPIN arq2Spin = new ARQ2SPIN(form.getModel());
	Select spinQuery = (Select)arq2Spin.createQuery(arqQuery, form.getQueryResource().getURI());

        // add some metadata
        String userUri = getController().getMapping().getHost() + "users/admin";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Model newModel = form.getModel();
        newModel.add(form.getReport(), newModel.createProperty(DublinCore.MODIFIED), newModel.createTypedLiteral(calendar));
        newModel.add(form.getReport(), newModel.createProperty(DublinCore.CREATOR), newModel.createResource(userUri));
        newModel.add(form.getReport(), RDF.type, newModel.createResource(Sioc.Forum));
        newModel.add(form.getReport(), newModel.createProperty(Sioc.has_creator), newModel.createResource(userUri));
        newModel.add(newModel.createResource(userUri), RDF.type, newModel.createResource(Sioc.UserAccount));
        newModel.add(newModel.createResource(userUri), newModel.createProperty(Sioc.name), newModel.createTypedLiteral("Admin"));
//newModel.write(System.out, FileUtils.langXMLAbbrev);

Resource reportResource = dk.semantic_web.rdf_editor.model.Model.getInstance().getData().createResource(form.getReport().getURI());
Resource endpointResource = dk.semantic_web.rdf_editor.model.Model.getInstance().getData().createResource(form.getEndpoint().getURI());
System.out.println("Report resource: " + reportResource);
System.out.println("Endpoint resource: " + endpointResource);

Model oldModel = ResourceUtils.reachableClosure(reportResource);
List<Statement> keepStatements = new ArrayList<Statement>();
keepStatements.add(oldModel.getProperty(reportResource, oldModel.createProperty(DublinCore.CREATED)));
keepStatements.add(oldModel.getProperty(endpointResource, RDF.type));
Statement endpointTitle = oldModel.getProperty(endpointResource, oldModel.createProperty(DublinCore.TITLE)); // can be null!
if (endpointTitle != null) keepStatements.add(endpointTitle);
oldModel.remove(keepStatements); // do not delete creation date, endpoint metadata etc.
//oldModel.write(System.out, FileUtils.langXMLAbbrev);
        dk.semantic_web.rdf_editor.model.Model.getInstance().getData().remove(oldModel);
        dk.semantic_web.rdf_editor.model.Model.getInstance().getData().add(newModel);
    }

    private void comment(HttpServletRequest request, HttpServletResponse response)
    {
	CommentRDFForm form = new CommentRDFForm(request);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        form.getModel().add(form.getCommentResource(), DCTerms.created, form.getModel().createTypedLiteral(calendar));

        dk.semantic_web.rdf_editor.model.Model.getInstance().getData().add(form.getModel());
    }

    protected boolean isUpdateView(HttpServletRequest request)
    {
        return (request.getParameter("view") != null && request.getParameter("view").equals("update"));
    }

    protected boolean isQueryAction(HttpServletRequest request)
    {
        return (request.getParameter("action") != null && request.getParameter("action").equals("query"));
    }

    protected boolean isUpdateAction(HttpServletRequest request)
    {
        return (request.getParameter("action") != null && request.getParameter("action").equals("update"));
    }

    protected boolean isCommentAction(HttpServletRequest request)
    {
        return (request.getParameter("action") != null && request.getParameter("action").equals("comment"));
    }

    public dk.semantic_web.diy.controller.Resource getReferrer(HttpServletRequest request)
    {
	String relativeUri = request.getHeader("Referrer");
	return getController().getMapping().findByURI(relativeUri);
    }

    @Override
    public UriBuilder getUriBuilder()
    {
	return URI_BUILDER;
    }
}
