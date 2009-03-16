/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Marker;
import com.googlecode.charts4j.Markers;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Priority;
import com.googlecode.charts4j.ScatterPlot;
import com.googlecode.charts4j.ScatterPlotData;
import com.googlecode.charts4j.Shape;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Pumba
 */
public class QueryResultChart
{
    //public static String getURL(ResultSet results)
    public static String getURL(Iterator<QuerySolution> results, String labelBinding, String xBinding, String yBinding)
    {
	double minXValue = 0;
	double maxXValue = 0;
	double minYValue = 0;
	double maxYValue = 0;
	
	List xDataList = new ArrayList<Double>();
	List yDataList = new ArrayList<Double>();
	
	List markers = new ArrayList<Marker>();
	
//System.out.println(results);
	for (int counter = 0 ; results.hasNext() ; counter++)
	{
	    //QuerySolution solution = results.nextSolution();
	    QuerySolution solution = results.next();

	    //RDFNode x = solution.get("varName");       // Get a result variable by name.
	    //Resource r = solution.getResource("VarR"); // Get a result variable - must be a resource

	    String label = null;
	    double x = 0;
	    double y = 0;

	    if (solution.contains(labelBinding) && solution.get(labelBinding).isLiteral())
	    {
		Literal labelLiteral = solution.getLiteral(labelBinding);
		label = labelLiteral.getLexicalForm();
	    }
	    if (solution.contains(xBinding) && solution.get(xBinding).isLiteral())
	    {
		Literal xLiteral = solution.getLiteral(xBinding);
		x = Double.parseDouble(xLiteral.getLexicalForm());
		if (x > maxXValue) maxXValue = x;
		if (x < minXValue) minXValue = x;
	    }
	    if (solution.contains(yBinding) && solution.get(yBinding).isLiteral())
	    {
		Literal yLiteral = solution.getLiteral(yBinding);
		y = Double.parseDouble(yLiteral.getLexicalForm());
		if (y > maxYValue) maxYValue = y;
		if (y < minYValue) minYValue = y;
	    }	    
	    
	    System.out.println("Label: " + label);
	    Marker marker = Markers.newTextMarker(label, Color.BLACK, 10);
	    markers.add(counter, marker);

	    xDataList.add(x);
	    yDataList.add(y);
	}
	Data xData = DataUtil.scale(xDataList);
	Data yData = DataUtil.scale(yDataList);
        //Data pointSizes = Data.newData(100, 30, 50, 75, 40, 35, 80, 100);
        ScatterPlotData data = Plots.newScatterPlotData(xData, yData);
        data.setLegend("Company");

        Color diamondColor = Color.newColor("FF471A");
        //data.addShapeMarkers(Shape.DIAMOND, diamondColor, 30);
	for (int i = 0; i < markers.size(); i++)
	    data.addMarker((Marker)markers.get(i), i);

        ScatterPlot chart = GCharts.newScatterPlot(data);
        chart.setSize(600, 450);
        chart.setGrid(20, 20, 3, 2);

        AxisLabels xAxisLabels = AxisLabelsFactory.newNumericRangeAxisLabels(minXValue, maxXValue);
        AxisLabels yAxisLabels = AxisLabelsFactory.newNumericRangeAxisLabels(minYValue, maxYValue);

	xAxisLabels.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 13, AxisTextAlignment.CENTER));
	yAxisLabels.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 13, AxisTextAlignment.CENTER));

        chart.addXAxisLabels(xAxisLabels);
        chart.addYAxisLabels(yAxisLabels);

        chart.setTitle("Scatter Plot", Color.BLACK, 16);
        //chart.setBackgroundFill(Fills.newSolidFill(Color.newColor("2F3E3E")));
        //LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("3783DB"), 100);
	//fill.addColorAndOffset(Color.newColor("9BD8F5"), 0);
	//chart.setAreaFill(fill);
	
	return chart.toURLString();
    }
}
