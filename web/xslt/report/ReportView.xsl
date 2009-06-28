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
google.load('visualization', '1',  {'packages': ["table", "scatterchart"]});

function drawTable() {
  var table = { cols: [ { id: 'd1e3', label: 'company', type: 'string' } , { id: 'd1e4', label: 'label', type: 'string' } , { id: 'd1e5', label: 'employees', type: 'number' } , { id: 'd1e6', label: 'revenue', type: 'number' } ], rows: [ { c: [ { v: 'http://dbpedia.org/resource/Heraeus' } , { v: 'Heraeus' } , { v: 11275 } , { v: 12080000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Schneider_Electric' } , { v: 'Schneider Electric' } , { v: 120 } , { v: 17300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Philips' } , { v: 'Philips' } , { v: 125500 } , { v: 26976000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Soci%C3%A9t%C3%A9_G%C3%A9n%C3%A9rale' } , { v: 'Société Générale' } , { v: 130100 } , { v: 21923000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Grupo_Santander' } , { v: 'Grupo Santander' } , { v: 131153 } , { v: 27068000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Delhaize_Group' } , { v: 'Delhaize Group' } , { v: 142500 } , { v: 19200000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Electrabel' } , { v: 'Electrabel' } , { v: 15794 } , { v: 12218000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/TNT_N.V.' } , { v: 'TNT N.V.' } , { v: 161000 } , { v: 11000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Unilever' } , { v: 'Unilever' } , { v: 174 } , { v: 40187000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Mitsubishi_Fuso_Truck_and_Bus_Corporation' } , { v: 'Mitsubishi Fuso Truck and Bus Corporation' } , { v: 18200 } , { v: 30368000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Auchan' } , { v: 'Auchan' } , { v: 186000 } , { v: 36710000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/France_T%C3%A9l%C3%A9com' } , { v: 'France Télécom' } , { v: 187331 } , { v: 52959000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/ThyssenKrupp' } , { v: 'ThyssenKrupp' } , { v: 195 } , { v: 51700000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Fiat' } , { v: 'Fiat' } , { v: 200701 } , { v: 58529000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Mapfre' } , { v: 'Mapfre' } , { v: 21500 } , { v: 13600000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Gruppo_Riva' } , { v: 'Gruppo Riva' } , { v: 24684 } , { v: 10100000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Telef%C3%B3nica' } , { v: 'Telefónica' } , { v: 248487 } , { v: 56441000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Edeka' } , { v: 'Edeka' } , { v: 250000 } , { v: 38000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Bayer_Schering_Pharma' } , { v: 'Bayer Schering Pharma' } , { v: 26000 } , { v: 10267000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Adidas' } , { v: 'Adidas' } , { v: 27000 } , { v: 10299000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Veolia_Environnement' } , { v: 'Veolia Environnement' } , { v: 300000 } , { v: 32600000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/AGF_%28company%29' } , { v: 'AGF (company)' } , { v: 32000 } , { v: 17300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Repsol_YPF' } , { v: 'Repsol YPF' } , { v: 36 } , { v: 55923000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Munich_Re' } , { v: 'Munich Re' } , { v: 37210 } , { v: 37400000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Astaldi' } , { v: 'Astaldi' } , { v: 4709 } , { v: 19300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/SAP_AG' } , { v: 'SAP AG' } , { v: 51447 } , { v: 10250000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Henkel' } , { v: 'Henkel' } , { v: 52 } , { v: 13070000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Deutsche_Post' } , { v: 'Deutsche Post' } , { v: 520112 } , { v: 63500000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Strabag' } , { v: 'Strabag' } , { v: 52791 } , { v: 10400000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Safran' } , { v: 'Safran' } , { v: 57000 } , { v: 12000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Deutsche_Bank' } , { v: 'Deutsche Bank' } , { v: 68849 } , { v: 67706000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Enel' } , { v: 'Enel' } , { v: 73500 } , { v: 42700000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Arcelor' } , { v: 'Arcelor' } , { v: 94000 } , { v: 32611000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/BASF' } , { v: 'BASF' } , { v: 95200 } , { v: 57950000000 } ] } ] }   ;
  var data = new google.visualization.DataTable(table, 0.6);

  var table = new google.visualization.Table(document.getElementById('table_div'));
  table.draw(data, {showRowNumber: true});

  google.visualization.events.addListener(table, 'select', function() {
    var row = table.getSelection()[0].row;
    alert('You selected ' + data.getValue(row, 0));
  });
}

function drawChart() {
  var table = { cols: [ { id: 'd1e3', label: 'company', type: 'string' } , { id: 'd1e4', label: 'label', type: 'string' } , { id: 'd1e5', label: 'employees', type: 'number' } , { id: 'd1e6', label: 'revenue', type: 'number' } ], rows: [ { c: [ { v: 'http://dbpedia.org/resource/Heraeus' } , { v: 'Heraeus' } , { v: 11275 } , { v: 12080000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Schneider_Electric' } , { v: 'Schneider Electric' } , { v: 120 } , { v: 17300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Philips' } , { v: 'Philips' } , { v: 125500 } , { v: 26976000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Soci%C3%A9t%C3%A9_G%C3%A9n%C3%A9rale' } , { v: 'Société Générale' } , { v: 130100 } , { v: 21923000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Grupo_Santander' } , { v: 'Grupo Santander' } , { v: 131153 } , { v: 27068000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Delhaize_Group' } , { v: 'Delhaize Group' } , { v: 142500 } , { v: 19200000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Electrabel' } , { v: 'Electrabel' } , { v: 15794 } , { v: 12218000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/TNT_N.V.' } , { v: 'TNT N.V.' } , { v: 161000 } , { v: 11000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Unilever' } , { v: 'Unilever' } , { v: 174 } , { v: 40187000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Mitsubishi_Fuso_Truck_and_Bus_Corporation' } , { v: 'Mitsubishi Fuso Truck and Bus Corporation' } , { v: 18200 } , { v: 30368000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Auchan' } , { v: 'Auchan' } , { v: 186000 } , { v: 36710000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/France_T%C3%A9l%C3%A9com' } , { v: 'France Télécom' } , { v: 187331 } , { v: 52959000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/ThyssenKrupp' } , { v: 'ThyssenKrupp' } , { v: 195 } , { v: 51700000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Fiat' } , { v: 'Fiat' } , { v: 200701 } , { v: 58529000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Mapfre' } , { v: 'Mapfre' } , { v: 21500 } , { v: 13600000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Gruppo_Riva' } , { v: 'Gruppo Riva' } , { v: 24684 } , { v: 10100000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Telef%C3%B3nica' } , { v: 'Telefónica' } , { v: 248487 } , { v: 56441000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Edeka' } , { v: 'Edeka' } , { v: 250000 } , { v: 38000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Bayer_Schering_Pharma' } , { v: 'Bayer Schering Pharma' } , { v: 26000 } , { v: 10267000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Adidas' } , { v: 'Adidas' } , { v: 27000 } , { v: 10299000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Veolia_Environnement' } , { v: 'Veolia Environnement' } , { v: 300000 } , { v: 32600000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/AGF_%28company%29' } , { v: 'AGF (company)' } , { v: 32000 } , { v: 17300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Repsol_YPF' } , { v: 'Repsol YPF' } , { v: 36 } , { v: 55923000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Munich_Re' } , { v: 'Munich Re' } , { v: 37210 } , { v: 37400000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Astaldi' } , { v: 'Astaldi' } , { v: 4709 } , { v: 19300000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/SAP_AG' } , { v: 'SAP AG' } , { v: 51447 } , { v: 10250000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Henkel' } , { v: 'Henkel' } , { v: 52 } , { v: 13070000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Deutsche_Post' } , { v: 'Deutsche Post' } , { v: 520112 } , { v: 63500000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Strabag' } , { v: 'Strabag' } , { v: 52791 } , { v: 10400000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Safran' } , { v: 'Safran' } , { v: 57000 } , { v: 12000000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Deutsche_Bank' } , { v: 'Deutsche Bank' } , { v: 68849 } , { v: 67706000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Enel' } , { v: 'Enel' } , { v: 73500 } , { v: 42700000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/Arcelor' } , { v: 'Arcelor' } , { v: 94000 } , { v: 32611000000 } ] } , { c: [ { v: 'http://dbpedia.org/resource/BASF' } , { v: 'BASF' } , { v: 95200 } , { v: 57950000000 } ] } ] }   ;
  var data = new google.visualization.DataTable(table, 0.6);
  var view = new google.visualization.DataView(data);
  view.hideColumns([0, 1]);

  var table = new google.visualization.ScatterChart(document.getElementById('chart_div'));
  table.draw(view, { width: 800, height: 400 } );
}

					</script>
					<button onclick="drawTable();">table!</button>
					<div id="table_div"></div>
					<button onclick="drawChart();">chart!</button>
					<div id="chart_div"></div>
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