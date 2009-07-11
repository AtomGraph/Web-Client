var data = new google.visualization.DataTable(table, 0.6);
var stringColumns = new Array();
var numericColumns = new Array();
var dateColumns = new Array();

for (i = 0; i < data.getNumberOfColumns(); i++)
{
	if (data.getColumnType(i) == "string") stringColumns.push(i);
	if (data.getColumnType(i) == "number") numericColumns.push(i);
	if (data.getColumnType(i) == "date") dateColumns.push(i);
}
if (numericColumns.length >= 2)
{
	var view = new google.visualization.DataView(data);
	view.setColumns(numericColumns);
	var container = document.getElementById("scatter-chart");
	alert(document);
	var visualization = new google.visualization.ScatterChart(container);
	visualization.draw(view, { width: 800, height: 400 } );
}

function drawTable() {
  var table = new google.visualization.Table(document.getElementById('table'));
  table.draw(data, {showRowNumber: true});

  google.visualization.events.addListener(table, 'select', function() {
    var row = table.getSelection()[0].row;
    alert('You selected ' + data.getValue(row, 0));
  });
}

function drawScatter() {
  var view = new google.visualization.DataView(data);
  view.hideColumns([0, 1]);

  var table = new google.visualization.ScatterChart(document.getElementById('chart_div'));
  table.draw(view, { width: 800, height: 400 } );
}

function drawLine() {
  var view = new google.visualization.DataView(data);

  var table = new google.visualization.LineChart(document.getElementById('chart_div'));
  table.draw(view, { width: 800, height: 400 } );
}

function drawMap() {
  var view = new google.visualization.DataView(data);
  view.setColumns([3,4]);

  var table = new google.visualization.Map(document.getElementById('chart_map'));
  table.draw(view, { width: 800, height: 400 } );
}