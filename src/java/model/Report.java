/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import frontend.controller.resource.report.ReportResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Transient;
import thewebsemantic.binding.RdfBean;
import static model.DublinCore.*;

/**
 *
 * @author Pumba
 */

@Namespace("http://www.semantic-web.dk/ontologies/semantic-reports/")
public class Report extends RdfBean<Report>
{
    private ReportResource resource = null;
    private String title = null;
    private String description = null;    
    private Date createdAt = null;
    private Query query = null;
    private User creator = null;
    //private Collection<Visualization> visualizations = new ArrayList<Visualization>();

    public Report()
    {
    }

    public Report(String title, Query query, User creator)
    {
	setTitle(title);
	setQuery(query);
	setCreator(creator);
	setCreatedAt(new Date());
    }

    @Transient
    public ReportResource getFrontEndResource()
    {
	return resource;
    }

    public void setFrontEndResource(ReportResource resource)
    {
	this.resource = resource;
    }
    
    @Id
    public URI getURI()
    {
	try
	{
	    String uri = getFrontEndResource().getAbsoluteURI();
	    
	    return new URI(uri);
	} catch (URISyntaxException ex)
	{
	    Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
    
    @RdfProperty(DATE)
    public Date getCreatedAt()
    {
	return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
	this.createdAt = createdAt;
    }

    @RdfProperty("http://www.semantic-web.dk/ontologies/semantic-reports/query")
    public Query getQuery()
    {
	return query;
    }

    public void setQuery(Query query)
    {
	this.query = query;
    }

    @RdfProperty(CREATOR)
    public User getCreator()
    {
	return creator;
    }

    public void setCreator(User creator)
    {
	this.creator = creator;
    }

    @RdfProperty(DESCRIPTION)
    public String getDescription()
    {
	return description;
    }

    public void setDescription(String description)
    {
	this.description = description;
    }

    @RdfProperty(TITLE)
    public String getTitle()
    {
	return title;
    }

    public void setTitle(String title)
    {
	this.title = title;
    }

    /*
    @RdfProperty("http://www.semantic-web.dk/ontologies/semantic-reports/visualizedBy")
    public Collection<Visualization> getVisualizations()
    {
	return visualizations;
    }

    public void setVisualizations(Collection<Visualization> visualizations)
    {
	this.visualizations = visualizations;
    }

    public void addVisualization(Visualization visualization)
    {
	visualizations.add(visualization);
    }
    */
}
