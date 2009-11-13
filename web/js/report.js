//google.setOnLoadCallback(countColumns(data));
var data = new google.visualization.DataTable(table, 0.6);
var typeColumns = new Array();

function initEmpty(container, visType, bindingElements, bindings, columns)
{
	if (visType.indexOf("Table") != -1)
        {
            drawTable(container);
        }
	if (visType.indexOf("ScatterChart") != -1)
            if (typeColumns.number.length > 1)
            {
                    initScatterChartControls(bindingElements, typeColumns.number, typeColumns.number);
                    drawScatterChart(container, columns); // duplicate
            }
	if (visType.indexOf("LineChart") != -1)
            if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            {
                    initLineChartControls(bindingElements, typeColumns.string, typeColumns.number);
                    drawLineChart(container, typeColumns.string[0], typeColumns.number);
            }
	if (visType.indexOf("PieChart") != -1)
            if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            {
                    initPieChartControls(bindingElements, typeColumns.string, typeColumns.number);
                    drawPieChart(container, typeColumns.string[0], typeColumns.number[0]);
            }
	if (visType.indexOf("Map") != -1)
            if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            {
                    initMapControls(bindingElements, typeColumns.lat, typeColumns.lng);
                    drawMap(container, typeColumns.lat[0], typeColumns.lng[1]);
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
		//if (typeColumns.number.length > 1) // scatter
		{
			var xColumn = null; //typeColumns.number;
                        var yColumns = new Array(); //typeColumns.number;

                        for (var i = 0; i < variables.length; i++)
                        {
                            if (variables[i].type == "http://code.google.com/apis/visualization/ScatterChartXBinding") xColumn = variables[i].value;
                            if (variables[i].type == "http://code.google.com/apis/visualization/ScatterChartYBinding") yColumns.push(variables[i].value);
                        }
//alert(typeColumns.toSource());
                        //initScatterChartControls(typeColumns.number, typeColumns.number);
                        drawScatterChart(container, variables);
		}

	if (visType.indexOf("LineChart") != -1)
		if (typeColumns.string.length > 0 && typeColumns.number.length > 0) // line
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array();
			columns[0] = typeColumns.string[0];
			// alert(columns.toSource());
			columns = columns.concat(typeColumns.number);
			//alert(typeColumns.number.toSource());
			view.setColumns(columns);
			var visualization = new google.visualization.LineChart(container);
			var options = new Array();
			options["titleX"] = data.getColumnLabel(columns[0]);
			options["titleY"] = data.getColumnLabel(columns[1]);
			visualization.draw(view, options);
		}

	if (visType.indexOf("PieChart") != -1)
		if (typeColumns.string.length > 0 && typeColumns.number.length > 0) // pie
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(typeColumns.string[0], typeColumns.number[1]);
			view.setColumns(columns);
			var visualization = new google.visualization.PieChart(container);
			var options = new Array();
			visualization.draw(view, options);
		}

	if (visType.indexOf("Map") != -1)
		if (typeColumns.lat.length > 0 && typeColumns.lng.length > 1) // map
		{
			var view = new google.visualization.DataView(data);
			var columns = new Array(typeColumns.lat[0], typeColumns.lng[1]);
			view.setColumns(columns);
			var visualization = new google.visualization.Map(container);
			var options = new Array();
			visualization.draw(view, options);
		}
}

function countColumns(data)
{
        typeColumns = { "string": [], "number": [], "date": [], "lat": [], "lng": [] };
        
	for (var i = 0; i < data.getNumberOfColumns(); i++)
	{
            if (data.getColumnType(i) == "string") typeColumns.string.push(i);
            if (data.getColumnType(i) == "date")
            {
                typeColumns.string.push(i); // date columns also treated as strings
                typeColumns.date.push(i);
            }
            if (data.getColumnType(i) == "number") // lat/lng columns
            {
                typeColumns.number.push(i);
		var range = data.getColumnRange(i);
		if (range.min >= -90 && range.max <= 90) typeColumns.lat.push(i);
		if (range.min >= -180 && range.max <= 180) typeColumns.lng.push(i);
            }
	}
//alert(typeColumns.toSource());

        //return typeColumns;
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