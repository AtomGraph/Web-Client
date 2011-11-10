/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.model.visualization;

/**
 *
 * @author Pumba
 */

//@Namespace("http://code.google.com/apis/visualization/")
public class PieChart extends Visualization
{
    private String labelBinding = null;
    private String valueBinding = null;

    public String getLabelBinding()
    {
	return labelBinding;
    }

    public void setLabelBinding(String labelBinding)
    {
	this.labelBinding = labelBinding;
    }

    public String getValueBinding()
    {
	return valueBinding;
    }

    public void setValueBinding(String valueBinding)
    {
	this.valueBinding = valueBinding;
    }
    
}
