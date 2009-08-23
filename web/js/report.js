//google.setOnLoadCallback(ReportLoader.init);
var data = new google.visualization.DataTable(table, 0.6);

function init(visualizations)
{
	var stringColumns = new Array();
	var numericColumns = new Array();
	var dateColumns = new Array();
	var latColumns = new Array();
	var lngColumns = new Array();

	for (i = 0; i < data.getNumberOfColumns(); i++)
	{
		if (data.getColumnType(i) == "string") stringColumns.push(i);
		if (data.getColumnType(i) == "number") numericColumns.push(i);
		if (data.getColumnType(i) == "date") dateColumns.push(i);
	}
	for (i = 0; i < numericColumns.length; i++) // lat/lng columns
	{
		var range = data.getColumnRange(numericColumns[i]);
		if (range.min >= -90 && range.max <= 90) latColumns.push(numericColumns[i]);
		if (range.min >= -180 && range.max <= 180) lngColumns.push(numericColumns[i]);
	}

	if (visualizations.indexOf("Table") != -1)
	{
		var table = new google.visualization.Table(document.getElementById('table'));
		visualizations.push(visualization);
		table.draw(data, {showRowNumber: true});
	}

	if (visualizations.indexOf("ScatterChart") != -1)
		if (numericColumns.length > 1) // scatter
		{
			var view = new google.visualization.DataView(data);
			var columns = numericColumns;
			view.setColumns(columns);
			var container = document.getElementById("scatter-chart");
			var visualization = new google.visualization.ScatterChart(container);
			var options = new Array();
			options["titleX"] = data.getColumnLabel(columns[0]);
			options["titleY"] = data.getColumnLabel(columns[1]);
			visualizations.push(visualization);
			visualization.draw(view, options);
		}

	if (visualizations.indexOf("LineChart") != -1)
		if (stringColumns.length > 0 && numericColumns.length > 0) // line
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array();
			columns[0] = stringColumns[0];
			// alert(columns.toSource());
			columns = columns.concat(numericColumns);
			//alert(numericColumns.toSource());
			view.setColumns(columns);
			var container = document.getElementById("line-chart");
			var visualization = new google.visualization.LineChart(container);
			var options = new Array();
			options["titleX"] = data.getColumnLabel(columns[0]);
			options["titleY"] = data.getColumnLabel(columns[1]);
			visualizations.push(visualization);
			visualization.draw(view, options);
		}

	if (visualizations.indexOf("PieChart") != -1)
		if (stringColumns.length > 0 && numericColumns.length > 0) // pie
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(stringColumns[0], numericColumns[1]);
			view.setColumns(columns);
			var container = document.getElementById("pie-chart");
			var visualization = new google.visualization.PieChart(container);
			var options = new Array();
			visualizations.push(visualization);
			visualization.draw(view, options);
		}

	if (visualizations.indexOf("Map") != -1)
		if (latColumns.length > 0 && lngColumns.length > 1) // map
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(latColumns[0], lngColumns[1]);
			view.setColumns(columns);
			var container = document.getElementById("map");
			var visualization = new google.visualization.Map(container);
			var options = new Array();
			visualizations.push(visualization);
			visualization.draw(view, options);
		}
}