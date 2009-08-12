/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.util.ArrayList;
import java.util.Collection;
import thewebsemantic.Namespace;

/**
 *
 * @author Pumba
 */

@Namespace("http://code.google.com/apis/visualization/")
public class ScatterChart extends Visualization
{
    private String xBinding = null;
    private Collection<String> yBindings = new ArrayList<String>();

    public String getXBinding()
    {
	return xBinding;
    }

    public void setXBinding(String xBinding)
    {
	this.xBinding = xBinding;
    }

    public Collection<String> getYBindings()
    {
	return yBindings;
    }

    public void setYBindings(Collection<String> yBindings)
    {
	this.yBindings = yBindings;
    }

    public void addYBinding(String yBinding)
    {
	yBindings.add(yBinding);
    }

}
