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
					<select>
						<xsl:apply-templates select="document('arg://reports')" mode="report-list"/>
					</select>
					<button type="submit" name="action" value="load">Load</button>
				</p>
			</form>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<textarea cols="80" rows="20" name="query-string">
						<xsl:value-of select="$query-string"/>
					</textarea>
					<br/>
					<button type="submit" name="action" value="query">Query</button>
				</p>
			</form>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="post" accept-charset="UTF-8">
				<p>
					<input type="text" name="title" value="Biggest cities by population, with area size and location"/>
					<input type="hidden" name="query-string" value="{$query-string}"/>
					<button type="submit" name="action" value="save">Save</button>
				</p>
			</form>

			<xsl:if test="$query-result">
				<p>

					<button onclick="drawTable();">table!</button>
					<div id="table"></div>
					<button onclick="drawScatter();">scatter!</button>
					<div id="scatter-chart" style="width: 800px; height: 400px;"></div>
					<button onclick="drawLine();">line!</button>
					<div id="line-chart" style="width: 800px; height: 400px;"></div>
					<button onclick="drawPie();">pie!</button>
					<div id="pie-chart" style="width: 800px; height: 400px;"></div>
					<button onclick="drawMap();">map!</button>
					<div id="map" style="width: 800px; height: 400px;"></div>
				</p>
			</xsl:if>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result" mode="report-list">
		<option value="{sparql:binding[@name = 'report']/sparql:uri}">
			<xsl:value-of select="sparql:binding[@name = 'title']/sparql:literal"/>
		</option>
	</xsl:template>

</xsl:stylesheet>