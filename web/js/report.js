var report = null;

function Visualization(report, bindings, variables, options, container)
{
    var visualization = this;
//alert(visualization.type.value);
    this.report = report;
//alert(Report.visualizationTypes.toSource());
    //this.type = Report.visualizationTypes.filter(function(visType) { return visType.type.value == visualization.type.value; } );
//alert(this.type.toSource());
    this.variables = variables;
//alert(this.type.value + "\n\n" + this.variables.toSource());
    this.bindings = bindings;
//alert(this.bindings.toSource());
    this.container = container;
    for (var i in this.bindings)
    {
	var binding = this.bindings[i];
	var bindingVariables = this.variables.filter(function(variable) { return variable.binding.value == binding.binding.value; } );
	binding.constructor = Binding;
	binding.constructor(this.report, this, bindingVariables);

//alert(bindingVariables.toSource());
	//binding.variables = bindingVariables;
    }
    this.getColumns = Visualization.prototype.getColumns;
    this.columns = this.getColumns();
    this.init = Visualization.prototype.init;
    this.init();
    //alert(visualization.googleVis.toSource());

}
Visualization.prototype.getColumns = function()
{
//this.getVariables = Visualization.prototype.getVariables;
//alert(this.visType.toSource());
//alert(this.variables.length);
    var orderColumns = new Array();
    var restColumns = new Array();
//alert(this.type.value + "\n\n" + this.bindings.length);
    for (var i in this.bindings)
    {
	var binding = this.bindings[i];
	//alert(binding.variables.toSource());
	for (var j in binding.variables)
	{
	    var variable = binding.variables[j];
	    //alert(variable.toSource());
	    if ("order" in binding) orderColumns[parseInt(binding.order.value)] = parseInt(variable.variable.value);
	    else restColumns = restColumns.concat(parseInt(variable.variable.value));
	}
    }
    /*
    for (var i in this.variables)
    {
	var variable = this.variables[i];
// QUIRK -- why order is string???
	if ("order" in variable.binding.type) orderColumns[parseInt(variable.binding.type.order.value)] = parseInt(variable.variable.value);
	else restColumns = restColumns.concat(parseInt(variable.variable.value));
    }
    */
    var columns = orderColumns.concat(restColumns);
//alert(this.type.toSource() + "\n\n" + columns.toSource());
    return columns;

}
Visualization.prototype.hasSufficientColumns = function()
{
    for (var i in this.bindings)
    {
	var binding = this.bindings[i];
	//binding.getColumns = Binding.prototype.getColumns;
        //var columns = binding.getColumns();

//alert(binding.columns.toSource());

        if ("cardinality" in binding && binding.cardinality.value > binding.columns.length) return false;
        if ("minCardinality" in binding && binding.minCardinality.value > binding.columns.length) return false;
	// maxCardinality???
    }
    return true;
}
Visualization.prototype.show = function()
{
//alert(this.columns.toSource());
//alert(this.type.value + "\n\n" + this.columns.toSource());
    this.view = new google.visualization.DataView(this.report.data);
    //this.getColumns = Visualization.prototype.getColumns;
    if (this.type.value.indexOf("Table") == -1) this.view.setColumns(this.columns); // all columns for Table
//alert(this.type.toSource() + "\n\n" + this.getColumns());
    var optArray = { }; // this.options
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
    this.googleVis.draw(this.view, optArray);

/*
if (visType.indexOf("Map") != -1)
    if (typeColumns.lat.length > 0 && typeColumns.lng.length > 0)
	drawMap(container, bindingTypes, variables);
*/
}
Visualization.prototype.toggle = function(show)
{
    if (show)
    {
	    this.container.style.display = "block";
	    this.fieldset.style.display = "block";
	    //this.toggle.checked = true;
    }
    else
    {
	    this.container.style.display = "none";
	    this.fieldset.style.display = "none";
	    //this.toggle.checked = false;
    }
}
Visualization.prototype.fillControls = function()
{
//alert(visualization.toSource());
    //var visTypeResults = filterResults(Report.bindingTypes.results.bindings, 'visType', visualization.type.value);
    //var visBindingTypes = Report.bindingTypes.results.bindings.filter(function(el) { return el.visType.value == visualization.type.value; } )
//alert(this.bindings.toSource());

    for (var i in this.bindings)
    {
	var binding = this.bindings[i];
//alert(binding.variables.length);

	//var columns = binding.getColumns();
	var columns = binding.columns;
//alert(columns.toSource());
//alert(visBindingTypes[i].toSource() + "\n\n" + bindingColmns.toSource());
//alert(binding.type.toSource());
	if (!(("cardinality" in binding && binding.cardinality.value == 1) ||
		("maxCardinality" in binding && binding.maxCardinality.value == 1)))
	    binding.control.multiple = "multiple";

	for (var j in columns)
	{
	    var option = document.createElement("option");
	    option.appendChild(document.createTextNode(this.report.data.getColumnLabel(columns[j])));
	    option.setAttribute("value", columns[j]);

	    binding.hasVariable = Binding.prototype.hasVariable;
	    if (binding.hasVariable(columns[j]))
		option.setAttribute("selected", "selected");
//alert(binding.hasVariable(columns[j]));
	    binding.control.appendChild(option);
	}
    }
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

function Binding(report, visualization, variables)
{
    var binding = this;
//alert(Report.bindingTypes.toSource());
    this.report = report;
    this.visualization = visualization;
    //this.control = control;
    var bindingType = Report.bindingTypes.filter(function(bindingType) { return bindingType.type.value == binding.type.value; } )[0];
    //alert(bindingType.toSource());
    // QUIRK -- bindingType should not be used, if its properties are already saved with binding
    this.type = bindingType.type;
    this.label = bindingType.label;
    this.dataTypes = bindingType.dataTypes;
    if ("order" in bindingType) this.order = bindingType.order;
    if ("cardinality" in bindingType) this.cardinality = bindingType.cardinality;
    if ("minCardinality" in bindingType) this.minCardinality = bindingType.minCardinality;
    //alert(bindingType.toSource());
    this.variables = variables;
    for (var i in this.variables)
    {
	var variable = this.variables[i];
	variable.constructor = Variable;
	variable.constructor(this.report, this.visualization, this);
    }
    // after variables are set
    this.getColumns = Binding.prototype.getColumns;
    this.columns = this.getColumns();
//alert(this.columns.toSource());
}
Binding.prototype.getWireTypes = function()
{
    var wireTypes = new Array();
//alert(this.dataTypes.toSource());

    for (var i in this.dataTypes)
    {
	var dataType = this.dataTypes[i];
	dataType.getWireType = DataType.prototype.getWireType;
	var wireType = dataType.getWireType();
	if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }
    return wireTypes;
}
Binding.prototype.getColumns = function()
{
    var columns = new Array();
//alert(this.report.toSource());
    this.getWireTypes = Binding.prototype.getWireTypes;
//alert(this.type.getWireTypes().toSource());

    var wireTypes = this.getWireTypes();
//alert(wireTypes.toSource());
//alert(this.report.toSource());
    for (var i in wireTypes)
        columns = columns.concat(this.report.getColumnsByWireType(wireTypes[i])); // add columns for each type
//alert(columns.toSource());
    return columns;
}
Binding.prototype.getVariablesFromControl = function()
{
//alert(this.toSource());
    var variables = new Array();

    for (var i in this.control.options)
    {
	var option = this.control.options[i];
//alert(option.toSource());
	if (option.selected)
	{
	    var variable = { };
	    variable.variable = { 'type': 'literal', 'value' : Number(option.value) };
	    variable.binding = this;
	    variable.bindingType = this.type;
	    variable.visualization = this.visualization;
	    variable.visType = this.visType;
	    variables.push(variable);
	}
    }
    return variables;
}
Binding.prototype.hasVariable = function(name)
{
    //alert(this.varia)
    var binding = this;
    var variables = this.variables.filter(function(variable) { return variable.variable.value == name; } );
    /*
    for (var i in this.variables)
    {
	var variable = this.variables[i];
	alert(variable.variable.value);
        //if (variable.bindingType == bindingType.type && this.variables[i].variable == value) return true;
    }
    */
//alert(variables.length);
    return (variables.length > 0);
}

function Variable(report, visualization, binding)
{
    this.report = report;
    this.visualization = visualization;
    this.binding = binding;
}

function DataType()
{

}
DataType.XSD_NS = 'http://www.w3.org/2001/XMLSchema#';
DataType.wireTypes = new Array();
DataType.wireTypes[DataType.XSD_NS + 'boolean'] = 'boolean';
DataType.wireTypes[DataType.XSD_NS + 'string'] = 'string';
DataType.wireTypes[DataType.XSD_NS + 'integer'] = 'number';
DataType.wireTypes[DataType.XSD_NS + 'decimal'] = 'number';
DataType.wireTypes[DataType.XSD_NS + 'float'] = 'number';
DataType.wireTypes[DataType.XSD_NS + 'double'] = 'number';
DataType.wireTypes[DataType.XSD_NS + 'date'] = 'date';
DataType.wireTypes[DataType.XSD_NS + 'dateTime'] = 'datetime';
DataType.wireTypes[DataType.XSD_NS + 'time'] = 'timeofday';
DataType.prototype.getWireType = function()
{
    return DataType.wireTypes[this.type.value];
}

function Option()
{

}

function BindingType(dataTypes)
{
    this.dataTypes = dataTypes;
    //alert(this.dataTypes.toSource());
}
BindingType.prototype.getWireTypes = function()
{
    var wireTypes = new Array();
    for (var i in this.dataTypes)
    {
	var dataType = this.dataTypes[i];
	dataType.getWireType = DataType.prototype.getWireType;
	var wireType = dataType.getWireType();
	if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }
    return wireTypes;
}

function VisualizationType(bindingTypes, dataTypes, optionTypes)
{
    this.bindingTypes = bindingTypes;
    this.dataTypes = dataTypes;
    this.optionTypes = optionTypes;
//alert(this.bindingTypes.toSource());
    for (var i in this.bindingTypes)
    {
	var bindingType = this.bindingTypes[i];
	var bindDataTypes = this.dataTypes.filter(function(dataType) { return dataType.bindingType.value == bindingType.type.value; } );
	bindingType.constructor = BindingType;
	bindingType.constructor(bindDataTypes);
	bindingType.visType = this;
	//alert(bindDataTypes.toSource());
    }
}

function Report(table, visualizations, bindings, variables, options, containers)
{
    //alert(Report.bindingTypes.results.bindings.toSource());
//alert(variables.toSource());
    this.data = new google.visualization.DataTable(table, 0.6);
    this.visualizations = visualizations.results.bindings;
    this.countColumns();
    // join and split the whole thing
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
//alert(visualization.toSource());
	var visBindings = bindings.results.bindings.filter(function(binding) { return binding.visualization.value == visualization.visualization.value; } );
	var visVariables = variables.results.bindings.filter(function(variable) { return variable.visualization.value == visualization.visualization.value; } );
//alert(visVariables.toSource());
	var visContainer = containers.filter(function(container) { return container.visType == visualization.type.value; } )[0];
	//var visOptions = options.results.bindings.filter(function(option) { return option.visualization.value == visualization.visualization.value; } );
	var visOptions = new Array();

//temp = new Visualization();
//alert(temp.baseClass);
	visualization.constructor = Visualization;
	// only pass arrays, not the whole SPARQL result
	visualization.constructor(this, visBindings, visVariables, visOptions, visContainer.element);
	//visualization.show = Visualization.prototype.show;
	//visualization.show();
//alert(visualization.variables.toSource());
    }
    //alert(this.visualizations.toSource());
    //this.bindings = bindings.results.bindings;
    //this.options = options.results.bindings;
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

Report.visualizationTypes = new Array();
Report.bindingTypes = new Array();
Report.dataTypes = new Array();
Report.optionTypes = new Array();

Report.shit = function(bindingTypes, dataTypes) // static types (classes)
{
    Report.bindingTypes = bindingTypes.results.bindings; //  QUIRK -- should not be necessary if bindings are saved with "order"!!!
    Report.dataTypes = dataTypes.results.bindings;
    //alert(this.bindingTypes.toSource());
}

Report.init = function(visualizationTypes, bindingTypes, dataTypes, optionTypes) // static types (classes)
{
    //alert(bindingTypes.toSource());
    Report.visualizationTypes = visualizationTypes.results.bindings;
    Report.bindingTypes = bindingTypes.results.bindings;
    Report.dataTypes = dataTypes.results.bindings;
    //Report.optionTypes = optionTypes.results.bindings;
    //alert(Report.dataTypes.toSource());

    for (var i in Report.visualizationTypes)
    {
	var visType = Report.visualizationTypes[i];
	var visBindingTypes = Report.bindingTypes.filter(function(bindingType) { return bindingType.visType.value == visType.type.value; } );
	var visDataTypes = Report.dataTypes.filter(function(dataType) { return dataType.visType.value == visType.type.value; } );
	var visOptionTypes = new Array();
	//alert(visDataTypes.toSource());

	visType.constructor = VisualizationType;
	// only pass arrays, not the whole SPARQL result
	visType.constructor(visBindingTypes, visDataTypes, visOptionTypes);
    }
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

    /*
    var bindings = this.getBindingsWithoutVariables();
    var missingVars = this.createVariables(bindings);
    this.variables = this.variables.results.bindings.concat(missingVars);
    */
}

Report.prototype.setToggleElements = function(elements)
{
    //alert(elements.toSource());
    // HIDE UNUSED ELEMENTS!!!
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
//alert(visualization.type.value);
	var element = elements.filter(function(element) { return element.visType == visualization.type.value; } )[0];
	visualization.toggleElement = element.element;
	//alert(visualization.toggleElement.toSource());
    }
}

