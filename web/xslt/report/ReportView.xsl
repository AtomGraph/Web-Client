<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY dom "http://www.itu.dk/people/martynas/Thesis/whatsup.owl#">
	<!ENTITY sys "http://www.xml.lt/system-ont.owl#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
exclude-result-prefixes="owl rdf rdfs xsd sparql">

	<xsl:import href="../sparql2google-wire.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:param name="query-result"/>
	<xsl:param name="chart-result"/>
	<xsl:param name="query-string" select="''"/>
	<xsl:param name="x-variable-default"/>
	<xsl:param name="y-variable-default"/>
	<xsl:param name="label-variable-default" select="'label'"/>
	<xsl:param name="chart-url"/>

	<xsl:template name="title">
		Query
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>
			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<textarea cols="80" rows="20" name="query-string">
						<xsl:value-of select="$query-string"/>
					</textarea>
					<br/>
					<!-- <input type="hidden" name="action" value="query"/> -->
					<button type="submit">Query</button>
				</p>
			</form>

			<xsl:if test="$query-result">
				<!--
				<h3>Results (<xsl:value-of select="count(document('arg://results')//sparql:result)"/>)</h3>
				<xsl:if test="document('arg://results')//sparql:result">
					<table>
						<thead>
							<tr>
								<xsl:apply-templates select="document('arg://results')//sparql:variable" mode="table-header"/>
							</tr>
						</thead>
						<tbody>
							<xsl:apply-templates select="document('arg://results')//sparql:result" mode="results-table-body"/>
						</tbody>
					</table>
				</xsl:if>
				<h3>Chart</h3>
				<form action="" method="get" accept-charset="UTF-8" >
					<p>
						<label for="x-variable">X axis</label>
						<select name="x-variable" id="x-variable">
							<xsl:variable name="selected">
								<xsl:choose>
									<xsl:when test="$chart-result">
										<xsl:value-of select="document('arg://chart-form')//XVariable"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$x-variable-default"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:apply-templates select="document('arg://results')/sparql:sparql/sparql:head/sparql:variable" mode="numeric-option">
								<xsl:with-param name="selected" select="$selected"/>
							</xsl:apply-templates>
						</select>
						<br/>
						<label for="y-variable">Y axis</label>
						<select name="y-variable" id="y-variable">
							<xsl:variable name="selected">
								<xsl:choose>
									<xsl:when test="$chart-result">
										<xsl:value-of select="document('arg://chart-form')//YVariable"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$y-variable-default"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:apply-templates select="document('arg://results')/sparql:sparql/sparql:head/sparql:variable" mode="numeric-option">
								<xsl:with-param name="selected" select="$selected"/>
							</xsl:apply-templates>
						</select>
						<br/>
						<label for="label-variable">Label</label>
						<select name="label-variable" id="label-variable">
							<xsl:variable name="selected">
								<xsl:choose>
									<xsl:when test="$chart-result">
										<xsl:value-of select="document('arg://chart-form')//LabelVariable"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$label-variable-default"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:apply-templates select="document('arg://results')/sparql:sparql/sparql:head/sparql:variable" mode="string-option">
								<xsl:with-param name="selected" select="$selected"/>
							</xsl:apply-templates>
						</select>
						<br/>
						<label for="chart-type">Chart type</label>
						<select name="type" id="chart-type">
							<option value="s">Scatter plot</option>	
						</select>
						<br/>
						<label for="chart-title">Chart title</label>
						<input type="text" name="title">
							<xsl:if test="$chart-result">
								<xsl:attribute name="value"><xsl:value-of select="document('arg://chart-form')//Title"/></xsl:attribute>
							</xsl:if>
						</input>
						<br/>
						<button type="submit">Set</button>
						<input type="hidden" name="query-string" value="{$query-string}"/>
					</p>
				</form>
				<p>
					<img src="{$chart-url}" alt="Chart"/>
				</p>
				-->
				<p>
				    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
					<script type="text/javascript">
