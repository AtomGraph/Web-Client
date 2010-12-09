var report = null;

function Visualization(bindings, variables, options, container)
{
    //var visualization = this;
    this.bindings = bindings;
//alert(this.bindings.toSource());
    this.container = container;
//alert(this.container.toSource());
    for (var i in this.bindings)
    {
	var binding = this.bindings[i];
	var bindingVariables = variables.results.bindings.filter(function(variable) { return variable.binding.value == binding.binding.value; } );
	binding.constructor = Binding;
	binding.constructor(bindingVariables);

//alert(bindingVariables.toSource());
	//binding.variables = bindingVariables;
    }
    this.init = Visualization.prototype.init;
    this.init();
    //alert(visualization.googleVis.toSource());

}
Visualization.prototype.alert = function()
{
    alert(this.bindings.toSource());
    //alert("whatup!");
}
Visualization.prototype.init = function()
{
    if (this.type.value.indexOf("Table") != -1) this.googleVis = new google.visualization.Table(this.container);
    if (this.type.value.indexOf("ScatterChart") != -1) this.googleVis = new google.visualization.ScatterChart(this.container);
    if (this.type.value.indexOf("LineChart") != -1) this.googleVis = new google.visualization.LineChart(this.container);
    if (this.type.value.indexOf("PieChart") != -1) this.googleVis = new google.visualization.PieChart(this.container);
    if (this.type.value.indexOf("BarChart") != -1) this.googleVis = new google.visualization.BarChart(this.container);
    if (this.type.value.indexOf("ColumnChart") != -1) this.googleVis = new google.visualization.ColumnChart(this.container);
    if (this.type.value.indexOf("AreaChart") != -1) this.googleVis = new google.visualization.AreaChart(this.container);
    if (this.type.value.indexOf("Map") != -1) this.googleVis = new google.visualization.Map(this.container);
}

function Binding(variables)
{
    this.variables = variables;
    //alert(this.variables.toSource());
}

Binding.prototype.getColumns = function()
{
    var columns = new Array();
    var wireTypes = Report.wireTypesByBindingType(binding);

    for (var i in wireTypes)
        columns = columns.concat(columnsByWireType(wireTypes[i])); // add columns for each type

    return columns;
}

function Variable()
{

}

function DataType()
{

}

function Option()
{

}

function Report(table, visualizations, bindings, variables, options, containers)
{
    //alert(Report.bindingTypes.results.bindings.toSource());
    
    this.data = new google.visualization.DataTable(table, 0.6);
    this.visualizations = visualizations.results.bindings;
    // join and split the whole thing
    for (var j in this.visualizations)
    {
	var visualization = this.visualizations[j];
	var visBindings = bindings.results.bindings.filter(function(binding) { return binding.visualization.value == visualization.visualization.value; } );
	var visContainer = containers.filter(function(container) { return container.visType == visualization.type.value; } )[0];
	//var visOptions = options.results.bindings.filter(function(option) { return option.visualization.value == visualization.visualization.value; } );
	var visOptions = new Array();
	visualization.constructor = Visualization;
	visualization.constructor(visBindings, variables, visOptions, visContainer);
	//visualization.bindings = visBindings;
    }
    //alert(this.visualizations.toSource());
    //this.bindings = bindings.results.bindings;
    //this.options = options.results.bindings;
    this.countColumns();
    //this.containers = containers;
    //for (var i in this.containers)
	//this.initVis(this.containers[i].element, this.containers[i].visType);
}

//google.setOnLoadCallback(countColumns(data));
Report.prototype.uri = null;
Report.prototype.data = null;
Report.prototype.typeColumns = new Array();
Report.prototype.visTypeToggleElements = new Array();
Report.prototype.visTypeFieldsetElements = new Array();
//Report.prototype.containers = new Array();
Report.prototype.bindingTypeElements = new Array();
Report.prototype.visualizations = new Array();
//Report.prototype.googleVisualizations = new Array();
//Report.prototype.bindings = new Array();
//Report.prototype.options = new Array();

Report.XSD_NS = 'http://www.w3.org/2001/XMLSchema#';
Report.visualizationTypes = new Array();
Report.bindingTypes = new Array();
Report.dataTypes = new Array();
Report.optionTypes = new Array();
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