Report.prototype.setFieldsetElements = function(elements)
{
    // HIDE UNUSED ELEMENTS!!!
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
	var element = elements.filter(function(element) { return element.visType == visualization.type.value; } )[0];
	visualization.fieldset = element.element;
	//alert(visualization.toggleElement.toSource());
    }

    //this.visTypeFieldsetElements = elements;
}

Report.prototype.setBindingControls = function(controls)
{
//alert(controls.toSource());
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
	for (var j in visualization.bindings)
	{
	    var binding = visualization.bindings[j];
	    //alert(binding.type.type.toSource());
	    var bindingControl = controls.filter(function(element) { return element.bindingType == binding.type.value; } )[0];
	    binding.control = bindingControl.element;
	    binding.control.visualization = visualization;
	    binding.control.binding = binding;
	    binding.control.onchange = function()
	    {
		this.binding.getVariablesFromControl = Binding.prototype.getVariablesFromControl;
		this.binding.variables = this.binding.getVariablesFromControl();
		//alert(this.binding.variables.length);
		this.visualization.getColumns = Visualization.prototype.getColumns;
		this.visualization.columns = this.visualization.getColumns();
		//alert(this.visualization.columns.toSource());
		this.visualization.show();
	    }
	    //report.toggleVisualization(http://code.google.com/apis/visualization/AreaChartArea chart, this.checked)
	    //alert(binding.control.toSource());
	}
    }
}
Report.prototype.showWithControls = function()
{
    //alert(this.visTypeToggleElements.toSource());
    
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
//alert(visualization.toSource());

//alert(toggleElement.toSource());

	visualization.hasSufficientColumns = Visualization.prototype.hasSufficientColumns;
	visualization.toggle = Visualization.prototype.toggle;

	if (visualization.hasSufficientColumns())
	{
	    //alert("sufficient" + this.visualizations[i].toSource());
	    visualization.fillControls = Visualization.prototype.fillControls;
	    visualization.fillControls();
	    visualization.show = Visualization.prototype.show;
	    visualization.show();
	    visualization.toggle(true);
	}
        else
	{
	    //alert("nope");
	    visualization.toggle(false);
	    visualization.toggleElement.disabled = true;
	}
    }
}

