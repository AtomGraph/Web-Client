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
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="#all">

	<!-- <xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/> -->
	<xsl:output method="html" encoding="UTF-8" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="text/html"/>

	<xsl:param name="uri"/>
	<xsl:param name="host-uri"/>
        <xsl:param name="view"/>

	<xsl:variable name="resource" select="/"/>

        <xsl:key name="binding-by-visualization" match="sparql:result" use="sparql:binding[@name = 'visualization']/sparql:uri"/>
        <xsl:key name="variable-by-visualization" match="sparql:result" use="sparql:binding[@name = 'visualization']/sparql:uri"/>
        <xsl:key name="variable-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>
        <xsl:key name="variable-by-binding" match="sparql:result" use="sparql:binding[@name = 'binding']/sparql:uri"/>
        <xsl:key name="variable-by-binding-type" match="sparql:result" use="sparql:binding[@name = 'bindingType']/sparql:uri"/>
        <xsl:key name="data-type-by-binding-type" match="sparql:result" use="sparql:binding[@name = 'bindingType']/sparql:uri"/>
        <xsl:key name="visualization-by-type" match="sparql:result" use="sparql:binding[@name = 'type']/sparql:uri"/>
        <xsl:key name="binding-by-type" match="sparql:result" use="sparql:binding[@name = 'type']/sparql:uri"/>

	<xsl:template match="sparql:sparql">
		<html xmlns="http://www.w3.org/1999/xhtml"> <!-- xml:base="{$base_url}" -->
			<head>
				<title>
					<xsl:call-template name="title"/>
				</title>


				<base href="{$host-uri}"/>
				<!-- <link href="xhtml/css/index.css" rel="stylesheet" type="text/css" media="all"/> -->
				<style type="text/css">
					html { font-family: "Arial"; font-size: small; }
					/* h1, h2, h3 { font-family: "Georgia"; } */
					#left, #main, #right { float: left; }
					#left, #right { width: 15%; }
					#main { width: 100%; }
					span.red { color: red; }
					table, td { border: 1px solid black; }
                                        table { width: 100%; }
					thead { font-weight: bold; text-align: center; }
                                        ol.pagination li { display: inline; }
                                        button { font-weight: bold; }
                                        ul#vis-types { padding: 0; }
                                        ul#vis-types li { display: inline; }
				</style>

				<script type="text/javascript" src="http://www.google.com/jsapi">&#160;</script>
				<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key=ABQIAAAACeGvD278ackc4SWUVEJSXBRKvlh_JZwu81_tOS6Bm9fWR6zB2BRWlRbMrtA0atMf6bgsA7OsCjgdVw" type="text/javascript">&#160;</script>
                                <script type="text/javascript">
google.load('visualization', '1',  {'packages': ["corechart", "table", "map"]});

var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
				</script>
				<script type="text/javascript" src="static/js/report.js">&#160;</script>
                                <meta name="author" content="http://semantic-web.dk"/>
                                <meta name="DC.title" content="Semantic Reports" />
			</head>
			<body>
                                <xsl:if test="$view = 'frontend.view.report.ReportCreateView' or $view = 'frontend.view.report.ReportUpdateView' or $view = 'frontend.view.report.ReportReadView'">
                                    <xsl:call-template name="body-onload"/>
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