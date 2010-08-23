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
exclude-result-prefixes="#all">

	<xsl:import href="../sparql2google-wire.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:param name="query-result"/>
	<xsl:variable name="report" select="document('arg://report')"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<xsl:copy-of select="document('arg://report')"/>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<textarea cols="80" rows="20" name="query-string">
						<xsl:value-of select="$report//sparql:binding[@name = 'queryString']/sparql:literal"/>
					</textarea>
					<br/>
					<button type="submit" name="action" value="query">Query</button>
				</p>
			</form>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="post" accept-charset="UTF-8">
				<p>
					<label for="title">Title</label>
					<input type="text" id="title" name="title" value="{$report//sparql:binding[@name = 'title']/sparql:literal}"/>
					<input type="hidden" name="query-string" value="{$report//sparql:binding[@name = 'queryString']/sparql:literal}"/>
					<button type="submit" name="action" value="update">Save</button>
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

</xsl:stylesheet>