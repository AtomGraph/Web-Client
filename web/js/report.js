var report = null;

function Report(table, visualizations, bindings, options, containers, bindingTypeElements)
{

    //alert(Report.bindingTypes.toSource());
    
    this.data = new google.visualization.DataTable(table, 0.6);
    this.visualizations = visualizations;
    this.bindings = bindings;
    this.options = options;
    //this.variables = variables;
    this.containers = containers;
    this.bindingTypeElements = bindingTypeElements;
    this.countColumns();
    for (var i in containers)
	this.initVis(containers[i].element, containers[i].visType);
}

//google.setOnLoadCallback(countColumns(data));
Report.prototype.uri = null;
Report.prototype.data = null;
Report.prototype.typeColumns = new Array();
Report.prototype.containers = new Array();
Report.prototype.visualizations = new Array();
Report.prototype.googleVisualizations = new Array();
Report.prototype.bindings = new Array();
Report.prototype.options = new Array();

Report.XSD_NS = 'http://www.w3.org/2001/XMLSchema#';
Report.visualizationTypes = new Array();
Report.bindingTypes = new Array();
Report.dataTypes = new Array();
Report.xsd2wireTypes = new Array();
Report.xsd2wireTypes[Report.XSD_NS + 'boolean'] = 'boolean';
Report.xsd2wireTypes[Report.XSD_NS + 'string'] = 'string';
Report.xsd2wireTypes[Report.XSD_NS + 'integer'] = 'number';
Report.xsd2wireTypes[Report.XSD_NS + 'decimal'] = 'number';
Report.xsd2wireTypes[Report.XSD_NS + 'float'] = 'number';
Report.xsd2wireTypes[Report.XSD_NS + 'double'] = 'number';
Report.xsd2wireTypes[Report.XSD_NS + 'date'] = 'date';
Report.xsd2wireTypes[Report.XSD_NS + 'dateTime'] = 'datetime';
Report.xsd2wireTypes[Report.XSD_NS + 'time'] = 'timeofday';

Report.init = function(visualizationTypes, bindingTypes, dataTypes) // static types (classes)
{
    Report.visualizationTypes = visualizationTypes;
    Report.bindingTypes = bindingTypes;
    Report.dataTypes = dataTypes;
    //Report.optionTypes = optionTypes;
}

Report.prototype.setVisualizations = function(visualizations)
{
    this.visualizations = visualizations;
}

Report.prototype.setBindings = function(bindings)
{
    this.bindings = bindings;
}

Report.prototype.setVariables = function(variables)
{
    this.variables = variables;
}

Report.prototype.initVis = function(containerElement, visType)
{
    if (visType.indexOf("Table") != -1) this.googleVisualizations[visType] = new google.visualization.Table(containerElement);
    if (visType.indexOf("ScatterChart") != -1) this.googleVisualizations[visType] = new google.visualization.ScatterChart(containerElement);
    if (visType.indexOf("LineChart") != -1) this.googleVisualizations[visType] = new google.visualization.LineChart(containerElement);
    if (visType.indexOf("PieChart") != -1) this.googleVisualizations[visType] = new google.visualization.PieChart(containerElement);
    if (visType.indexOf("BarChart") != -1) this.googleVisualizations[visType] = new google.visualization.BarChart(containerElement);
    if (visType.indexOf("ColumnChart") != -1) this.googleVisualizations[visType] = new google.visualization.ColumnChart(containerElement);
    if (visType.indexOf("AreaChart") != -1) this.googleVisualizations[visType] = new google.visualization.AreaChart(containerElement);
    if (visType.indexOf("Map") != -1) this.googleVisualizations[visType] = new google.visualization.Map(containerElement);
}

