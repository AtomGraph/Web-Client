google.setOnLoadCallback(init);
var data = new google.visualization.DataTable(table, 0.6);

function init()
{

	var stringColumns = new Array();
	var numericColumns = new Array();
	var dateColumns = new Array();
	var visualizations = new Array();

	for (i = 0; i < data.getNumberOfColumns(); i++)
	{
		if (data.getColumnType(i) == "string") stringColumns.push(i);
		if (data.getColumnType(i) == "number") numericColumns.push(i);
		if (data.getColumnType(i) == "date") dateColumns.push(i);
	}

	var table = new google.visualization.Table(document.getElementById('table'));
	visualizations.push(visualization);
	table.draw(data, {showRowNumber: true});
	
	if (numericColumns.length >= 2) // scatter
	{
		var view = new google.visualization.DataView(data);
		var columns = numericColumns;
		view.setColumns(columns);
		var container = document.getElementById("scatter-chart");
		var visualization = new google.visualization.ScatterChart(container);
		var options = new Array();
		options["titleX"] = data.getColumnLabel(columns[0]);
		options["titleY"] = data.getColumnLabel(columns[1]);
		visualizations.push(visualization);
		visualization.draw(view, options);
	}
	
	if (stringColumns.length >= 1 && numericColumns.length > 0) // line
	{
		var view = new google.visualization.DataView(data);
		var columns = new Array();
		columns[0] = stringColumns[0];
		// alert(columns.toSource());
		columns = columns.concat(numericColumns);
		//alert(numericColumns.toSource());
		view.setColumns(columns);
		var container = document.getElementById("scatter-chart");
		var visualization = new google.visualization.LineChart(container);
		var options = new Array();
		options["titleX"] = data.getColumnLabel(columns[0]);
		options["titleY"] = data.getColumnLabel(columns[1]);
		visualizations.push(visualization);
		visualization.draw(view, options);
	}

}

function drawTable()
{
  var table = new google.visualization.Table(document.getElementById('table'));
  table.draw(data, {showRowNumber: true});

  google.visualization.events.addListener(table, 'select', function() {
    var row = table.getSelection()[0].row;
    alert('You selected ' + data.getValue(row, 0));
  });
}

function drawScatter() {
  var view = new google.visualization.DataView(data);
  view.setColumns([1, 2]);

  var table = new google.visualization.ScatterChart(document.getElementById("scatter-chart"));
  table.draw(view, { width: 800, height: 400 } );
}

function drawLine() {
  var view = new google.visualization.DataView(data);

  var table = new google.visualization.LineChart(document.getElementById("line-chart"));
  table.draw(view, { width: 800, height: 400 } );
}

function drawMap() {
  var view = new google.visualization.DataView(data);
  view.setColumns([3,4]);

  var table = new google.visualization.Map(document.getElementById("map"));
  table.draw(view, { width: 800, height: 400 } );
}