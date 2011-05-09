/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.RdfType;
import thewebsemantic.binding.RdfBean;
import static dk.semantic_web.sem_rep.model.vocabulary.DublinCore.*;

/**
 *
 * @author Pumba
 */

@Namespace("http://rdfs.org/sioc/ns#")
@RdfType("UserAccount")
public class UserAccount extends RdfBean<UserAccount>
{
    private String name = null;
    private Date createdAt = null;

    @RdfProperty(CREATED)
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

    @Id
    public URI getURI()
    {
	try
	{
	    String uri = "http://temp.com/user/123";

	    return new URI(uri);
	} catch (URISyntaxException ex)
	{
	    Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }

}
