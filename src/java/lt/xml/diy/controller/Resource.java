/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lt.xml.diy.view.View;

/**
 *
 * @author Pumba
 */
public interface Resource
{
	public String getURI();
	public String getRelativeURI();

	public Resource getParent();
	public void setParent(Resource parent);
	//public Resource[] getChildren();

	public Controller getController();
	public void setController(Controller controller);
	public View getView();
	public void setView(View view);

	public View doGet(HttpServletRequest request, HttpServletResponse response);
	public View doPost(HttpServletRequest request, HttpServletResponse response);
	public View doPut(HttpServletRequest request, HttpServletResponse response);
	public View doDelete(HttpServletRequest request, HttpServletResponse response);
}