google.load('visualization', '1',  {'packages': ["table", "scatterchart", "linechart",  "map"]});

function drawTable() {
  var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
  var data = new google.visualization.DataTable(table, 0.6);

  var table = new google.visualization.Table(document.getElementById('table_div'));
  table.draw(data, {showRowNumber: true});

  google.visualization.events.addListener(table, 'select', function() {
    var row = table.getSelection()[0].row;
    alert('You selected ' + data.getValue(row, 0));
  });
}

function drawScatter() {
  var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
  var data = new google.visualization.DataTable(table, 0.6);
  var view = new google.visualization.DataView(data);
  view.hideColumns([0, 1]);

  var table = new google.visualization.ScatterChart(document.getElementById('chart_div'));
  table.draw(view, { width: 800, height: 400 } );
}

function drawLine() {
  var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
  var data = new google.visualization.DataTable(table, 0.6);
  var view = new google.visualization.DataView(data);

  var table = new google.visualization.LineChart(document.getElementById('chart_div'));
  table.draw(view, { width: 800, height: 400 } );
}

function drawMap() {
  var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
  var data = new google.visualization.DataTable(table, 0.6);
  var view = new google.visualization.DataView(data);
  view.setColumns([3,4]);

  var table = new google.visualization.Map(document.getElementById('chart_map'));
  table.draw(view, { width: 800, height: 400 } );
}
					</script>
<script src="http://maps.google.com/maps?file=api&amp;v=2" type="text/javascript"></script>

					<button onclick="drawTable();">table!</button>
					<div id="table_div"></div>
					<button onclick="drawScatter();">scatter!</button>
					<button onclick="drawLine();">line!</button>
					<div id="chart_div"></div>
					<button onclick="drawMap();">map!</button>
					<div id="chart_map" style="width: 800px; height: 400px;"></div>
				</p>
			</xsl:if>
		</div>
	</xsl:template>

	<xsl:template match="sparql:variable" mode="table-header">
		<td>
			<xsl:value-of select="@name"/>
		</td>
	</xsl:template>

	<xsl:template match="sparql:variable[//sparql:binding[@name = current()/@name]/sparql:literal[string(number(.)) != 'NaN']]" mode="numeric-option">
		<xsl:param name="selected"/>
		<option value="{@name}">
			<xsl:if test="@name = $selected">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="@name"/>
		</option>
	</xsl:template>

	<xsl:template match="sparql:variable[//sparql:binding[@name = current()/@name]/sparql:literal[string(number(.)) = 'NaN']]" mode="string-option">
		<xsl:param name="selected"/>
		<option value="{@name}">
			<xsl:if test="@name = $selected">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="@name"/>
		</option>
	</xsl:template>

	<xsl:template match="sparql:result" mode="results-table-body">
		<tr>
			<xsl:variable name="current" select="."/>
			<xsl:for-each select="document('arg://results')//sparql:variable">
				<xsl:variable name="name" select="@name"/>
				<xsl:choose>
					<xsl:when test="$current/sparql:binding[@name=$name]">
						<!-- apply template for the correct value type (bnode, uri, literal) -->
						<xsl:apply-templates select="$current/sparql:binding[@name=$name]"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- no binding available for this variable in this solution -->
					  </xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template match="sparql:binding">
		<td>
			<xsl:apply-templates/>
		</td>
	</xsl:template>

	<xsl:template match="sparql:literal">
		&quot;<xsl:value-of select="text()"/>&quot;
	</xsl:template>

	<xsl:template match="sparql:uri">
		&lt;<a href="{text()}">
				<xsl:value-of select="text()"/>
			</a>&gt;
	</xsl:template>

	<xsl:template match="sparql:bnode">
		_:<xsl:value-of select="text()"/>
	</xsl:template>

</xsl:stylesheet>