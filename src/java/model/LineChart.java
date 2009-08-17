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
public class LineChart extends Visualization
{
    private String labelBinding = null;
    private Collection<String> bindings = new ArrayList<String>();

    public Collection<String> getBindings()
    {
	return bindings;
    }

    public void setBindings(Collection<String> bindings)
    {
	this.bindings = bindings;
    }

    public String getLabelBinding()
    {
	return labelBinding;
    }

    public void setLabelBinding(String labelBinding)
    {
	this.labelBinding = labelBinding;
    }
    
}
