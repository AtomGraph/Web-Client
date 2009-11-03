//google.setOnLoadCallback(countColumns(data));
var data = new google.visualization.DataTable(table, 0.6);
var stringColumns = new Array();
var numericColumns = new Array();
var dateColumns = new Array();
var latColumns = new Array();
var lngColumns = new Array();

function initEmpty(container, visType, bindingElements, bindings, columns)
{
	if (visType.indexOf("Table") != -1)
        {
            drawTable(container);
        }
	if (visType.indexOf("ScatterChart") != -1)
            if (numericColumns.length > 1)
            {
                    initScatterChartControls(bindingElements, numericColumns, numericColumns);
                    drawScatterChart(container, columns); // duplicate
            }
	if (visType.indexOf("LineChart") != -1)
            if (stringColumns.length > 0 && numericColumns.length > 0)
            {
                    initLineChartControls(bindingElements, stringColumns, numericColumns);
                    drawLineChart(container, stringColumns[0], numericColumns);
            }
	if (visType.indexOf("PieChart") != -1)
            if (stringColumns.length > 0 && numericColumns.length > 0)
            {
                    initPieChartControls(bindingElements, stringColumns, numericColumns);
                    drawPieChart(container, stringColumns[0], numericColumns[0]);
            }
	if (visType.indexOf("Map") != -1)
            if (latColumns.length > 0 && lngColumns.length > 0)
            {
                    initMapControls(bindings, latColumns, lngColumns);
                    drawMap(container, latColumns[0], lngColumns[1]);
            }
}

function init(container, visUri, visType, variables)
{
    alert(variables.toSource());
	//scatterChart = new google.visualization.ScatterChart(document.getElementById("scatter-chart")); // onLoad()
	scatterChart = new google.visualization.ScatterChart(container); // onLoad()
    
	if (visType.indexOf("Table") != -1)
	{
		var table = new google.visualization.Table(document.getElementById('table'));
		table.draw(data, {showRowNumber: true});
	}

	if (visType.indexOf("ScatterChart") != -1)
		//if (numericColumns.length > 1) // scatter
		{
			var xColumn = null; //numericColumns;
                        var yColumns = new Array(); //numericColumns;

                        for (var i = 0; i < variables.length; i++)
                        {
                            if (variables[i].type == "http://code.google.com/apis/visualization/ScatterChartXBinding") xColumn = variables[i].value;
                            if (variables[i].type == "http://code.google.com/apis/visualization/ScatterChartYBinding") yColumns.push(variables[i].value);
                        }
//alert(columns.toSource());
                        //initScatterChartControls(numericColumns, numericColumns);
                        drawScatterChart(container, variables);
		}

	if (visType.indexOf("LineChart") != -1)
		if (stringColumns.length > 0 && numericColumns.length > 0) // line
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array();
			columns[0] = stringColumns[0];
			// alert(columns.toSource());
			columns = columns.concat(numericColumns);
			//alert(numericColumns.toSource());
			view.setColumns(columns);
			var visualization = new google.visualization.LineChart(container);
			var options = new Array();
			options["titleX"] = data.getColumnLabel(columns[0]);
			options["titleY"] = data.getColumnLabel(columns[1]);
			visualization.draw(view, options);
		}

	if (visType.indexOf("PieChart") != -1)
		if (stringColumns.length > 0 && numericColumns.length > 0) // pie
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(stringColumns[0], numericColumns[1]);
			view.setColumns(columns);
			var visualization = new google.visualization.PieChart(container);
			var options = new Array();
			visualization.draw(view, options);
		}

	if (visType.indexOf("Map") != -1)
		if (latColumns.length > 0 && lngColumns.length > 1) // map
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(latColumns[0], lngColumns[1]);
			view.setColumns(columns);
			var visualization = new google.visualization.Map(container);
			var options = new Array();
			visualization.draw(view, options);
		}
}

function countColumns(data)
{
        var columns = new Array();
        
	for (var i = 0; i < data.getNumberOfColumns(); i++)
	{
            var types = [ data.getColumnType(i) ];
            if (data.getColumnType(i) == "date") types.push("string"); // date columns also treated as strings
            if (data.getColumnType(i) == "number") // lat/lng columns
            {
		var range = data.getColumnRange(i);
		if (range.min >= -90 && range.max <= 90) types.push("lat");
		if (range.min >= -180 && range.max <= 180) types.push("lng");
            }

            var column = { "index" : i, "types" : types };
            columns.push(column);
	}
}

function drawTable(container)
{
	var table = new google.visualization.Table(container);
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

function initScatterChartControls(bindingElements, xColumns, yColumns)
{
	for (var i = 0; i < xColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(xColumns[i])));
		option.setAttribute("value", xColumns[i]);
		bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < yColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(yColumns[i])));
		option.setAttribute("value", yColumns[i]);
		option.setAttribute("selected", "selected");
		bindingElements[1].element.appendChild(option);
	}
}

function drawScatterChart(container, columns)
{
//alert(xColumn);
//alert(yColumns.toSource());

	var view = new google.visualization.DataView(data);
	/*
        var columns = new Array();
	columns[0] = xColumn;
	columns = columns.concat(yColumns);
        */
	view.setColumns(columns);
	var visualization = new google.visualization.ScatterChart(container);
        var options = new Array();
	options["titleX"] = data.getColumnLabel(columns[0]);
	options["titleY"] = data.getColumnLabel(columns[1]);
	visualization.draw(view, options);
}

function initLineChartControls(bindingElements, labelColumns, valueColumns)
{
	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		option.setAttribute("selected", "selected");
		bindingElements[1].element.appendChild(option);
	}
}

function drawLineChart(container, labelColumn, valueColumns)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array();
	columns[0] = labelColumn;
	columns = columns.concat(valueColumns);
	view.setColumns(columns);
	var visualization = new google.visualization.LineChart(container);
	var options = new Array();
	options["titleX"] = data.getColumnLabel(columns[0]);
	options["titleY"] = data.getColumnLabel(columns[1]);
	visualization.draw(view, options);
}

function initPieChartControls(bindingElements, labelColumns, valueColumns)
{
	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		//option.setAttribute("selected", "selected");
		bindingElements[1].element.appendChild(option);
	}
}

function drawPieChart(container, labelColumn, valueColumn)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array(labelColumn, valueColumn);
	view.setColumns(columns);
	var visualization = new google.visualization.PieChart(container);
	var options = new Array();
	visualization.draw(view, options);
}

function initMapControls(bindingElements, latColumns, lngColumns)
{
	for (var i = 0; i < latColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(latColumns[i])));
		bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < lngColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(lngColumns[i])));
		//option.setAttribute("selected", "selected");
		bindingElements[1].element.appendChild(option);
	}
}

function drawMap(container, latColumn, lngColumn)
{
	var view = new google.visualization.DataView(data);
	var columns = new Array(latColumn, lngColumn);
	view.setColumns(columns);
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