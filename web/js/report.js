// =============================== Instances ===================================

var report = null;

//google.setOnLoadCallback(countColumns(data));
Report.prototype.uri = null;
Report.prototype.data = null;
Report.prototype.typeColumns = new Array();
Report.prototype.visualizations = new Array();
Report.prototype.bindings = new Array();
Report.prototype.variables = new Array();
Report.prototype.options = new Array();
Report.prototype.containers = new Array();

function Report(table, visualizations, bindings, variables, options, containers)
{
    this.data = new google.visualization.DataTable(table, 0.6);
    // count columns after data is set - but before visualizations are filtered
    this.countColumns();
    this.visualizations = visualizations.results.bindings;
    this.bindings = bindings.results.bindings;
    this.variables = variables.results.bindings;
    //this.options = options.results.bindings;
    this.containers = containers;
    // hide unused containers!!!
    for (var h = 0; h < this.containers.length; h++)
	this.containers[h].display = "none";
    // join and split the whole thing
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	var visBindings = this.bindings.filter(function(binding) { return binding.visualization.value == visualization.visualization.value; } );
	var visVariables = this.variables.filter(function(variable) { return variable.visualization.value == visualization.visualization.value; } );
	var visContainer = this.containers.filter(function(container) { return container.visType == visualization.type.value; } )[0];
	//var visOptions = this.options.filter(function(option) { return option.visualization.value == visualization.visualization.value; } );
	var visOptions = new Array();

	visualization.constructor = Visualization;
	// only pass arrays, not the whole SPARQL result
	visualization.constructor(this, visBindings, visVariables, visOptions, visContainer.element);
	visualization.init = Visualization.prototype.init;
	visualization.init();
    }
    // filter out visualizations that do not have sufficient columns!!!
    this.visualizations = this.getSufficientVisualizations(this.visualizations);
    //var unsufficient = this.getUnsufficientVisualizations(this.visualizations);
}
Report.setToggleElements = function(report, elements)
{
    for (var j = 0; j < Report.visualizationTypes.length; j++)
    {
	var visType = Report.visualizationTypes[j];
	var typeToggle = elements.filter(function(element) { return element.visType == visType.type.value; } )[0];
	visType.hasSufficientColumns = VisualizationType.prototype.hasSufficientColumns;
	var sufficient = visType.hasSufficientColumns(report);
	typeToggle.element.disabled = !sufficient;

	// REFACTOR FOLLOWING!!!
	var visualization = null;
	var temp = report.visualizations.filter(function(visualization) { return visualization.type.value == visType.type.value; } );
	if (temp.length > 0) visualization = temp[0];

	// add missing visualizations
	if (visualization == null)
	{
	    var visBindings = new Array();
	    for (var i = 0; i < visType.bindingTypes.length; i++)
	    {
		var bindingType = visType.bindingTypes[i];
		var binding = { }; // new Binding();
		binding.type = bindingType.type;
		visBindings.push(binding);
	    }
//alert(visBindings.toSource());
	    var visContainer = report.containers.filter(function(container) { return container.visType == visType.type.value; } )[0];
	    visualization = new Visualization(report, visBindings, [], [], visContainer.element);
	    //visualization.report = report;
	    visualization.type = visType.type;
//alert("null " + visualization.type.value);
	    visualization.init = Visualization.prototype.init;
	    visualization.init();
	    visualization.createVariables();
	    //visualization.toggle(false);
	    report.visualizations.push(visualization);
	    typeToggle.element.checked = false;
	}
	else typeToggle.element.checked = true;

	visualization.toggleElement = typeToggle.element;
	//typeToggle.element.checked = true; // -- set in ReportCreateView.xsl
	typeToggle.element.visualization = visualization;
	typeToggle.element.onchange = function()
	{
	    //this.visualization.toggle = Visualization.prototype.toggle;
	    this.visualization.toggle(this.checked);
	    //this.visualization.show();
	}
    }
}
Report.prototype.setFieldsetElements = function(elements)
{
    // HIDE UNUSED ELEMENTS!!!
    for (var j = 0; j < elements.length; j++)
	elements[j].element.style.display = "none";

    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	var element = elements.filter(function(element) { return element.visType == visualization.type.value; } )[0];
	visualization.fieldset = element.element;
    }
}
Report.prototype.setControls = function(controls)
{
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	visualization.setControls = Visualization.prototype.setControls;
	visualization.setControls(controls);
    }
}
Report.prototype.getSufficientVisualizations = function(visualizations)
{
    return visualizations.filter(
	function(visualization)
	{
	    visualization.hasSufficientColumns = Visualization.prototype.hasSufficientColumns;
	    return visualization.hasSufficientColumns();
	});
}
Report.prototype.getUnsufficientVisualizations = function(visualizations)
{
    return visualizations.filter(
	function(visualization)
	{
	    visualization.hasSufficientColumns = Visualization.prototype.hasSufficientColumns;
	    return !visualization.hasSufficientColumns();
	});
}
Report.prototype.showWithControls = function()
{
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	visualization.fillControls = Visualization.prototype.fillControls;
	visualization.fillControls();
	visualization.show = Visualization.prototype.show;
	visualization.show();
	visualization.toggle = Visualization.prototype.toggle;
	visualization.toggle(visualization.toggleElement.checked);
    }
}
Report.prototype.show = function()
{
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	visualization.show = Visualization.prototype.show;
	visualization.show();
    }
}
// creates Variables for first request to ReportCreateView, when none are submitted from form controls
Report.prototype.createVariables = function()
{
    var variables = new Array();

    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	visualization.createVariables = Visualization.prototype.createVariables;
	visualization.createVariables();
    }
    return variables;
}
Report.prototype.countColumns = function()
{
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
    return this.typeColumns[wireType];
}

