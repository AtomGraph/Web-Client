/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import dk.semantic_web.diy.controller.Form;

/**
 *
 * @author Pumba
 */
public interface FormResultView
{
    public void setForm(Form form);
    public Form getForm();

    public void setSuccessful(boolean successful);
    public boolean isSuccessful();
}
