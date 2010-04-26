//google.setOnLoadCallback(countColumns(data));
var data = new google.visualization.DataTable(table, 0.6);
var typeColumns = new Array();
var visualizations = new Array();
var XSD_NS = 'http://www.w3.org/2001/XMLSchema#';
var dataTypes = new Array();
dataTypes[XSD_NS + 'boolean'] = 'boolean';
dataTypes[XSD_NS + 'string'] = 'string';
dataTypes[XSD_NS + 'integer'] = 'number';
dataTypes[XSD_NS + 'decimal'] = 'number';
dataTypes[XSD_NS + 'float'] = 'number';
dataTypes[XSD_NS + 'double'] = 'number';
dataTypes[XSD_NS + 'date'] = 'date';
dataTypes[XSD_NS + 'dateTime'] = 'datetime';
dataTypes[XSD_NS + 'time'] = 'timeofday';

function initVis(container, visType)
{
    if (visType.indexOf("Table") != -1) visualizations[visType] = new google.visualization.Table(container);
    if (visType.indexOf("ScatterChart") != -1) visualizations[visType] = new google.visualization.ScatterChart(container);
    if (visType.indexOf("LineChart") != -1) visualizations[visType] = new google.visualization.LineChart(container);
    if (visType.indexOf("PieChart") != -1) visualizations[visType] = new google.visualization.PieChart(container);
    if (visType.indexOf("Map") != -1) visualizations[visType] = new google.visualization.Map(container);
}

function initControls(visType, bindingElements, bindingTypes, xsdTypes, variables)
{
    initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables);
/*
    if (visType.indexOf("ScatterChart") != -1)
        if (typeColumns.number.length > 1)
            initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables);
    if (visType.indexOf("LineChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables);
    if (visType.indexOf("PieChart") != -1)
        if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables);
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables);
*/
}

function initAndDraw(container, visType, variables)
{
    initVis(container, visType);
    draw(visualizations[visType], visType, variables);
}

function initWithControlsAndDraw(container, visType, bindingElements, bindingTypes, xsdTypes, variables)
{
    if (hasSufficientColumns(bindingTypes, variables))
    {
        initVis(container, visType);
        initControls(visType, bindingElements, bindingTypes, xsdTypes, variables);
        draw(visualizations[visType], visType, variables);
    }
    //else toggleVisualization(container, fieldset, false); // switch off
}

function hasSufficientColumns(bindingTypes, variables)
{
    for (var i = 0; i < bindingTypes.length; i++)
    {
        var columns = columnsByBindingType(bindingTypes[i], variables);
        var bindingType = bindingTypes[i];

        if ("cardinality" in bindingType && bindingType.cardinality != columns.length) return false;
        if ("minCardinality" in bindingType && bindingType.minCardinality > columns.length) return false;
        if ("maxCardinality" in bindingType && bindingType.maxCardinality < columns.length) return false;
    }
    return true;
}

function draw(container, visType, variables)
{
    if (visType.indexOf("Table") != -1)
        drawTable(container, variables);
    if (visType.indexOf("ScatterChart") != -1)
        //if (typeColumns.number.length > 1)
            drawScatterChart(container, variables);
    if (visType.indexOf("LineChart") != -1)
        //if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            drawLineChart(container, variables);
    if (visType.indexOf("PieChart") != -1)
        //if (typeColumns.string.length > 0 && typeColumns.number.length > 0)
            drawPieChart(container, variables);
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            drawMap(container, variables);
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
}

function xsdTypeToWireType(xsdType)
{
    return dataTypes[xsdType];
}

function variablesByWireType(wireType, variables)
{
    return typeColumns[wireType];
}

function xsdTypesByBindingType(bindingType, xsdTypes)
{
    var bindingXsdTypes = new Array();

    for (var k = 0; k < xsdTypes.length; k++)
        if (bindingType == xsdTypes[k].bindingType) // join
            bindingXsdTypes.push(xsdTypes[k].type);

    return bindingXsdTypes;
}

function wireTypesByBindingType(bindingType, xsdTypes)
{
    var wireTypes = new Array();
    var bindingXsdTypes = xsdTypesByBindingType(bindingType, xsdTypes);

    for (var k = 0; k < bindingXsdTypes.length; k++)
    {
        var wireType = xsdTypeToWireType(bindingXsdTypes[k]);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType);
    }

    return wireTypes;
}

function columnsByBindingType(bindingType, variables)
{
//alert(bindingType.toSource());
    var bindingColumns = new Array();

    for (var k = 0; k < variables.length; k++)
        if (bindingType.type == variables[k].bindingType) // join
            bindingColumns.push(variables[k].variable);
//alert(bindingColumns.toSource());
    return bindingColumns;
}