function Visualization(report, bindings, variables, options, container)
{
    this.report = report;
    this.variables = variables;
    this.bindings = bindings;
    this.container = container;
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	var bindingVariables = this.variables.filter(function(variable) { return variable.binding.value == binding.binding.value; } );
	binding.constructor = Binding;
	binding.constructor(this.report, this, bindingVariables);
    }
    //this.getColumns = Visualization.prototype.getColumns;
    //this.columns = this.getColumns();
//alert(this.type.toSource());
//alert(this.type.value + "\n\n" + this.columns.toSource());
    //this.init = Visualization.prototype.init;
    //this.init();
}
Visualization.prototype.getColumns = function()
{
//alert(this.type.value + "\n\n" + this.bindings.toSource());

    var orderColumns = new Array();
    var restColumns = new Array();
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	for (var j = 0; j < binding.variables.length; j++)
	{
	    var variable = binding.variables[j];
	    // QUIRK -- why order is string???
	    if ("order" in binding.bindingType) orderColumns[parseInt(binding.bindingType.order.value)] = parseInt(variable.variable.value);
	    else restColumns = restColumns.concat(parseInt(variable.variable.value));
	}
    }
    var columns = orderColumns.concat(restColumns);
    return columns;
}
Visualization.prototype.hasSufficientColumns = function()
{
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
        if ("cardinality" in binding.bindingType && binding.bindingType.cardinality.value > binding.columns.length) return false;
        if ("minCardinality" in binding.bindingType && binding.bindingType.minCardinality.value > binding.columns.length) return false;
	// maxCardinality???
    }
    return true;
}
Visualization.prototype.createVariables = function()
{
    for (var j = 0; j < this.bindings.length; j++)
    {
	var binding = this.bindings[j];
	binding.createVariables = Binding.prototype.createVariables;
	binding.createVariables();
    }
    //visualization.getColumns = Visualization.prototype.getColumns;
    this.columns = this.getColumns();
}
Visualization.prototype.show = function()
{
//alert(this.type.value + "\n\n" + this.columns.toSource());

    this.container.style.display = "block";
    this.view = new google.visualization.DataView(this.report.data);
    if (this.type.value.indexOf("Table") == -1) this.view.setColumns(this.columns); // all columns for Table
    var optArray = { }; // this.options
    /*
    for (var j = 0; j < visOptions.length; j++)
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
    this.googleVis.draw(this.view, optArray);
}
Visualization.prototype.toggle = function(show)
{
//alert("show: " + true);
    if (show)
    {
//alert(this.container.toSource());
	this.container.style.display = "block";
	this.fieldset.style.display = "block";
	this.toggleElement.checked = true;
    }
    else
    {
	this.container.style.display = "none";
	this.fieldset.style.display = "none";
	this.toggleElement.checked = false;
    }
}
Visualization.prototype.setControls = function(controls)
{
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	var bindingControl = controls.filter(function(element) { return element.bindingType == binding.type.value; } )[0];
	binding.setControl = Binding.prototype.setControl;
	binding.setControl(bindingControl.element);
    }
}
Visualization.prototype.fillControls = function()
{
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	binding.fillControls = Binding.prototype.fillControls;
	binding.fillControls();
    }
}
Visualization.prototype.init = function()
{
    this.getColumns = Visualization.prototype.getColumns;
    this.columns = this.getColumns();
//alert(this.type.value + "\n\n" + this.columns.toSource());
//alert(this.type.value + "\n\n" + this.variables.toSource());

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
//alert(bindingType.toSource());
//alert(this.type.value);
//alert(visualization.type.value);
    var binding = this;
    // hack for visualization ontology changes
    //if (this.type.value == "http://code.google.com/apis/visualization/MapAddressBinding") this.type.value = "http://code.google.com/apis/visualization/MapLabelBinding";
    this.report = report;
    this.visualization = visualization;
    this.bindingType = Report.bindingTypes.filter(function(bindingType) { return bindingType.type.value == binding.type.value; } )[0];
    if ("order" in this.bindingType) this.order = this.bindingType.order;
    this.variables = variables;

    for (var i = 0; i < this.variables.length; i++)
    {
	var variable = this.variables[i];
	variable.constructor = Variable;
	variable.constructor(this.report, this.visualization, this);
    }
    // after variables are set
    this.getColumns = Binding.prototype.getColumns;
    this.columns = this.getColumns();
}
Binding.prototype.getWireTypes = function()
{
    var wireTypes = new Array();

    for (var i = 0; i < this.bindingType.dataTypes.length; i++)
    {
	var dataType = this.bindingType.dataTypes[i];
	dataType.getWireType = DataType.prototype.getWireType;
	var wireType = dataType.getWireType();
	if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }
    
    return wireTypes;
}
Binding.prototype.getColumns = function()
{
    // works directly on data, not binding.variables
    var columns = new Array();
    this.getWireTypes = Binding.prototype.getWireTypes;

    var wireTypes = this.getWireTypes();
    for (var i = 0; i < wireTypes.length; i++)
        columns = columns.concat(this.report.getColumnsByWireType(wireTypes[i])); // add columns for each type
    
    return columns;
}
Binding.prototype.hasVariable = function(name)
{
    var variables = this.variables.filter(function(variable) { return variable.variable.value == name; } );
    return (variables.length > 0);
}
Binding.prototype.createVariables = function()
{
    // create a Variable for the first of this binding's data columns
    //for (var i = 0; i < this.columns.length; i++)
    {
	var column = this.columns[0];
	var variable = new Variable(this.report, this.visualization, this);
	variable.variable = { 'type' : 'typed-literal', 'value' : column }; // 'datatype
	variable.binding = this;
	variable.bindingType = this.type;
	variable.visualization = this.visualization;
	variable.visType = this.visType;
	this.variables.push(variable);
    }
}
Binding.prototype.setControl = function(control)
{
    this.control = control;
    this.control.visualization = this.visualization;
    this.control.binding = this;
    this.control.onchange = function()
    {
	this.binding.control.getSelectedOptions = getSelectedOptions;
	var selectedOptions = this.binding.control.getSelectedOptions();
	// do not allow deselecting too much/all
	if ("cardinality" in this.binding.bindingType && this.binding.bindingType.cardinality.value > selectedOptions.length) return false;
	if ("minCardinality" in this.binding.bindingType && this.binding.bindingType.minCardinality.value > selectedOptions.length) return false;

	this.binding.getVariablesFromControl = Binding.prototype.getVariablesFromControl;
	this.binding.variables = this.binding.getVariablesFromControl();
	this.visualization.getColumns = Visualization.prototype.getColumns;
	this.visualization.columns = this.visualization.getColumns();
	this.visualization.show();
	return true;
    }
}
Binding.prototype.fillControls = function()
{
    if (!(("cardinality" in this.bindingType && this.bindingType.cardinality.value == 1) ||
	("maxCardinality" in this.bindingType && this.bindingType.maxCardinality.value == 1)))
	    this.control.multiple = "multiple";

    for (var i = 0; i < this.columns.length; i++)
    {
	var option = document.createElement("option");
	option.appendChild(document.createTextNode(this.report.data.getColumnLabel(this.columns[i])));
	option.setAttribute("value", this.columns[i]);

	this.hasVariable = Binding.prototype.hasVariable;
	if (this.hasVariable(this.columns[i])) option.setAttribute("selected", "selected");
	this.control.appendChild(option);
    }
}
Binding.prototype.getVariablesFromControl = function()
{
    var variables = new Array();

    for (var i = 0; i < this.control.options.length; i++)
    {
	var option = this.control.options[i];
	if (option.selected)
	{
	    var variable = new Variable(this.report, this.visualization, this);
	    variable.variable = { 'type': 'typed-literal', 'value' : Number(option.value) };
	    variable.binding = this;
	    variable.bindingType = this.type;
	    variable.visualization = this.visualization;
	    variable.visType = this.visType;
	    variables.push(variable);
	}
    }
    return variables;
}

function Variable(report, visualization, binding)
{
    this.report = report;
    this.visualization = visualization;
    this.binding = binding;
}

function Option()
{

}

// =========================== Types/classes ===================================

Report.visualizationTypes = new Array();
Report.bindingTypes = new Array();
Report.dataTypes = new Array();
Report.optionTypes = new Array();

// ReportType() ???
Report.init = function(visualizationTypes, bindingTypes, dataTypes, optionTypes) // static types (classes)
{
    Report.visualizationTypes = visualizationTypes.results.bindings;
    Report.bindingTypes = bindingTypes.results.bindings;
    Report.dataTypes = dataTypes.results.bindings;
    //Report.optionTypes = optionTypes.results.bindings;

    for (var i = 0; i < Report.visualizationTypes.length; i++)
    {
	var visType = Report.visualizationTypes[i];
	var visBindingTypes = Report.bindingTypes.filter(function(bindingType) { return bindingType.visType.value == visType.type.value; } );
	var visDataTypes = Report.dataTypes.filter(function(dataType) { return dataType.visType.value == visType.type.value; } );
	var visOptionTypes = new Array();

	visType.constructor = VisualizationType;
	// only pass arrays, not the whole SPARQL result
	visType.constructor(visBindingTypes, visDataTypes, visOptionTypes);
    }
}

function VisualizationType(bindingTypes, dataTypes, optionTypes)
{
    this.bindingTypes = bindingTypes;
    this.dataTypes = dataTypes;
    this.optionTypes = optionTypes;
    for (var i = 0; i < this.bindingTypes.length; i++)
    {
	var bindingType = this.bindingTypes[i];
	var bindDataTypes = this.dataTypes.filter(function(dataType) { return dataType.bindingType.value == bindingType.type.value; } );
	bindingType.constructor = BindingType;
	bindingType.constructor(bindDataTypes);
	bindingType.visType = this;
    }
}
VisualizationType.prototype.hasSufficientColumns = function(report)
{
    for (var i = 0; i < this.bindingTypes.length; i++)
    {
	var bindingType = this.bindingTypes[i];
	bindingType.hasSufficientColumns = BindingType.prototype.hasSufficientColumns;
	if (!bindingType.hasSufficientColumns(report)) return false;
    }
    return true;
}

function BindingType(dataTypes)
{
    this.dataTypes = dataTypes;
    //this.getColumns = BindingType.prototype.getColumns;
    //this.columns = this.getColumns();
}
BindingType.prototype.getWireTypes = function()
{
    var wireTypes = new Array();
    for (var i = 0; i < this.dataTypes.length; i++)
    {
	var dataType = this.dataTypes[i];
	dataType.getWireType = DataType.prototype.getWireType;
	var wireType = dataType.getWireType();
	if (wireTypes.indexOf(wireType) == -1) wireTypes.push(wireType); // no duplicates
    }
    return wireTypes;
}
BindingType.prototype.getColumns = function(report)
{
    // no this.report at this point!!!
    var columns = new Array();
    this.getWireTypes = BindingType.prototype.getWireTypes;
    //this.report.getColumnsByWireType = Report.prototype.getColumnsByWireType;

    var wireTypes = this.getWireTypes();

    for (var i = 0; i < wireTypes.length; i++)
        columns = columns.concat(report.getColumnsByWireType(wireTypes[i])); // add columns for each type

    return columns;
}
BindingType.prototype.hasSufficientColumns = function(report)
{
    this.getColumns = BindingType.prototype.getColumns;
    var columns = this.getColumns(report);

    if ("cardinality" in this && this.cardinality.value > columns.length) return false;
    if ("minCardinality" in this && this.minCardinality.value > columns.length) return false;
    // maxCardinality???

    return true;
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

// =========================== Helpers ====================================

function getSelectedOptions()
{
    var selected = new Array();
    for (var i = 0; i < this.options.length; i++)
	if (this.options[i].selected) selected.push(this.options[i].value);
    return selected;
}

// ============================ Internet Explorer fixes ========================

if (!Array.prototype.filter)
{
  Array.prototype.filter = function(fun /*, thisp */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (typeof fun !== "function")
      throw new TypeError();

    var res = [];
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in t)
      {
        var val = t[i]; // in case fun mutates this
        if (fun.call(thisp, val, i, t))
          res.push(val);
      }
    }

    return res;
  };
}

if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(searchElement /*, fromIndex */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (len === 0)
      return -1;

    var n = 0;
    if (arguments.length > 0)
    {
      n = Number(arguments[1]);
      if (n !== n)
        n = 0;
      else if (n !== 0 && n !== (1 / 0) && n !== -(1 / 0))
        n = (n > 0 || -1) * Math.floor(Math.abs(n));
    }

    if (n >= len)
      return -1;

    var k = n >= 0
          ? n
          : Math.max(len - Math.abs(n), 0);

    for (; k < len; k++)
    {
      if (k in t && t[k] === searchElement)
        return k;
    }
    return -1;
  };
}