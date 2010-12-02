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

	<xsl:param name="relative-uri"/>
	<xsl:param name="host-uri"/>
	<xsl:param name="uri" select="xs:anyURI(concat($host-uri, $relative-uri))" as="xs:anyURI"/>
        <xsl:param name="view" as="xs:string"/>

	<xsl:variable name="resource" select="/"/>

	<xsl:template match="sparql:sparql">
		<html xmlns="http://www.w3.org/1999/xhtml"> <!-- xml:base="{$base_url}" -->
			<head>
                                <base href="{$host-uri}"/>
				<link href="static/css/index.css" rel="stylesheet" type="text/css" media="all"/>

                                <meta name="author" content="http://semantic-web.dk"/>
                                <meta name="DC.title" content="Semantic Reports"/>
				<meta name="description" xml:lang="en" lang="en" content="Generic SPARQL results visualizer and report datastore"/>
				<meta name="keywords" xml:lang="en" lang="en" content="semantic, reports, semantic web, linked data, rdf, sparql, query, generic, endpoint, visualization"/>
				<meta name="google-site-verification" content="9OYr7f_MgT7pH7oSBQY6D2zB7mzZFWy-1ilHcpJtVsg" />

                                <xsl:call-template name="head"/>

				<script type="text/javascript">

				  var _gaq = _gaq || [];
				  _gaq.push(['_setAccount', 'UA-1004105-6']);
				  _gaq.push(['_trackPageview']);

				  (function() {
				    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
				    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
				    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
				  })();

				</script>
                        </head>
			<body>
                                <xsl:if test="$view = 'frontend.view.report.ReportCreateView' or $view = 'frontend.view.report.ReportUpdateView' or $view = 'frontend.view.report.ReportReadView'">
                                    <xsl:call-template name="body-onload"/>
                                </xsl:if>

				<h1>
					<a href="{$host-uri}">Semantic Reports</a>
				</h1>

				<p>Generic SPARQL results visualizer and report datastore</p>
				
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

                                <div id="footer">
                                    <p>
                                        <a href="mailto:info@semantic-web.dk">info@semantic-web.dk</a>
                                    </p>
                                </div>
			</body>
		</html>
	</xsl:template>

        <xsl:template name="report-scripts">
            <script type="text/javascript" src="http://www.google.com/jsapi">&#160;</script>
            <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key=ABQIAAAACeGvD278ackc4SWUVEJSXBRKvlh_JZwu81_tOS6Bm9fWR6zB2BRWlRbMrtA0atMf6bgsA7OsCjgdVw" type="text/javascript">&#160;</script>

            <script type="text/javascript">
            google.load('visualization', '1',  {'packages': ["corechart", "table", "map"]});

            var table = <xsl:apply-templates select="document('arg://results')" mode="sparql2wire"/>;
            </script>
            
            <script type="text/javascript" src="static/js/report.js">&#160;</script>
	    <!--
	    <script type="text/javascript" src="static/js/sparql.js">&#160;</script>
	    <script type="text/javascript">
		var sparqler = new SPARQL.Service("<xsl:value-of select="$host-uri"/>sparql");
		sparqler.setMethod("GET");
	    </script>
	    -->
	</xsl:template>

</xsl:stylesheet>