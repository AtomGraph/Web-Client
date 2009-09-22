//google.setOnLoadCallback(ReportLoader.init);
var data = new google.visualization.DataTable(table, 0.6);
var stringColumns = new Array();
var numericColumns = new Array();
var dateColumns = new Array();
var latColumns = new Array();
var lngColumns = new Array();
var scatterChart = null;

function initEmpty()
{
	scatterChart = new google.visualization.ScatterChart(document.getElementById("scatter-chart")); // onLoad()

	countColumns(data);

	drawTable();
	if (numericColumns.length > 1)
	{
		initScatterChartControls(numericColumns, numericColumns);
		drawScatterChart(numericColumns[0], numericColumns); // duplicate
	}
	if (stringColumns.length > 0 && numericColumns.length > 0)
	{
		initLineChartControls(stringColumns, numericColumns);
		drawLineChart(stringColumns[0], numericColumns);
	}
	if (stringColumns.length > 0 && numericColumns.length > 0)
	{
		initPieChartControls(stringColumns, numericColumns);
		drawPieChart(stringColumns[0], numericColumns[0]);
	}
	if (latColumns.length > 0 && lngColumns.length > 0)
	{
		initMapControls(latColumns, lngColumns);
		drawMap(latColumns[0], lngColumns[1]);
	}
}

function init(visualizations)
{
	countColumns(data);

	if (visualizations.indexOf("Table") != -1)
	{
		var table = new google.visualization.Table(document.getElementById('table'));
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
			visualization.draw(view, options);
		}
}

function countColumns(data)
{
	for (var i = 0; i < data.getNumberOfColumns(); i++)
	{
		if (data.getColumnType(i) == "string") stringColumns.push(i);
		if (data.getColumnType(i) == "number") numericColumns.push(i);
		if (data.getColumnType(i) == "date") dateColumns.push(i);
		stringColumns = stringColumns.concat(dateColumns); // date columns also treated as strings
	}
	for (var i = 0; i < numericColumns.length; i++) // lat/lng columns
	{
		var range = data.getColumnRange(numericColumns[i]);
		if (range.min >= -90 && range.max <= 90) latColumns.push(numericColumns[i]);
		if (range.min >= -180 && range.max <= 180) lngColumns.push(numericColumns[i]);
	}
}

function drawTable()
{
	var table = new google.visualization.Table(document.getElementById('table'));
	table.draw(data, { showRowNumber: true });
}

function toggleScatterChart(show)
{
	if (show)
	{
		document.getElementById("scatter-chart-controls").style.display = "block";
		document.getElementById("scatter-chart").style.display = "block";
	}
	else
	{
		document.getElementById("scatter-chart-controls").style.display = "none";
		document.getElementById("scatter-chart").style.display = "none";
	}
}

function initScatterChartControls(xColumns, yColumns)
{
	var xSelect = document.getElementById("scatter-chart-x-binding");
	var ySelect = document.getElementById("scatter-chart-y-binding");

	for (var i = 0; i < xColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(xColumns[i])));
		option.setAttribute("value", xColumns[i]);
		xSelect.appendChild(option);
	}
	for (var i = 0; i < yColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(yColumns[i])));
		option.setAttribute("value", yColumns[i]);
		option.setAttribute("selected", "selected");
		ySelect.appendChild(option);
	}
}

function drawScatterChart(xColumn, yColumns)
{
//alert(xColumn);
//alert(yColumns.toSource());

	var view = new google.visualization.DataView(data);
	var columns = new Array();
	columns[0] = xColumn;
	columns = columns.concat(yColumns);
	view.setColumns(columns);
	var options = new Array();
	options["titleX"] = data.getColumnLabel(columns[0]);
	options["titleY"] = data.getColumnLabel(columns[1]);
	scatterChart.draw(view, options);
}

function initLineChartControls(labelColumns, valueColumns)
{
	var labelSelect = document.getElementById("line-chart-label-binding");
	var valueSelect = document.getElementById("line-chart-value-binding");

	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		labelSelect.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		option.setAttribute("selected", "selected");
		valueSelect.appendChild(option);
	}
}

function drawLineChart(labelColumn, valueColumns)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array();
	columns[0] = labelColumn;
	columns = columns.concat(valueColumns);
	view.setColumns(columns);
	var container = document.getElementById("line-chart");
	var visualization = new google.visualization.LineChart(container);
	var options = new Array();
	options["titleX"] = data.getColumnLabel(columns[0]);
	options["titleY"] = data.getColumnLabel(columns[1]);
	visualization.draw(view, options);
}

function initPieChartControls(labelColumns, valueColumns)
{
	var labelSelect = document.getElementById("pie-chart-label-binding");
	var valueSelect = document.getElementById("pie-chart-value-binding");

	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		labelSelect.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		//option.setAttribute("selected", "selected");
		valueSelect.appendChild(option);
	}
}

function drawPieChart(labelColumn, valueColumn)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array(labelColumn, valueColumn);
	view.setColumns(columns);
	var container = document.getElementById("pie-chart");
	var visualization = new google.visualization.PieChart(container);
	var options = new Array();
	visualization.draw(view, options);
}

function initMapControls(latColumns, lngColumns)
{
	var latSelect = document.getElementById("map-lat-binding");
	var lngSelect = document.getElementById("map-lng-binding");

	for (var i = 0; i < latColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(latColumns[i])));
		latSelect.appendChild(option);
	}
	for (var i = 0; i < lngColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(lngColumns[i])));
		//option.setAttribute("selected", "selected");
		lngSelect.appendChild(option);
	}
}

function drawMap(latColumn, lngColumn)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array(latColumn, lngColumn);
	view.setColumns(columns);
	var container = document.getElementById("map");
	var visualization = new google.visualization.Map(container);
	var options = new Array();
	visualization.draw(view, options);
}

function getSelectedValues(select)
{
	var selectedValues = new Array();

	for (var i = 0; i < select.options.length; i++)
		if (select.options[i].selected) selectedValues.push(Number(select.options[i].value));

	return selectedValues;
}