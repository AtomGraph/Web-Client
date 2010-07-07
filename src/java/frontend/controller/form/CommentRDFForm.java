/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend.controller.form;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import javax.servlet.http.HttpServletRequest;
import model.vocabulary.Sioc;

/**
 *
 * @author Pumba
 */
public class CommentRDFForm extends RDFForm
{

    public CommentRDFForm(HttpServletRequest request)
    {
        super(request);
    }

    public Resource getCommentResource()
    {
        Resource comment = null;
        Resource commentClass = getModel().createResource(Sioc.Post);
        ResIterator iter = getModel().listResourcesWithProperty(RDF.type, commentClass);
        if (iter.hasNext())
            comment = iter.next();
        return comment;
    }

}