Report.shit = function(bindingTypes, dataTypes) // static types (classes)
{
    //alert(bindingTypes.toSource());
    Report.bindingTypes = bindingTypes; //  QUIRK -- should not be necessary if bindings are saved with "order"!!!
    Report.dataTypes = dataTypes;
}

Report.init = function(visualizationTypes, bindingTypes, dataTypes, optionTypes) // static types (classes)
{
    //alert(bindingTypes.toSource());
    Report.visualizationTypes = visualizationTypes;
    Report.bindingTypes = bindingTypes;
    Report.dataTypes = dataTypes;
    Report.optionTypes = optionTypes;
    //alert(optionTypes.toSource());
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

    var bindings = this.getBindingsWithoutVariables();
//alert(bindings.toSource());
    var missingVars = this.createVariables(bindings);
    //alert(missingVars.toSource());
    this.variables = this.variables.results.bindings.concat(missingVars);
    //alert(this.variables.toSource());
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

Report.prototype.fillControls = function(visualization)
{
//alert(visualization.toSource());
    //var visTypeResults = filterResults(Report.bindingTypes.results.bindings, 'visType', visualization.type.value);
    var visBindingTypes = Report.bindingTypes.results.bindings.filter(function(el) { return el.visType.value == visualization.type.value; } )
//alert(visBindingTypes.toSource());

    for (var i in visBindingTypes)
    {
//alert(visBindingTypes[i].type);
	//var bindingElement = this.elementByBindingType(visTypeResults[i].type.value);
	var bindingElement = this.bindingTypeElements.filter(function(el) { return el.bindingType == visBindingTypes[i].type.value; } )[0];
//alert(bindingElement.toSource());

	var bindingColumns = this.columnsByBindingType(visBindingTypes[i]);
//alert(visBindingTypes[i].toSource() + "\n\n" + bindingColumns.toSource());

	if (!(("cardinality" in visBindingTypes[i] && visBindingTypes[i].cardinality.value == 1) ||
		("maxCardinality" in visBindingTypes[i] && visBindingTypes[i].maxCardinality.value == 1)))
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
//alert(Report.bindingTypes.results.bindings.toSource());
    //var visBindings = objectsByVisType(visualization.type.value, this.bindings);
    var visBindings = this.bindings.results.bindings.filter(function(el) { return el.visualization.value == visualization.visualization.value; } );
//alert(visBindings.toSource());

    for (var i in visBindings)
    {
//alert(visBindings[i].toSource());
        var columns = this.columnsByBinding(visBindings[i]);
	//var bindingType = objectByType(visBindings[i].type, Report.bindingTypes.results.bindings);
	//var bindingType = filterResults(Report.bindingTypes.results.bindings, 'type', visBindings[i].type)[0];
	var bindingType = Report.bindingTypes.results.bindings.filter(function(el) { return el.type.value == visBindings[i].type.value; } )[0];

//alert(bindingType.toSource());

        if ("cardinality" in bindingType && bindingType.cardinality.value > columns.length) return false;
        if ("minCardinality" in bindingType && bindingType.minCardinality.value > columns.length) return false;
	// maxCardinality???
    }
    return true;
}

Report.prototype.columnsByVariables = function(variables)
{
//alert(variables.toSource());
	var orderColumns = new Array();
        var restColumns = new Array();
        for (var i in variables)
        {
            //var binding = bindingByVariable(this.bindings.results.bindings, variables[i]);
	    //var binding = this.bindings.results.bindings.filter(function(el) { return el.binding.value == variables[i].binding.value; } )[0];
	    var bindingType = Report.bindingTypes.results.bindings.filter(function(el) { return el.type.value == variables[i].bindingType.value; } )[0];
//alert(bindingType.toSource());
//alert(bindingType.toSource() + "\n\n" + variables[i].toSource() + "\n\n" + orderColumns.toSource() + "\n\n" + parseInt(bindingType.order.value));
//alert(variables[i].variable.value);
//alert(orderColumns.toSource() + "\n\n" + restColumns.toSource());
//alert("order: " + bindingType.order.value + "\n\n" + "value: " + variables[i].variable.value);
// QUIRK -- why order is string???
// QUIRK -- binding.order should be used instead of bindingType.order
	    if ("order" in bindingType) orderColumns[parseInt(bindingType.order.value)] = parseInt(variables[i].variable.value);
            else restColumns = restColumns.concat(parseInt(variables[i].variable.value));
        }
	var columns = orderColumns.concat(restColumns);
//alert(columns.toSource());
	return columns;
}

Report.prototype.showWithControls = function()
{
    //alert(this.visTypeToggleElements.toSource());
    
    for (var i in this.visualizations.results.bindings)
    {
	var visualization = this.visualizations.results.bindings[i];
//alert(visualization.toSource());
	//var toggleElement = objectByVisType(this.visualizations[i].type.value, this.visTypeToggleElements);
	var toggleElement = this.visTypeToggleElements.filter(function(el) { return el.visType == visualization.type.value; } )[0];

//alert(toggleElement.toSource());

	if (this.hasSufficientColumns(this.visualizations.results.bindings[i]))
	{
	    //alert("sufficient" + this.visualizations[i].toSource());
	    this.fillControls(this.visualizations.results.bindings[i]);
	    this.draw(this.visualizations.results.bindings[i]);
	    toggleElement.element.checked = true;
	}
        else
	{
	    //alert("nope");
	    this.toggleVisualization(this.visualizations.results.bindings[i], false); // switch off
	    toggleElement.element.disabled = true;
	}
    }
}

Report.prototype.show = function()
{
    for (var i in this.visualizations.results.bindings)
	//if (this.hasSufficientColumns(this.visualizations[i]))
	    this.draw(this.visualizations.results.bindings[i]);
	//else alert("nope");
}

Report.prototype.draw = function(visualization)
{
//alert(this.variables.toSource());
	//var visVariables = objectsByVisType(visualization.type.value, this.variables);
	var visVariables = this.variables.filter(function(el) { return el.visType.value == visualization.type.value; } )
//alert(visualization.toSource() + "\n\n" + visVariables.toSource());
	var visColumns = this.columnsByVariables(visVariables);
//alert(visualization.toSource() + "\n\n" + visColumns.toSource());
/*
var columnArr = new Array();
for (var i in visColumns)
    columnArr = columnArr.concat(parseInt(visColumns[i].value));
*/
//alert(visualization.toSource() + " " + columnArr.toSource());
//alert(visType + "  " + bindings.toSource() + " " + variables.toSource());
	var view = new google.visualization.DataView(this.data);
	if (visualization.type.value.indexOf("Table") == -1) view.setColumns(visColumns); // all columns for Table

	//var visOptions = objectsByVisType(visualization.type.value, this.options);
	var visOptions = this.options.filter(function(el) { return el.visType == visualization.type.value; } )

	var optArray = { };
	/*
	for (var j in visOptions)
	{
	    var name = visOptions[j].name;
	    var value = visOptions[j].value;
	    if (visOptions[j].name == "hAxis.title")
	    {
		name = "hAxis";
		value = { title: visOptions[j].value }
	    }
	    if (visOptions[j].name == "vAxis.title")
	    {
		name = "vAxis";
		value = { title: visOptions[j].value }
	    }
	    optArray[name] = value; // set visualization options
	}
	*/
	optArray["height"] = 450; // CSS doesn't work on Table??
	optArray["allowHtml"] = true; // to allow hyperlinks in Table
//alert(visualization.type + " " + optArray.toSource());
	var googleVis = this.googleVisualizations[visualization.type.value];
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

Report.wireTypesByBindingType = function(bindingType)
{
//alert(bindingType.toSource());
    var wireTypes = new Array();
    var bindingTypeDataTypes = Report.dataTypes.results.bindings.filter(function(el) { return el.bindingType.value == bindingType.type.value; } )

//alert(bindingTypeResults.toSource());
    for (var i in bindingTypeDataTypes)
    {
        var wireType = Report.xsdTypeToWireType(bindingTypeDataTypes[i].type.value);
        if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }
//alert(wireTypes);
    return wireTypes;
}

Report.prototype.columnsByBindingType = function(bindingType)
{
//alert(bindingType.toSource());
    var bindingColumns = new Array();
    var wireTypes = Report.wireTypesByBindingType(bindingType);
//alert(wireTypes.toSource());

    for (var i in wireTypes)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[i])); // add columns for each type
//alert(bindingType.toSource() + "\n\n" + bindingColumns.toSource());
    return bindingColumns;
}

Report.prototype.columnsByBinding = function(binding)
{
    var bindingColumns = new Array();
    var wireTypes = Report.wireTypesByBindingType(binding);

    for (var i in wireTypes)
        bindingColumns = bindingColumns.concat(columnsByWireType(wireTypes[i])); // add columns for each type

    return bindingColumns;
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

Report.prototype.createVariables = function(bindings)
{
    var variables = new Array();

//alert(bindings.toSource());
//alert(bindings.length);
    for (var i in bindings)
    {
//alert(bindings[i].toSource());

	var bindingType = Report.bindingTypes.results.bindings.filter(function(el) { return el.type.value == bindings[i].type.value; } )[0];
        var bindingColumns = this.columnsByBindingType(bindingType);
        for (var j in bindingColumns)
        {
            var variable = { };
            variable.variable = { 'type' : 'typed-literal', 'value' : bindingColumns[j] }; // 'datatype
	    variable.binding = eval(bindings[i].binding.toSource());
            variable.bindingType = eval(bindings[i].type.toSource());
	    variable.visualization = eval(bindings[i].visualization.toSource());
	    variable.visType = eval(bindings[i].visType.toSource());
	    variables.push(variable);
        }
    }
//alert(variables.toSource());
    return variables;
}

Report.prototype.toggleVisualization = function(visType, show)
{
//alert(visType.toSource());
    //var container = objectByVisType(visType.type, this.containers);
    var container = this.containers.filter(function(el) { return el.visType == visType.type.value; } )[0];
    //var fieldset = objectByVisType(visType.type, this.visTypeFieldsetElements);
    var fieldset = this.visTypeFieldsetElements.filter(function(el) { return el.visType == visType.type.value; } )[0];
    //var toggle = objectByVisType(visType.type, this.visTypeToggleElements);
    var toggle = this.visTypeToggleElements.filter(function(el) { return el.visType == visType.type.value; } )[0];

    if (show)
    {
	    container.element.style.display = "block";
	    fieldset.element.style.display = "block";
	    toggle.element.checked = true;
    }
    else
    {
	    container.element.style.display = "none";
	    fieldset.element.style.display = "none";
	    toggle.element.checked = false;
    }
}

Report.prototype.getBindingVariables = function(bindingTypeElement, binding)
{
alert(binding.toSource());

    var variables = new Array();

    for (var i in bindingTypeElement.element.options)
	if (bindingTypeElement.element.options[i].selected)
	    {
		var variable = { };
		variable.variable = Number(bindingTypeElement.element.options[i].value);
		variable.binding = binding.binding;
		variable.bindingType = binding.type;
		variable.visualization = binding.visualization;
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
	//var binding = objectByType(this.bindingTypeElements[i].bindingType, this.bindings);
	var element = this.bindingTypeElements[i];
	var binding = this.bindings.results.bindings.filter(function(el) { return el.type.value == element.bindingType; } )[0];

alert(binding.toSource());
        variables = variables.concat(this.getBindingVariables(this.bindingTypeElements[i], binding));
    }
    return variables;
}

Report.prototype.getBindingsWithoutVariables = function()
{
    var bindings = new Array();
    var report = this;
//alert(report.bindings.toSource());
//alert(this.variables.toSource());

    for (var i in this.bindings.results.bindings)
    {
	//var variables = objectsByBindingType(this.variables, this.bindings.results.bindings[i].type.value); // byBinding?
	var variables = this.variables.results.bindings.filter(function(el) { return el.binding.value == report.bindings.results.bindings[i].binding.value; } );
//alert(this.bindings.results.bindings[i].binding.value + " " + variables.toSource());
	if (variables.length == 0) bindings.push(this.bindings.results.bindings[i]);
    }
//alert(bindings.toSource());
    return bindings;
}