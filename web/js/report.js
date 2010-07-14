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
    if (visType.indexOf("BarChart") != -1) visualizations[visType] = new google.visualization.BarChart(container);
    if (visType.indexOf("ColumnChart") != -1) visualizations[visType] = new google.visualization.ColumnChart(container);
    if (visType.indexOf("AreaChart") != -1) visualizations[visType] = new google.visualization.AreaChart(container);
    if (visType.indexOf("Map") != -1) visualizations[visType] = new google.visualization.Map(container);
}

function initControls(visType, bindingElements, bindingTypes, xsdTypes, variables)
{
    var visBindingTypes = bindingTypesByVisType(bindingTypes, visType);
    //alert(visBindingTypes.toSource());
    //alert(variables.toSource());

    for (var i in visBindingTypes)
    {
        var bindingElement = elementByBindingType(bindingElements, visBindingTypes[i].type);
	//alert(bindingElement.toSource());

	if (!("cardinality" in visBindingTypes[i]) ||
		("cardinality" in visBindingTypes[i] && visBindingTypes[i].cardinality > j) ||
		("minCardinality" in visBindingTypes[i] && visBindingTypes[i].minCardinality > j))
	    bindingElement.element.multiple = "multiple";

	var bindingColumns = columnsByBindingType(visBindingTypes[i], xsdTypes);
//alert(visBindingTypes[i].toSource() + " " + bindingColumns.toSource());

	for (var j in bindingColumns)
	{
	    var option = document.createElement("option");
	    option.appendChild(document.createTextNode(data.getColumnLabel(bindingColumns[j])));
	    option.setAttribute("value", bindingColumns[j]);

	    if (variableExists(variables, visBindingTypes[i], bindingColumns[j]))
		option.setAttribute("selected", "selected");
//alert(visBindingTypes[i].toSource());
	    bindingElement.element.appendChild(option);
	}
    }
}

function initOptions(visType, optionElements, options)
{
//alert(options.toSource());
        //var visColumns = variablesToColumns(bindingTypes, variables);

    for (var i in optionElements)
	for (var j in options)
	    if (optionElements[i].optionType == options[j].type)
		if (options[j].name == "hAxis.title" || options[j].name == "vAxis.title")
		{
		    var wireType = xsdTypeToWireType(options[j].dataType);
		    var columns = columnsByWireType(wireType);
		    //alert(columns.toSource());
		    //optionElements[i].element.value = options[j].value;
		}
}

function initAndDraw(container, visType, bindings, variables, options)
{
    initVis(container, visType);
    draw(visualizations[visType], visType, bindings, variables, options);
}

function initWithControlsAndDraw(container, fieldset, toggle, visType, bindingElements, bindingTypes, xsdTypes, bindings, variables, optionElements, options)
{
    // bindingElements & optionElements - only for this visualization
    if (hasSufficientColumns(bindingTypes, xsdTypes, bindings))
    {
        initVis(container, visType);
        initControls(visType, bindingElements, bindingTypes, xsdTypes, variables);
	//initOptions(visType, optionElements, options);
        draw(visualizations[visType], visType, bindings, variables, options);
        toggle.checked = true;
    }
    else
    {
        toggleVisualization(container, fieldset, false); // switch off
        toggle.disabled = true;
    }
}

function hasSufficientColumns(bindingTypes, xsdTypes, bindings)
{
    for (var i in bindings)
    {
        var columns = columnsByBinding(bindings[i], xsdTypes);
	var bindingType = bindingTypeByType(bindingTypes, bindings[i].type);
//alert("BINDING TYPE: " + bindingType.toSource());
//alert(binding.type + " Columns: " + columns.length + " Cardinality: " + binding.cardinality);
        if ("cardinality" in bindingType && bindingType.cardinality > columns.length) return false;
        if ("minCardinality" in bindingType && bindingType.minCardinality > columns.length) return false;
    }
    return true;
}

function variablesToColumns(bindings, variables)
{
	var orderColumns = new Array();
        var restColumns = new Array();
        for (var i in variables)
        {
            var binding = bindingByVariable(bindings, variables[i]);
            if ("order" in binding) orderColumns[binding.order] = variables[i].variable;
            else restColumns = restColumns.concat(variables[i].variable);
        }
        return orderColumns.concat(restColumns);
}

