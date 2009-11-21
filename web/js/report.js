//google.setOnLoadCallback(countColumns(data));
var data = new google.visualization.DataTable(table, 0.6);
var typeColumns = new Array();
var visualizations = new Array();

function initVis(container, visType)
{
    if (visType.indexOf("Table") != -1) visualizations[visType] = new google.visualization.Table(container);
    if (visType.indexOf("ScatterChart") != -1) visualizations[visType] = new google.visualization.ScatterChart(container);
    if (visType.indexOf("LineChart") != -1) visualizations[visType] = new google.visualization.LineChart(container);
    if (visType.indexOf("PieChart") != -1) visualizations[visType] = new google.visualization.PieChart(container);
    if (visType.indexOf("Map") != -1) visualizations[visType] = new google.visualization.Map(container);
}

function initControls(visType, bindingElements, bindings, columns)
{
    //if (visType.indexOf("Table") != -1)
    if (visType.indexOf("ScatterChart") != -1)
        if (typeColumns.number.length > 1)
            initVisualizationControls(bindingElements, bindings, columns);
    if (visType.indexOf("LineChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            initVisualizationControls(bindingElements, bindings, columns);
    if (visType.indexOf("PieChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            initVisualizationControls(bindingElements, bindings, columns);
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            initVisualizationControls(bindingElements, bindings, columns);
}

function initAndDraw(container, visType, bindings, columns)
{
    initVis(container, visType);
    draw(visualizations[visType], visType, bindings, columns);
}

function initWithControlsAndDraw(container, visType, bindingElements, bindings, columns)
{
    initVis(container, visType);
    initControls(visType, bindingElements, bindings, columns);
    draw(visualizations[visType], visType, bindings, columns);
}

function draw(visualization, visType, bindings, columns)
{
    if (visType.indexOf("Table") != -1)
        drawTable(visualization, visType, bindings, columns);
    if (visType.indexOf("ScatterChart") != -1)
        if (typeColumns.number.length > 1)
            drawScatterChart(visualization, visType, bindings, columns);
    if (visType.indexOf("LineChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            drawLineChart(visualization, visType, bindings, columns);
    if (visType.indexOf("PieChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            drawPieChart(visualization, visType, bindings, columns);
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            drawMap(visualization, visType, bindings, columns);
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

function drawTable(visualization, visType, bindings, columns)
{
	visualization.draw(data, { showRowNumber: true });
}

function toggleVisualization(container, fieldset, show)
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

function initVisualizationControls(bindingElements, bindings, columns)
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

function drawScatterChart(visualization, visType, bindings, columns)
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

function drawLineChart(visualization, visType, bindings, columns)
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

function drawPieChart(visualization, visType, bindings, columns)
{
        var visColumns = new Array();

        for (var i = 0; i < columns.length; i++)
        {
            if (columns[i].bindingType.indexOf("PieChartLabelBinding") != -1) visColumns[0] = columns[i].columns[0];
            if (columns[i].bindingType.indexOf("PieChartValueBinding") != -1) visColumns[1] = columns[i].columns[0];
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
	var options = new Array();
	visualization.draw(view, options);
}

function drawMap(visualization, visType, bindings, columns)
{
        var visColumns = new Array();

        for (var i = 0; i < columns.length; i++)
        {
            if (columns[i].bindingType.indexOf("MapLatBinding") != -1) visColumns[0] = columns[i].columns[0];
            if (columns[i].bindingType.indexOf("MapLngBinding") != -1) visColumns[1] = columns[i].columns[0];
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
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