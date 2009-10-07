<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
	<!ENTITY vis "http://code.google.com/apis/visualization/">
]>
<xsl:stylesheet version="2.0"
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

	<xsl:variable name="report" select="document('arg://report')"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<!-- <xsl:copy-of select="document('arg://results')"/> -->
			<!-- <xsl:copy-of select="document('arg://visualizations')"/> -->

			<dl>
				<dt>Endpoint</dt>
				<dd>
					<a href="{$report//sparql:binding[@name = 'endpoint']/sparql:uri}">
						<xsl:value-of select="$report//sparql:binding[@name = 'endpoint']/sparql:uri"/>
					</a>
				</dd>
			</dl>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="update">Edit</button>
				</p>
			</form>

			<xsl:apply-templates select="document('arg://visualizations')//sparql:result" mode="visualization"/>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']/sparql:uri = '&vis;Table']" mode="visualization">
		<h3>Table</h3>
		<div id="table"></div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']/sparql:uri = '&vis;ScatterChart']" mode="visualization">
		<h3>Scatter chart</h3>
		<div id="scatter-chart" style="width: 800px; height: 400px;"></div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']/sparql:uri = '&vis;LineChart']" mode="visualization">
		<h3>Line chart</h3>
		<div id="line-chart" style="width: 800px; height: 400px;"></div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']/sparql:uri = '&vis;PieChart']" mode="visualization">
		<h3>Pie chart</h3>
		<div id="pie-chart" style="width: 800px; height: 400px;"></div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']/sparql:uri = '&vis;Map']" mode="visualization">
		<h3>Map</h3>
		<div id="map" style="width: 800px; height: 400px;"></div>
	</xsl:template>

</xsl:stylesheet>