Report.prototype.show = function()
{
    for (var i in this.visualizations)
    {
	var visualization = this.visualizations[i];
	visualization.show = Visualization.prototype.show;
	visualization.show();
	//else alert("nope");
    }
}
Report.prototype.countColumns = function()
{
    //alert("count!!");
    this.typeColumns = { "string": [], "number": [], "date": [], "lat": [], "lng": [] };

    for (var i = 0; i < this.data.getNumberOfColumns(); i++)
    {
	if (this.data.getColumnType(i) == "string") this.typeColumns.string.push(i);
	if (this.data.getColumnType(i) == "date")
	{
	    this.typeColumns.string.push(i); // date columns also treated as strings
	    this.typeColumns.date.push(i);
	}
	if (this.data.getColumnType(i) == "number") // lat/lng columns
	{
	    this.typeColumns.number.push(i);
	    var range = this.data.getColumnRange(i);
	    if (range.min >= -90 && range.max <= 90) this.typeColumns.lat.push(i);
	    if (range.min >= -180 && range.max <= 180) this.typeColumns.lng.push(i);
	}
    }
}
Report.prototype.getColumnsByWireType = function(wireType)
{
//alert(wireType.toSource());
    return this.typeColumns[wireType];
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

Report.prototype.getBindingVariables = function(bindingTypeElement, binding)
{
//alert(binding.toSource());

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
//alert(this.toSource());
    var variables = new Array();
    for (var i in this.bindingTypeElements)
    {
	//var binding = objectByType(this.bindingTypeElements[i].bindingType, this.bindings);
	var element = this.bindingTypeElements[i];
	var binding = this.bindings.results.bindings.filter(function(el) { return el.type.value == element.bindingType; } )[0];

//alert(binding.toSource());
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