Report.prototype.fillControls = function(visualization)
{
    var visBindingTypes = objectsByVisType(visualization.type, Report.bindingTypes);
    //alert(visBindingTypes.toSource());
//alert(this.bindingTypeElements.toSource());

    for (var i in visBindingTypes)
    {
//alert(visBindingTypes[i].type);
	var bindingElement = this.elementByBindingType(visBindingTypes[i].type);
	var bindingColumns = this.columnsByBindingType(visBindingTypes[i].type);
	//alert(bindingElement.toSource());
//alert(visBindingTypes[i].toSource() + " " + bindingColumns.toSource());

	if (!(("cardinality" in visBindingTypes[i] && visBindingTypes[i].cardinality == 1) ||
		("maxCardinality" in visBindingTypes[i] && visBindingTypes[i].maxCardinality == 1)))
	    bindingElement.element.multiple = "multiple";

	for (var j in bindingColumns)
	{
	    var option = document.createElement("option");
	    option.appendChild(document.createTextNode(this.data.getColumnLabel(bindingColumns[j])));
	    option.setAttribute("value", bindingColumns[j]);

	    if (variableExists(this.variables, visBindingTypes[i], bindingColumns[j]))
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
//alert(bindings.toSource());
    initVis(container, visType);
    draw(this.googleVisualizations[visType], visType, bindings, variables, options);
}

function initWithControlsAndDraw(container, fieldset, toggle, visType, bindingTypeElements, bindingTypes, xsdTypes, bindings, variables, optionElements, options)
{
    //alert(bindings.toSource());
    // bindingElements & optionElements - only for this visualization
    if (hasSufficientColumns(bindingTypes, xsdTypes, bindings))
    {
        initVis(container, visType);
        initControls(visType, bindingTypeElements, bindingTypes, xsdTypes, variables);
	//initOptions(visType, optionElements, options);
        draw(this.googleVisualizations[visType], visType, bindings, variables, options);
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
	var bindingType = objectByType(bindings[i].type, bindingTypes);
        if ("cardinality" in bindingType && bindingType.cardinality > columns.length) return false;
        if ("minCardinality" in bindingType && bindingType.minCardinality > columns.length) return false;
	// maxCardinality???
    }
    return true;
}

function variablesToColumns(bindings, variables)
{
//alert(bindings.toSource());
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

Report.prototype.show = function()
{
    for (var i in this.visualizations)
    {
	this.fillControls(this.visualizations[i]);
	this.draw(this.visualizations[i]);
    }
}

Report.prototype.draw = function(visualization)
{
//alert(visualization.toSource());

        var visVariables = objectsByVisType(visualization.type, this.variables);
//alert(visVariables.toSource());
	var visColumns = variablesToColumns(this.bindings, visVariables);
//alert(visType + "  " + visColumns.toSource());
//alert(visType + "  " + bindings.toSource() + " " + variables.toSource());
	var view = new google.visualization.DataView(this.data);
	if (visualization.type.indexOf("Table") == -1) view.setColumns(visColumns); // all columns for Table

	var visOptions = objectsByVisType(visualization.type, this.options);
	var optArray = { };
	for (var j in visOptions)
	    optArray[visOptions[j].name] = visOptions[j].value; // set visualization options
	
	optArray["height"] = 450; // CSS doesn't work on Table??

	var container = this.googleVisualizations[visualization.type];
	container.draw(view, optArray);

    /*
    if (visType.indexOf("Map") != -1)
        if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
            drawMap(container, bindingTypes, variables);
    */
}

Report.prototype.countColumns = function()
{
        typeColumns = { "string": [], "number": [], "date": [], "lat": [], "lng": [] };
        
	for (var i = 0; i < this.data.getNumberOfColumns(); i++)
	{
            if (this.data.getColumnType(i) == "string") typeColumns.string.push(i);
            if (this.data.getColumnType(i) == "date")
            {
                typeColumns.string.push(i); // date columns also treated as strings
                typeColumns.date.push(i);
            }
            if (this.data.getColumnType(i) == "number") // lat/lng columns
            {
                typeColumns.number.push(i);
		var range = this.data.getColumnRange(i);
		if (range.min >= -90 && range.max <= 90) typeColumns.lat.push(i);
		if (range.min >= -180 && range.max <= 180) typeColumns.lng.push(i);
            }
	}
}

Report.xsdTypeToWireType = function(xsdType)
{
    return Report.xsd2wireTypes[xsdType];
}

function columnsByWireType(wireType)
{
    return typeColumns[wireType];
}

function objectsByBindingType(objects, bindingType)
{
    var bindingTypeObjects = new Array();
//alert(bindingType.toSource());
    for (var i in objects)
        if (bindingType == objects[i].bindingType) // join
            bindingTypeObjects.push(objects[i].type);

    return bindingTypeObjects;
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

Report.wireTypesByBindingType = function(bindingType)
{
    var wireTypes = new Array();
    var bindingXsdTypes = objectsByBindingType(Report.dataTypes, bindingType);
//alert(bindingXsdTypes.toSource());
    for (var i in bindingXsdTypes)
    {
        var wireType = Report.xsdTypeToWireType(bindingXsdTypes[i]);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }

    return wireTypes;
}

function wireTypesByBinding(binding, xsdTypes)
{
    var wireTypes = new Array();
    var bindingXsdTypes = objectsByBindingType(xsdTypes, binding.type);
//alert(bindingXsdTypes.toSource());
    for (var k = 0; k < bindingXsdTypes.length; k++)
    {
        var wireType = xsdTypeToWireType(bindingXsdTypes[k]);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType);
    }

    return wireTypes;
}

Report.prototype.columnsByBindingType = function(bindingType)
{
//alert(bindingType);
    var bindingColumns = new Array();
    var wireTypes = Report.wireTypesByBindingType(bindingType);
//alert(wireTypes.toSource());

    for (var i in wireTypes)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[i])); // add columns for each type

    return bindingColumns;
}

function columnsByBinding(binding, xsdTypes)
{
    var bindingColumns = new Array();
    var wireTypes = wireTypesByBindingType(binding.type, xsdTypes);

    for (var i in wireTypes)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[i])); // add columns for each type

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

