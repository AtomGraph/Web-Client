var report = null;

//google.setOnLoadCallback(countColumns(data));
Report.prototype.uri = null;
Report.prototype.data = null;
Report.prototype.typeColumns = new Array();
Report.prototype.visTypeToggleElements = new Array();
Report.prototype.visTypeFieldsetElements = new Array();
Report.prototype.bindingTypeElements = new Array();
Report.prototype.visualizations = new Array();
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
function Report(table, visualizations, bindings, variables, options, containers)
{
    this.data = new google.visualization.DataTable(table, 0.6);
    this.visualizations = visualizations.results.bindings;
    this.countColumns();
    // join and split the whole thing
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	var visBindings = bindings.results.bindings.filter(function(binding) { return binding.visualization.value == visualization.visualization.value; } );
	var visVariables = variables.results.bindings.filter(function(variable) { return variable.visualization.value == visualization.visualization.value; } );
	var visContainer = containers.filter(function(container) { return container.visType == visualization.type.value; } )[0];
	//var visOptions = options.results.bindings.filter(function(option) { return option.visualization.value == visualization.visualization.value; } );
	var visOptions = new Array();

	visualization.constructor = Visualization;
	// only pass arrays, not the whole SPARQL result
	visualization.constructor(this, visBindings, visVariables, visOptions, visContainer.element);
    }
    //alert(this.visualizations.toSource());
    //this.bindings = bindings.results.bindings;
    //this.options = options.results.bindings;
    //this.containers = containers;
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

