/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.analytics.view;

import org.graphity.Form;
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
