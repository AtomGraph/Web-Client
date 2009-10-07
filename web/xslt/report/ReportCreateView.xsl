<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
        <!ENTITY rep "http://www.semantic-web.dk/ontologies/semantic-reports/">
        <!ENTITY vis "http://code.google.com/apis/visualization/">
        <!ENTITY dc "http://purl.org/dc/elements/1.1/">
        <!ENTITY spin "http://spinrdf.org/sp#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
exclude-result-prefixes="#all">

	<xsl:import href="../sparql2google-wire.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:param name="query-result"/>
	<xsl:param name="visualization-result"/>
	<xsl:param name="query-string" select="''"/>
	<xsl:param name="report-id"/>
	<xsl:param name="report-uri" select="concat('http://localhost:8084/semantic-reports/reports/', $report-id)"/>

	<xsl:template name="title">
		Create report
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<xsl:copy-of select="document('arg://visualization-types')"/>
			<xsl:copy-of select="document('arg://report')"/>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<input type="hidden" name="view" value="create"/>
                                        <input type="hidden" name="report-id" value="{$report-id}"/>
<input type="hidden" name="rdf"/>
<input type="hidden" name="v" value="&vis;"/>
<input type="hidden" name="n" value="rdf"/>
<input type="hidden" name="v" value="&rdf;"/>
<input type="hidden" name="n" value="rep"/>
<input type="hidden" name="v" value="&rep;"/>
<input type="hidden" name="n" value="dc"/>
<input type="hidden" name="v" value="&dc;"/>
<input type="hidden" name="n" value="spin"/>
<input type="hidden" name="v" value="&spin;"/>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="on" value="rep"/>
<input type="hidden" name="ov" value="Report"/>
<input type="hidden" name="pu" value="&rep;query"/>
<input type="hidden" name="ob" value="query"/>

<input type="hidden" name="sb" value="query"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="on" value="spin"/>
<input type="hidden" name="ov" value="Select"/>

					<label for="query-string">Query</label>
					<br/>
<input type="hidden" name="pn" value="spin"/>
<input type="hidden" name="pv" value="text"/>
<input type="hidden" name="lt" value="&xsd;string"/>

					<textarea cols="80" rows="20" id="query-string" name="ol">
						<xsl:if test="$query-result">
							<xsl:value-of select="$query-string"/>
						</xsl:if>
					</textarea>
					<br/>
<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pn" value="dc"/>
<input type="hidden" name="pv" value="title"/>
<input type="hidden" name="lt" value="&xsd;string"/>

					<label for="title">Title</label>
					<input type="text" id="title" name="ol" value="whatever!!"/>
					<br/>
<input type="hidden" name="sb" value="query"/>
<input type="hidden" name="pn" value="spin"/>
<input type="hidden" name="pv" value="from"/>

					<label for="endpoint">Endpoint</label>
					<input type="text" id="endpoint" name="ou" value="http://dbpedia.org/sparql"/>
					<br/>
					<button type="submit" name="action" value="query">Query</button>
					<button type="submit" name="action" value="save">Save</button>
				</p>
				
				<fieldset>
					<legend>Visualizations</legend>
					<ul>
						<xsl:apply-templates select="document('arg://visualization-types')" mode="vis-type"/>
						
						<!--
						<li>
							<input type="checkbox" id="table-option" name="visualization" value="table" checked="checked"/>
							<label for="table-option">Table</label>
						</li>
						<li>
							<input type="checkbox" id="scatter-chart-option" name="visualization" value="&vis;ScatterChart" onclick="toggleScatterChart(this.checked);"/>
							<label for="scatter-chart-option">Scatter chart</label>
						</li>
						<li>
							<input type="checkbox" id="line-chart-option" name="visualization" value="&vis;LineChart"/>
							<label for="line-chart-option">Line chart</label>
						</li>
						<li>
							<input type="checkbox" id="pie-chart-option" name="visualization" value="&vis;PieChart"/>
							<label for="pie-chart-option">Pie chart</label>
						</li>
						<li>
							<input type="checkbox" id="map-option" name="visualization" value="&vis;Map"/>
							<label for="map-option">Map</label>
						</li>
						-->
					</ul>
				</fieldset>

		<div id="table"></div>

				<fieldset id="scatter-chart-controls">
					<legend>Scatter chart</legend>
					<p>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&rep;visualisedBy"/>
