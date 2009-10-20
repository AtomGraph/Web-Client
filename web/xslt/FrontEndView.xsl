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
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
exclude-result-prefixes="#all">

	<!-- <xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/> -->
	<xsl:output method="html" encoding="UTF-8" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="text/html"/>

	<xsl:param name="uri"/>
	<xsl:param name="host-uri"/>
        <xsl:param name="view"/>

	<xsl:variable name="resource" select="/"/>
<xsl:key name="variable-by-visualization" match="sparql:result" use="sparql:binding[@name = 'visualization']/sparql:uri"/>

	<xsl:template match="sparql:sparql">
		<html xmlns="http://www.w3.org/1999/xhtml"> <!-- xml:base="{$base_url}" -->
			<head>
				<title>
					<xsl:call-template name="title"/>
					<!-- <xsl:copy-of select="document('arg://companies')"/> -->
				</title>


				<base href="{$host-uri}"/>
				<!-- <link href="xhtml/css/index.css" rel="stylesheet" type="text/css" media="all"/> -->
				<style type="text/css">
					html { font-family: "Arial"; font-size: small; }
					/* h1, h2, h3 { font-family: "Georgia"; } */
					#left, #main, #right { float: left; }
					#left, #right { width: 15%; }
					#main { width: 65%; }
					span.red { color: red; }
					table, td { border: 1px solid black; }
					thead { font-weight: bold; text-align: center; }
				</style>

				<script type="text/javascript" src="http://www.google.com/jsapi"></script>
				<script src="http://maps.google.com/maps?file=api&amp;v=2" type="text/javascript"></script>
				<script type="text/javascript">
google.load('visualization', '1',  {'packages': ["table", "scatterchart", "linechart", "piechart", "map"]});

var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
				</script>
				<script type="text/javascript" src="static/js/report.js"></script>

			</head>
			<body>

                <xsl:if test="$view = 'frontend.view.report.ReportReadView' or $view = 'frontend.view.report.ReportUpdateView'">
					<xsl:attribute name="onload">countColumns(data);
            <xsl:for-each select="document('arg://visualizations')//sparql:result">
                <xsl:text>init(document.getElementById('</xsl:text><xsl:value-of select="generate-id()"/><xsl:text>-visualization'), '</xsl:text>
                <xsl:value-of select="sparql:binding[@name = 'visualization']/sparql:uri"/>
                <xsl:text>', '</xsl:text>
                <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                <xsl:text>', [</xsl:text>
                    <xsl:for-each select="key('variable-by-visualization', sparql:binding[@name = 'visualization']/sparql:uri, document('arg://variables'))">
                        <xsl:text>{ type: '</xsl:text>
                            <xsl:value-of select="sparql:binding[@name = 'bindingType']/sparql:uri"/>
                        <xsl:text>', value: </xsl:text>
                        <xsl:value-of select="sparql:binding[@name = 'variable']/sparql:literal"/>
                        <xsl:text> }</xsl:text>
                        <xsl:if test="position() != last()">,</xsl:if>
                    </xsl:for-each>
                <xsl:text>]);</xsl:text>
            </xsl:for-each>
                                        </xsl:attribute>
<!-- <xsl:copy-of select="key('variable-by-visualization', 'http://temp.com/visualization/123', document('arg://variables'))"/> -->

                                </xsl:if>
                <xsl:if test="$view = 'frontend.view.report.ReportCreateView'">
                                    <xsl:attribute name="onload">countColumns(data); 
            <xsl:for-each select="document('arg://visualization-types')//sparql:result">
                <xsl:text>initEmpty(document.getElementById('</xsl:text><xsl:value-of select="generate-id()"/><xsl:text>-visualization'), '</xsl:text>
                <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                <xsl:text>', [</xsl:text>
                <xsl:for-each select="key('binding-type-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, document('arg://binding-types'))">
                    <xsl:text>document.getElementById('</xsl:text><xsl:value-of select="generate-id()"/><xsl:text>-binding')</xsl:text>
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                <xsl:text>], numericColumns);</xsl:text>
            </xsl:for-each>
                                    </xsl:attribute>
				</xsl:if>

				<h1>
					<a href="{$host-uri}">Semantic Reports</a>
				</h1>
				<!--
				<div id="left">
					<h1>whatsup</h1>
					<ul>
						<li>
							<a href="Location/">Locations</a>
						</li>
						<li>
							<a href="Place/">Places</a>
						</li>
						<li>
							<a href="Time/">Time</a>
						</li>
						<li>
							<a href="Event/">Events</a>
						</li>
						<li>
							<a href="Person/">People</a>
						</li>
					</ul>
					<ul>
						<li>
							<a href="Settings">Settings</a>
						</li>
						<li>
							<a href="Query">Query</a>
						</li>
						<li>
							<a href="Search">Search</a>
						</li>
					</ul>
				</div>
				-->

				<xsl:call-template name="content"/>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>