Report.prototype.setToggleElements = function(elements)
{
    //alert(elements.toSource());
    // DISABLE UNUSED ELEMENTS!!!
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
//alert(visualization.type.value);
	var element = elements.filter(function(element) { return element.visType == visualization.type.value; } )[0];
	visualization.toggleElement = element.element;
	visualization.toggleElement.visualization = this;
	visualization.toggleElement.onchange = function()
	{
	    this.visualization.toggle = Visualization.prototype.toggle;
	    this.visualization.toggle(this.checked);
	}
	//alert(visualization.toggleElement.toSource());
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

Report.prototype.setBindingControls = function(controls)
{
    for (var i = 0; i < this.visualizations.length; i++)
    {
	var visualization = this.visualizations[i];
	for (var j = 0; j < visualization.bindings.length; j++)
	{
	    var binding = visualization.bindings[j];
	    var bindingControl = controls.filter(function(element) { return element.bindingType == binding.type.value; } )[0];
	    binding.control = bindingControl.element;
	    binding.control.visualization = visualization;
	    binding.control.binding = binding;
	    binding.control.onchange = function()
	    {
		this.binding.getVariablesFromControl = Binding.prototype.getVariablesFromControl;
		this.binding.variables = this.binding.getVariablesFromControl();
		this.visualization.getColumns = Visualization.prototype.getColumns;
		this.visualization.columns = this.visualization.getColumns();
		this.visualization.show();
	    }
	}
    }
}
Report.prototype.showWithControls = function()
{
    for (var i = 0; i < this.visualizations.length; i++)
    {
	/*
	visualization.hasSufficientColumns = Visualization.prototype.hasSufficientColumns;
	visualization.toggle = Visualization.prototype.toggle;

	if (visualization.hasSufficientColumns())
	*/

	var visualization = this.visualizations[i];
	visualization.fillControls = Visualization.prototype.fillControls;
	visualization.fillControls();
	visualization.show = Visualization.prototype.show;
	visualization.show();
	visualization.toggle = Visualization.prototype.toggle;
	visualization.toggle(true);

	/*
	}
        else
	{
	    //alert("nope");
	    visualization.toggle(false);
	    visualization.toggleElement.disabled = true;
	}
	*/
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
    var visualization = this;
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
    this.getColumns = Visualization.prototype.getColumns;
    this.columns = this.getColumns();
    this.init = Visualization.prototype.init;
    this.init();
}
Visualization.prototype.getColumns = function()
{
    var orderColumns = new Array();
    var restColumns = new Array();
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	for (var j = 0; j < binding.variables.length; j++)
	{
	    var variable = binding.variables[j];
	    // QUIRK -- why order is string???
	    if ("order" in binding) orderColumns[parseInt(binding.order.value)] = parseInt(variable.variable.value);
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
        if ("cardinality" in binding && binding.cardinality.value > binding.columns.length) return false;
        if ("minCardinality" in binding && binding.minCardinality.value > binding.columns.length) return false;
	// maxCardinality???
    }
    return true;
}
Visualization.prototype.show = function()
{
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
    if (show)
    {
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
Visualization.prototype.fillControls = function()
{
    for (var i = 0; i < this.bindings.length; i++)
    {
	var binding = this.bindings[i];
	binding.fillControls = Binding.prototype.fillControls;
	binding.fillControls(); //???

  }
}
Binding.prototype.fillControls = function()
{
    if (!(("cardinality" in this && this.cardinality.value == 1) ||
	    ("maxCardinality" in this && this.maxCardinality.value == 1)))
	this.control.multiple = "multiple";

    for (var j = 0; j < this.columns.length; j++)
    {
	var option = document.createElement("option");
	option.appendChild(document.createTextNode(this.report.data.getColumnLabel(this.columns[j])));
	option.setAttribute("value", this.columns[j]);

	this.hasVariable = Binding.prototype.hasVariable;
	if (this.hasVariable(this.columns[j]))
	    option.setAttribute("selected", "selected");
	this.control.appendChild(option);
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
    // hack for visualization ontology changes
    if (this.type.value == "http://code.google.com/apis/visualization/MapAddressBinding") this.type.value = "http://code.google.com/apis/visualization/MapLabelBinding";
    this.report = report;
    this.visualization = visualization;
    var bindingType = Report.bindingTypes.filter(function(bindingType) { return bindingType.type.value == binding.type.value; } )[0];
    // QUIRK -- bindingType should not be used, if its properties are already saved with binding
    this.type = bindingType.type;
    this.label = bindingType.label;
    this.dataTypes = bindingType.dataTypes;
    // QUIRK -- only "order" belongs here!!! (belongs to binding and not bindingType)
    if ("order" in bindingType) this.order = bindingType.order;
    if ("cardinality" in bindingType) this.cardinality = bindingType.cardinality;
    if ("minCardinality" in bindingType) this.minCardinality = bindingType.minCardinality;
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

    for (var i = 0; i < this.dataTypes.length; i++)
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
    this.getWireTypes = Binding.prototype.getWireTypes;

    var wireTypes = this.getWireTypes();
    for (var i = 0; i < wireTypes.length; i++)
        columns = columns.concat(this.report.getColumnsByWireType(wireTypes[i])); // add columns for each type
    
    return columns;
}
Binding.prototype.getVariablesFromControl = function()
{
    var variables = new Array();

    for (var i = 0; i < this.control.options.length; i++)
    {
	var option = this.control.options[i];
	if (option.selected)
	{
	    var variable = { };
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
Binding.prototype.hasVariable = function(name)
{
    var variables = this.variables.filter(function(variable) { return variable.variable.value == name; } );
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

// =========================== NOT USED? ====================================

Report.prototype.createVariables = function(bindings)
{
    var variables = new Array();

    for (var i = 0; i < bindings.length; i++)
    {
	var bindingType = Report.bindingTypes.results.bindings.filter(function(el) { return el.type.value == bindings[i].type.value; } )[0];
        var bindingColumns = this.columnsByBindingType(bindingType);
        for (var j = 0; j < bindingColumns.length; j++)
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
    return variables;
}

Report.prototype.getBindingsWithoutVariables = function()
{
    var bindings = new Array();
    var report = this;

    for (var i = 0; i < this.bindings.results.bindings.length; i++)
    {
	var variables = this.variables.results.bindings.filter(function(el) { return el.binding.value == report.bindings.results.bindings[i].binding.value; } );
	if (variables.length == 0) bindings.push(this.bindings.results.bindings[i]);
    }
    return bindings;
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