<input type="hidden" name="ob" value="vis"/>

<input type="hidden" name="sb" value="vis"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ov" value="ScatterChart"/>
<input type="hidden" name="pv" value="xBinding"/>
<input type="hidden" name="lt" value="&xsd;string"/>

						<label for="scatter-chart-x-binding">X binding</label>
						<select id="scatter-chart-x-binding" name="ol" onchange="drawScatterChart(getSelectedValues(this)[0], getSelectedValues(document.getElementById('scatter-chart-y-binding')));">
							<!-- filled out in JavaScript -->
						</select>
						<label for="scatter-chart-y-binding">Y bindings</label>
<input type="hidden" name="pv" value="yBinding"/>
<input type="hidden" name="lt" value="&xsd;string"/>

						<select id="scatter-chart-y-binding" name="ol" multiple="multiple" onchange="drawScatterChart(getSelectedValues(document.getElementById('scatter-chart-x-binding'))[0], getSelectedValues(this));">
							<!-- filled out in JavaScript -->
						</select>
						<!--
						<input type="hidden" name="visualization" value="scatter-chart"/>
						<button type="submit" name="action" value="update">Update</button>
						-->
					</p>
				</fieldset>

		<div id="scatter-chart" style="width: 800px; height: 400px;"></div>

				<fieldset>
					<legend>Line chart</legend>
					<p>
						<label for="line-chart-label-binding">Label binding</label>
						<select id="line-chart-label-binding" name="line-chart-label-binding">
							<!-- filled out in JavaScript -->
						</select>
						<label for="line-chart-value-binding">Value bindings</label>
						<select id="line-chart-value-binding" name="line-chart-value-binding" multiple="multiple">
							<!-- filled out in JavaScript -->
						</select>
						<input type="hidden" name="visualization" value="line-chart"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>

		<div id="line-chart" style="width: 800px; height: 400px;"></div>

				<fieldset>
					<legend>Pie chart</legend>
					<p>
						<label for="pie-chart-label-binding">Label binding</label>
						<select id="pie-chart-label-binding" name="pie-chart-label-binding">
							<!-- filled out in JavaScript -->
						</select>
						<label for="pie-chart-value-binding">Value binding</label>
						<select id="pie-chart-value-binding" name="pie-chart-value-binding">
							<!-- filled out in JavaScript -->
						</select>
						<input type="hidden" name="visualization" value="pie-chart"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>

		<div id="pie-chart" style="width: 800px; height: 400px;"></div>

				<fieldset>
					<legend>Map</legend>
					<p>
						<label for="map-lat-binding">Latitude binding</label>
						<select id="map-lat-binding" name="map-lat-binding">
							<!-- filled out in JavaScript -->
						</select>
						<label for="map-lng-binding">Longitude binding</label>
						<select id="map-lng-binding" name="map-lng-binding">
							<!-- filled out in JavaScript -->
						</select>
						<input type="hidden" name="visualization" value="map"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>

		<div id="map" style="width: 800px; height: 400px;"></div>
			</form>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="vis-type">
		<li>
			<input type="checkbox" id="{generate-id()}-option" name="visualization" value="{sparql:binding[@name = 'type']/sparql:uri}" checked="checked"/>
			<label for="{generate-id()}-option">
				<xsl:value-of select="sparql:binding[@name = 'label']/sparql:literal"/>
			</label>
		</li>
	</xsl:template>

</xsl:stylesheet>