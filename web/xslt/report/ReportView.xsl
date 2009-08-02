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
					<button type="submit" name="action" value="query">Query</button>
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