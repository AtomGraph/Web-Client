/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;

/**
 *
 * @author Pumba
 */

@Namespace("http://www.semantic-web.dk/report/")
public class Report
{
    private int id;
    private String title = null;
    private String description = null;    
    private Date createdAt = null;
    private User creator = null;
    private Collection<Visualization> charts = new ArrayList<Visualization>();

    @Id
    public int getId()
    {
	return id;
    }

    @RdfProperty("http://purl.org/dc/elements/1.1/date")
    public Date getCreatedAt()
    {
	return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
	this.createdAt = createdAt;
    }

    @RdfProperty("http://purl.org/dc/elements/1.1/creator")
    public User getCreator()
    {
	return creator;
    }

    public void setCreator(User creator)
    {
	this.creator = creator;
    }

    @RdfProperty("http://purl.org/dc/elements/1.1/description")
    public String getDescription()
    {
	return description;
    }

    public void setDescription(String description)
    {
	this.description = description;
    }

    @RdfProperty("http://purl.org/dc/elements/1.1/title")
    public String getTitle()
    {
	return title;
    }

    public void setTitle(String title)
    {
	this.title = title;
    }

    public Collection<Visualization> getCharts()
    {
	return charts;
    }

    public void setCharts(Collection<Visualization> charts)
    {
	this.charts = charts;
    }
    
    public void addChart(Visualization chart)
    {
	
    }
}