function objectByVisType(visType, objects)
{
    for (var i in objects)
        if (objects[i].visType == visType) return objects[i];
    return null;
}

function objectByType(type, objects)
{
    for (var i in objects)
        if (objects[i].type == type) return objects[i];
    return null;
}

function objectsByVisType(visType, objects)
{
    var visObjects = new Array();

    for (var i in objects)
        if (objects[i].visType == visType) visObjects.push(objects[i]);

    return visObjects;
}

function bindingByVariable(bindings, variable)
{
    for (var i in bindings)
        if (bindings[i].type == variable.bindingType) return bindings[i];
    return null;
}

Report.prototype.elementByBindingType = function(bindingType)
{
    for (var i in this.bindingTypeElements)
        if (this.bindingTypeElements[i].bindingType == bindingType) return this.bindingTypeElements[i];
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

function variableExists(variables, bindingType, value)
{//alert(variables.toSource());
    for (var i in variables)
        if (variables[i].bindingType == bindingType.type && variables[i].variable == value) return true;
    return false;
}

Report.prototype.countVariables = function()
{
    var variables = new Array();

    for (var i in this.bindings)
    {
        var bindingColumns = this.columnsByBindingType(this.bindings[i].type);
        for (var j in bindingColumns)
        {
            var variable = { };
            variable.variable = bindingColumns[j];
	    variable.binding = this.bindings[i].binding;
            variable.bindingType = this.bindings[i].type;
	    variable.visType = this.bindings[i].visType;
	    variables.push(variable);
        }
    }
//alert(variables.toSource());
    return variables;
}

function toggleVisualization(container, fieldset, show)
{//alert(container.id);
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

Report.prototype.getBindingVariables = function(binding)
{
	var variables = new Array();

	for (var i in this.bindingTypeElements.options)
            if (this.bindingTypeElements.options[i].selected)
                {
                    var variable = { };
                    variable.variable = Number(this.bindingTypeElements.options[i].value);
		    variable.binding = binding.binding;
		    variable.bindingType = binding.type;
		    variable.visType = binding.visType;
                    variables.push(variable);
                }
	return variables;
}

Report.prototype.getVariablesFromControls = function() // bindings???
{
//alert(bindingElements.toSource());
//alert(bindings.toSource());
    var variables = new Array();
    for (var i in this.bindingTypeElements)
    {
	var binding = objectByType(this.bindingTypeElements[i].bindingType, this.bindings);
//alert(binding.toSource());
        variables = variables.concat(this.getBindingVariables(this.bindingTypeElements[i].element, binding));
    }
    return variables;
}