/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.view;

import dk.semantic_web.diy.controller.Form;
import java.util.List;

/**
 *
 * @author Pumba
 */
public interface FormResultView
{
    public void setForm(Form form);
    public Form getForm();

    public void setErrors(List<Error> errors);
    public List<Error> getErrors();

    public void setResult(Boolean successful);
    public Boolean getResult();
}
