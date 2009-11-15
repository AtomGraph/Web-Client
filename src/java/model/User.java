/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.util.Date;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.RdfType;
import thewebsemantic.binding.RdfBean;
import static model.DublinCore.*;

/**
 *
 * @author Pumba
 */

@Namespace("http://rdfs.org/sioc/ns#")
@RdfType("User")
public class User extends RdfBean<User>
{
    private String name = null;
    private Date createdAt = null;

    @RdfProperty(DATE)
    public Date getCreatedAt()
    {
	return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
	this.createdAt = createdAt;
    }

    //@Id
    @RdfProperty("http://rdfs.org/sioc/ns#name")
    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }
    
}
