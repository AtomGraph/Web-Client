//google.setOnLoadCallback(countColumns(data));
var data = new google.visualization.DataTable(table, 0.6);
var typeColumns = new Array();
var visualizations = new Array();

function initEmpty(container, visType, bindingElements, bindings, columns)
{
	if (visType.indexOf("Table") != -1)
        {
            drawTable(container);
        }
	if (visType.indexOf("ScatterChart") != -1)
        {
            visualizations[visType] = new google.visualization.ScatterChart(container);

            if (typeColumns.number.length > 1)
            {
                    initChartControls(bindingElements, columns);
                    drawScatterChart(visualizations[visType], visType, columns);
            }
        }
	if (visType.indexOf("LineChart") != -1)
        {
            visualizations[visType] = new google.visualization.LineChart(container);

            if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            {

                initChartControls(bindingElements, columns);
                drawLineChart(visualizations[visType], visType, columns);
            }
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
                        drawScatterChart(container, visType, variables);
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

function toggleChart(container, fieldset, show)
{
	if (show)
	{
		container.style.display = "block";
		fieldset.style.display = "block";
	}
	else
	{
		container.style.display = "none";
		fieldset.style.display = "none";
	}
}

function initChartControls(bindingElements, columns)
{
    for (var i = 0; i < bindingElements.length; i++)
	for (var j = 0; j < columns.length; j++)
            if (bindingElements[i].bindingType == columns[j].bindingType)
                for (var k = 0; k < columns[j].columns.length; k++)
                {
                    var option = document.createElement("option");
                    option.appendChild(document.createTextNode(data.getColumnLabel(columns[j].columns[k])));
                    option.setAttribute("value", columns[j].columns[k]);
                    option.setAttribute("selected", "selected");
                    bindingElements[i].element.appendChild(option);
                }
}

function drawScatterChart(visualization, visType, columns)
{
//alert(columns);
        var visColumns = new Array();

        for (var i = 0; i < columns.length; i++)
        {
            if (columns[i].bindingType.indexOf("ScatterChartXBinding") != -1) visColumns[0] = columns[i].columns[0];
            if (columns[i].bindingType.indexOf("ScatterChartYBinding") != -1) visColumns = visColumns.concat(columns[i].columns);
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
        var options = new Array();
	options["titleX"] = data.getColumnLabel(visColumns[0]);
	options["titleY"] = data.getColumnLabel(visColumns[1]);
	visualization.draw(view, options);
}

function initLineChartControls(bindingElements, labelColumns, valueColumns)
{
	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		option.setAttribute("value", labelColumns[i]);
		bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		option.setAttribute("value", valueColumns[i]);
                option.setAttribute("selected", "selected");
		bindingElements[1].element.appendChild(option);
	}
}

function drawLineChart(visualization, visType, columns)
{
	var visColumns = new Array();
        for (var i = 0; i < columns.length; i++)
        {
            if (columns[i].bindingType.indexOf("LineChartLabelBinding") != -1) visColumns[0] = columns[i].columns[0];
            if (columns[i].bindingType.indexOf("LineChartValueBinding") != -1) visColumns = visColumns.concat(columns[i].columns);
        }

        var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
	var options = new Array();
	options["titleX"] = data.getColumnLabel(visColumns[0]);
	options["titleY"] = data.getColumnLabel(visColumns[1]);
	visualization.draw(view, options);
}

function initPieChartControls(bindingElements, labelColumns, valueColumns)
{
	for (var i = 0; i < labelColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(labelColumns[i])));
		option.setAttribute("value", labelColumns[i]);
                bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < valueColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(valueColumns[i])));
		option.setAttribute("value", valueColumns[i]);
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
		option.setAttribute("value", latColumns[i]);
                bindingElements[0].element.appendChild(option);
	}
	for (var i = 0; i < lngColumns.length; i++)
	{
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(data.getColumnLabel(lngColumns[i])));
		option.setAttribute("value", lngColumns[i]);
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