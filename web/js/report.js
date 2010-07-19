var report = null;

function Report(table, visualizations, bindings, options, containers)
{
    //alert(Report.bindingTypes.toSource());
    
    this.data = new google.visualization.DataTable(table, 0.6);
    this.visualizations = visualizations;
    this.bindings = bindings;
    this.options = options;
    this.countColumns();
    this.containers = containers;
    for (var i in this.containers)
	this.initVis(this.containers[i].element, this.containers[i].visType);
}

//google.setOnLoadCallback(countColumns(data));
Report.prototype.uri = null;
Report.prototype.data = null;
Report.prototype.typeColumns = new Array();
Report.prototype.visTypeToggleElements = new Array();
Report.prototype.visTypeFieldsetElements = new Array();
Report.prototype.containers = new Array();
Report.prototype.bindingTypeElements = new Array();
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
    //alert(variables.toSource());
    this.variables = variables;
}

Report.prototype.setVisTypeToggleElements = function(visTypeToggleElements)
{
    this.visTypeToggleElements = visTypeToggleElements;
}

Report.prototype.setVisTypeFieldsetElements = function(visTypeFieldsetElements)
{
    this.visTypeFieldsetElements = visTypeFieldsetElements;
}

Report.prototype.setBindingTypeElements = function(bindingTypeElements)
{
    this.bindingTypeElements = bindingTypeElements;
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

	    if (this.variableExists(visBindingTypes[i], bindingColumns[j]))
		option.setAttribute("selected", "selected");
//alert(visBindingTypes[i].toSource());
	    bindingElement.element.appendChild(option);
	}
    }
}

function initOptions(visType, optionElements, options)
{
//alert(options.toSource());
        //var visColumns = columnsByVariables(this.bindings, variables);

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

Report.prototype.hasSufficientColumns = function(visualization)
{
    var visBindings = objectsByVisType(visualization.type, this.bindings);

    for (var i in visBindings)
    {
        var columns = this.columnsByBinding(visBindings[i]);
	var bindingType = objectByType(visBindings[i].type, Report.bindingTypes);
        if ("cardinality" in bindingType && bindingType.cardinality > columns.length) return false;
        if ("minCardinality" in bindingType && bindingType.minCardinality > columns.length) return false;
	// maxCardinality???
    }
    return true;
}

Report.prototype.columnsByVariables = function(variables)
{
//alert(bindings.toSource());
	var orderColumns = new Array();
        var restColumns = new Array();
        for (var i in variables)
        {
            var binding = bindingByVariable(this.bindings, variables[i]);
            if ("order" in binding) orderColumns[binding.order] = variables[i].variable;
            else restColumns = restColumns.concat(variables[i].variable);
        }
        return orderColumns.concat(restColumns);
}

Report.prototype.showWithControls = function()
{
    //alert(this.visTypeToggleElements.toSource());
    
    for (var i in this.visualizations)
    {
	var toggleElement = objectByVisType(this.visualizations[i].type, this.visTypeToggleElements);

	if (this.hasSufficientColumns(this.visualizations[i]))
	{
	    //alert("sufficient" + this.visualizations[i].toSource());
	    this.fillControls(this.visualizations[i]);
	    this.draw(this.visualizations[i]);
	    toggleElement.element.checked = true;
	}
        else
	{
	    //alert("nope");
	    this.toggleVisualization(this.visualizations[i], false); // switch off
	    toggleElement.element.disabled = true;
	}
    }
}

Report.prototype.show = function()
{
    for (var i in this.visualizations)
	//if (this.hasSufficientColumns(this.visualizations[i]))
	    this.draw(this.visualizations[i]);
	//else alert("nope");
}

Report.prototype.draw = function(visualization)
{
//alert(visualization.toSource());

        var visVariables = objectsByVisType(visualization.type, this.variables);
//alert(visVariables.toSource());
	var visColumns = this.columnsByVariables(visVariables);
//alert(visType + "  " + visColumns.toSource());
//alert(visType + "  " + bindings.toSource() + " " + variables.toSource());
	var view = new google.visualization.DataView(this.data);
	if (visualization.type.indexOf("Table") == -1) view.setColumns(visColumns); // all columns for Table

	var visOptions = objectsByVisType(visualization.type, this.options);
	var optArray = { };
	for (var j in visOptions)
	    optArray[visOptions[j].name] = visOptions[j].value; // set visualization options
	
	optArray["height"] = 450; // CSS doesn't work on Table??

	var googleVis = this.googleVisualizations[visualization.type];
	googleVis.draw(view, optArray);

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

Report.prototype.columnsByBinding = function(binding)
{
    var bindingColumns = new Array();
    var wireTypes = Report.wireTypesByBindingType(binding.type, this.dataTypes);

    for (var i in wireTypes)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[i])); // add columns for each type

    return bindingColumns;
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
        if (bindings[i].binding == variable.binding) return bindings[i];
    return null;
}

Report.prototype.elementByBindingType = function(bindingType)
{
    for (var i in this.bindingTypeElements)
        if (this.bindingTypeElements[i].bindingType == bindingType) return this.bindingTypeElements[i];
    return null;
}

Report.prototype.variableExists = function(bindingType, value)
{//alert(variables.toSource());
    for (var i in this.variables)
        if (this.variables[i].bindingType == bindingType.type && this.variables[i].variable == value) return true;
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

Report.prototype.toggleVisualization = function(visType, show)
{//alert(container.id);
//alert(visType.toSource());
    var container = objectByVisType(visType.type, this.containers);
    var fieldset = objectByVisType(visType.type, this.visTypeFieldsetElements);
    
    if (show)
    {
	    container.element.style.display = "block";
	    fieldset.element.style.display = "block";
    }
    else
    {
	    container.element.style.display = "none";
	    fieldset.element.style.display = "none";
    }
}

Report.prototype.getBindingVariables = function(bindingTypeElement, binding)
{
	var variables = new Array();

	for (var i in bindingTypeElement.element.options)
            if (bindingTypeElement.element.options[i].selected)
                {
                    var variable = { };
                    variable.variable = Number(bindingTypeElement.element.options[i].value);
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
        variables = variables.concat(this.getBindingVariables(this.bindingTypeElements[i], binding));
    }
    return variables;
}