function draw(container, visType, bindings, variables, options)
{
        var visColumns = variablesToColumns(bindings, variables);
//alert(visType + "  " + visColumns.toSource());
//alert(visType + "  " + bindings.toSource() + " " + variables.toSource());
	var view = new google.visualization.DataView(data);
	if (visType.indexOf("Table") == -1) view.setColumns(visColumns); // all columns for Table

	var visOptions = optionsByVisType(visType, options);
	var optArray = { };
	for (var j in visOptions)
	    optArray[options[j].name] = options[j].value; // set visualization options
	
	optArray["height"] = 450; // CSS doesn't work on Table??

	container.draw(view, optArray);

    /*
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            drawMap(container, bindingTypes, variables);
    */
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

function columnsByWireType(wireType)
{
    return typeColumns[wireType];
}

function xsdTypesByBindingType(bindingType, xsdTypes)
{
    var bindingXsdTypes = new Array();
//alert(bindingType.toSource());
    for (var k = 0; k < xsdTypes.length; k++)
        if (bindingType.type == xsdTypes[k].bindingType) // join
            bindingXsdTypes.push(xsdTypes[k].type);

    return bindingXsdTypes;
}

function xsdTypesByBinding(binding, xsdTypes)
{
    var bindingXsdTypes = new Array();
//alert(bindingType.toSource());
    for (var k = 0; k < xsdTypes.length; k++)
        if (binding.type == xsdTypes[k].bindingType) // join
            bindingXsdTypes.push(xsdTypes[k].type);

    return bindingXsdTypes;
}

function wireTypesByBindingType(bindingType, xsdTypes)
{
    var wireTypes = new Array();
    var bindingXsdTypes = xsdTypesByBindingType(bindingType, xsdTypes);
//alert(bindingXsdTypes.toSource());
    for (var k = 0; k < bindingXsdTypes.length; k++)
    {
        var wireType = xsdTypeToWireType(bindingXsdTypes[k]);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType);
    }

    return wireTypes;
}

function wireTypesByBinding(binding, xsdTypes)
{
    var wireTypes = new Array();
    var bindingXsdTypes = xsdTypesByBindingType(binding, xsdTypes);
//alert(bindingXsdTypes.toSource());
    for (var k = 0; k < bindingXsdTypes.length; k++)
    {
        var wireType = xsdTypeToWireType(bindingXsdTypes[k]);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType);
    }

    return wireTypes;
}

function columnsByBindingType(bindingType, xsdTypes)
{
    var bindingColumns = new Array();
    var wireTypes = wireTypesByBindingType(bindingType, xsdTypes);

    for (var k = 0; k < wireTypes.length; k++)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[k])); // add columns for each type

    return bindingColumns;
}

function columnsByBinding(binding, xsdTypes)
{
    var bindingColumns = new Array();
    var wireTypes = wireTypesByBindingType(binding, xsdTypes);

    for (var k = 0; k < wireTypes.length; k++)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[k])); // add columns for each type

    return bindingColumns;
}

function variablesByBindingType(bindingType, variables)
{
//alert(bindingType.toSource());
    var bindingVariables = new Array();

    for (var k = 0; k < variables.length; k++)
        if (bindingType.type == variables[k].bindingType) // join
            bindingVariables.push(variables[k].variable);
//alert(bindingColumns.toSource());
    return bindingVariables;
}

function bindingTypeByType(bindingTypes, type)
{
    for (var i in bindingTypes)
        if (bindingTypes[i].type == type) return bindingTypes[i];
    return null;
}

function bindingTypesByVisType(bindingTypes, visType)
{
    var visBindingTypes = new Array();

    for (var i in bindingTypes)
        if (bindingTypes[i].visType == visType) visBindingTypes.push(bindingTypes[i]);

    return visBindingTypes;
}

function bindingByVariable(bindings, variable)
{
    for (var i in bindings)
        if (bindings[i].type == variable.bindingType) return bindings[i];
    return null;
}

function elementByBindingType(bindingElements, bindingType)
{
    for (var i in bindingElements)
        if (bindingElements[i].bindingType == bindingType) return bindingElements[i];
    return null;
}

function bindingElementsByType(bindingElements, bindingTypes)
{
    var elements = new Array();
    
    for (var i = 0; i < bindingElements.length; i++)
	for (var j = 0; j < bindingTypes.length; j++) // for (var j = 0; j < variables.length; j++)
            if (bindingElements[i].bindingType == bindingTypes[j].type)
                elements.push(bindingElements[i]);

    return elements;
}

function optionsByVisType(visType, options)
{
    var visOptions = new Array();
//alert(bindingType.toSource());
    for (var k = 0; k < options.length; k++)
        if (visType == options[k].visType) // join
            visOptions.push(options[k]);

    return visOptions;
}

function variableExists(variables, bindingType, value)
{//alert(variables.toSource());
    for (var i in variables)
	{
	    //alert("variables[i].bindingType: " + variables[i].bindingType + "\nbindingType.type: " + bindingType.type + "\nvariables[i].variable: " + variables[i].variable + "\nvalue: " + value);
        if (variables[i].bindingType == bindingType.type && variables[i].variable == value) return true;
	}
    return false;
}

function countVariables(data, bindingTypes, xsdTypes)
{
    var variables = new Array();

    for (var i in bindingTypes)
    {
        var bindingColumns = columnsByBindingType(bindingTypes[i], xsdTypes);

        for (var j in bindingColumns)
        {
            var variable = { };
            variable.variable = bindingColumns[j];
            variable.bindingType = bindingTypes[i].type;
            variables.push(variable);
        }
    }
    return variables;
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