function countVariables(data, bindingTypes, xsdTypes)
{
    var variables = new Array();

    for (var j = 0; j < bindingTypes.length; j++)
    {
        var bindingWireTypes = new Array();
        var bindingColumns = new Array();
        for (var k = 0; k < xsdTypes.length; k++)
            if (bindingTypes[j].type == xsdTypes[k].bindingType) // join
            {
                var xsdType = xsdTypes[k].type;
                var wireType = xsdTypeToWireType(xsdType);
                var dataTypeColumns = variablesByWireType(wireType);
                if (bindingWireTypes.indexOf(wireType) == -1)
                {
                    //alert(dataTypeColumns);
                    bindingWireTypes.push(wireType);
                    bindingColumns = bindingColumns .concat(dataTypeColumns);
                }
            }

        for (var l = 0; l < bindingColumns.length; l++)
        {
            var variable = { };
            variable.variable = bindingColumns [l];
            variable.bindingType = bindingTypes[j].type;
            variables.push(variable);
        }
//alert(variables.toSource());
    }
    return variables;
}

function drawTable(container, variables)
{
	container.draw(data, { showRowNumber: true });
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

function initVisualizationControls(bindingElements, bindingTypes, xsdTypes, variables)
{
    for (var i = 0; i < bindingElements.length; i++)
	for (var j = 0; j < bindingTypes.length; j++) // for (var j = 0; j < variables.length; j++)
            if (bindingElements[i].bindingType == bindingTypes[j].type)
            {   
                var bindingDataTypes = new Array();
                var bindingColumns = new Array();
                for (var k = 0; k < xsdTypes.length; k++)
                    if (bindingTypes[j].type == xsdTypes[k].bindingType)
                    {
                        var xsdType = xsdTypes[k].type;
                        var wireType = dataTypes[xsdType];
                        var dataTypeColumns = typeColumns[wireType];
                        if (bindingDataTypes.indexOf(wireType) == -1)
                        {
                            //alert(dataTypeColumns);
                            bindingDataTypes.push(wireType);
                            bindingColumns = bindingColumns.concat(dataTypeColumns);
                        }
                    }
                //alert(dataTypeColumns);

                for (var l = 0; l < bindingColumns.length; l++)
                {
                    var option = document.createElement("option");
                    option.appendChild(document.createTextNode(data.getColumnLabel(bindingColumns[l])));
                    option.setAttribute("value", bindingColumns[l]);
                    for (var m = 0; m < variables.length; m++)
                        if (variables[m].bindingType == bindingTypes[j].type && variables[m].variable == bindingColumns[l]) // variables[m].columns.indexOf(bindingColumns[l]) != -1
                            //alert(variables[m].columns + " | " + bindingColumns[l]);
                            option.setAttribute("selected", "selected");
                    bindingElements[i].element.appendChild(option);
                }
            }
}

function drawScatterChart(container, variables)
{
        var visColumns = new Array();

        for (var i = 0; i < variables.length; i++)
        {
            if (variables[i].bindingType.indexOf("ScatterChartXBinding") != -1) visColumns[0] = variables[i].variable;
            if (variables[i].bindingType.indexOf("ScatterChartYBinding") != -1) visColumns = visColumns.concat(variables[i].variable);
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
        var options = new Array();
	options["titleX"] = data.getColumnLabel(visColumns[0]);
	options["titleY"] = data.getColumnLabel(visColumns[1]);
	container.draw(view, options);
}

function drawLineChart(container, variables)
{
	var visColumns = new Array();
        for (var i = 0; i < variables.length; i++)
        {
            if (variables[i].bindingType.indexOf("LineChartLabelBinding") != -1) visColumns[0] = variables[i].variable;
            if (variables[i].bindingType.indexOf("LineChartValueBinding") != -1) visColumns = visColumns.concat(variables[i].variable);
        }

        var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
	var options = new Array();
	options["titleX"] = data.getColumnLabel(visColumns[0]);
	options["titleY"] = data.getColumnLabel(visColumns[1]);
	container.draw(view, options);
}

function drawPieChart(container, variables)
{
        var visColumns = new Array();

        for (var i = 0; i < variables.length; i++)
        {
            if (variables[i].bindingType.indexOf("PieChartLabelBinding") != -1) visColumns[0] = variables[i].variable;
            if (variables[i].bindingType.indexOf("PieChartValueBinding") != -1) visColumns[1] = variables[i].variable;
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
	var options = new Array();
	container.draw(view, options);
}

function drawMap(container, variables)
{
        var visColumns = new Array();

        for (var i = 0; i < variables.length; i++)
        {
            if (variables[i].bindingType.indexOf("MapLatBinding") != -1) visColumns[0] = variables[i].variable;
            if (variables[i].bindingType.indexOf("MapLngBinding") != -1) visColumns[1] = variables[i].variable;
        }

	var view = new google.visualization.DataView(data);
	view.setColumns(visColumns);
	var options = new Array();
	container.draw(view, options);
}

function getBindingVariables(bindingElement, bindingType)
{
	var variables = new Array();

	for (var i = 0; i < bindingElement.options.length; i++)
            if (bindingElement.options[i].selected)
                {
                    var variable = { };
                    variable.variable = Number(bindingElement.options[i].value);
                    variable.bindingType = bindingType;
                    variables.push(variable);
                    //alert(variable.toSource());
                }

	return variables;
}

function getVisualizationVariables(bindingElements, bindingTypes)
{
    var variables = new Array();
    for (var i = 0; i < bindingElements.length; i++)
        variables = variables.concat(getBindingVariables(bindingElements[i], bindingTypes[i]));
    return variables;
}