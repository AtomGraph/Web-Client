<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
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
	<xsl:param name="visualization-result"/>
	<xsl:param name="query-string" select="''"/>

	<xsl:template name="title">
		Create report
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<xsl:copy-of select="document('arg://report')"/>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<input type="hidden" name="view" value="create"/>
					<label for="query-string">Query</label>
					<br/>
					<textarea cols="80" rows="20" id="query-string" name="query-string">
						<xsl:if test="$query-result">
							<xsl:value-of select="$query-string"/>
						</xsl:if>
					</textarea>
					<br/>
					<label for="title">Title</label>
					<input type="text" id="title" name="title" value="whatever!!"/>
					<br/>
					<label for="endpoint">Endpoint</label>
					<input type="text" id="endpoint" name="endpoint" value="http://dbpedia.org/sparql"/>
					<br/>
					<button type="submit" name="action" value="query">Query</button>
					<button type="submit" name="action" value="save">Save</button>
				</p>
				
				<fieldset>
					<legend>Visualizations</legend>
					<ul>
						<li>
							<input type="checkbox" id="table-option" name="visualization" value="table" checked="checked"/>
							<label for="table-option">Table</label>
						</li>
						<li>
							<input type="checkbox" id="scatter-chart-option" name="visualization" value="scatter-chart"/>
							<label for="scatter-chart-option">Scatter chart</label>
						</li>
						<li>
							<input type="checkbox" id="line-chart-option" name="visualization" value="line-chart"/>
							<label for="line-chart-option">Line chart</label>
						</li>
						<li>
							<input type="checkbox" id="pie-chart-option" name="visualization" value="pie-chart"/>
							<label for="pie-chart-option">Pie chart</label>
						</li>
						<li>
							<input type="checkbox" id="map-option" name="visualization" value="map"/>
							<label for="map-option">Map</label>
						</li>
					</ul>
				</fieldset>

		<div id="table"></div>

				<fieldset>
					<legend>Scatter chart</legend>
					<p>
						<label for="scatter-chart-x-binding">X binding</label>
						<select id="scatter-chart-x-binding" name="x-binding">
							<!-- <option value="population">population</option>
							<option value="area">area</option> -->
						</select>
						<label for="scatter-chart-y-binding">Y binding</label>
						<select id="scatter-chart-y-binding" name="y-binding" multiple="multiple">
							<!-- <option value="population">population</option>
							<option value="area">area</option> -->
						</select>
						<input type="hidden" name="visualization" value="scatter-chart"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>

		<div id="scatter-chart" style="width: 800px; height: 400px;"></div>

				<fieldset>
					<legend>Line chart</legend>
					<p>
						<label for="label-binding">Label binding</label>
						<select id="label-binding" name="label-binding">
							<option value="population">population</option>
							<option value="area">area</option>
						</select>
						<label for="y-binding">Y binding</label>
						<select id="y-binding" name="y-binding" multiple="multiple">
							<option value="population">population</option>
							<option value="area">area</option>
						</select>
						<input type="hidden" name="visualization" value="scatter-chart"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>

		<div id="line-chart" style="width: 800px; height: 400px;"></div>

				<fieldset>
					<legend>Pie chart</legend>
					<p>
						<label for="x-binding">X binding</label>
						<select id="x-binding" name="x-binding">
							<option value="population">population</option>
							<option value="area">area</option>
						</select>
						<label for="y-binding">Y binding</label>
						<select id="y-binding" name="y-binding" multiple="multiple">
							<option value="population">population</option>
							<option value="area">area</option>
						</select>
						<input type="hidden" name="visualization" value="scatter-chart"/>
						<button type="submit" name="action" value="update">Update</button>
					</p>
				</fieldset>
			
			</form>
		</div>
	</xsl:template>

</xsl:stylesheet>