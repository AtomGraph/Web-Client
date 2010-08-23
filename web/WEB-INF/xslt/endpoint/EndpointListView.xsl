<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
        <!ENTITY rep "http://www.semantic-web.dk/ontologies/semantic-reports/">
	<!ENTITY vis "http://code.google.com/apis/visualization/">
        <!ENTITY spin "http://spinrdf.org/sp#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
        <!ENTITY dc "http://purl.org/dc/elements/1.1/">
	<!ENTITY foaf "http://xmlns.com/foaf/0.1/">
	<!ENTITY sioc "http://rdfs.org/sioc/ns#">
        <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
        <!ENTITY dbpedia "http://dbpedia.org/resource/">
        <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
        <!ENTITY dbpprop "http://dbpedia.org/property/">
        <!ENTITY category "http://dbpedia.org/resource/Category:">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:sparql="&sparql;"
exclude-result-prefixes="#all">

        <xsl:include href="../FrontEndView.xsl"/>
        <xsl:include href="../query-string.xsl"/>
        <xsl:include href="../page-numbers.xsl"/>

        <xsl:param name="total-item-count" as="xs:integer"/>
	<!--
        <xsl:param name="offset" select="0" as="xs:integer"/>
        <xsl:param name="limit" select="20" as="xs:integer"/>
        <xsl:param name="order-by" as="xs:string"/>
        <xsl:param name="desc-default" select="true()" as="xs:boolean"/>
        <xsl:param name="desc" select="$desc-default" as="xs:boolean"/>
	-->

        <xsl:variable name="endpoints" select="/" as="document-node()"/>
	<!-- <xsl:variable name="reports" select="document('arg://reports')" as="document-node()"/> -->

	<xsl:template name="title">
		Semantic Reports: Endpoints
	</xsl:template>

	<xsl:template name="head">
            <title>
                <xsl:call-template name="title"/>
            </title>
        </xsl:template>

	<xsl:template name="body-onload">
        </xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2>
                            <xsl:call-template name="title"/>
                        </h2>

<xsl:copy-of select="$endpoints"/>
			<!--
                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>
			-->

			<table style="width: 100%;">
				<thead>
					<td>Title</td>
					<td>URI</td>
					<td># of reports</td>
                                </thead>
				<tbody>
					<xsl:apply-templates select="$endpoints" mode="endpoint-table"/>
				</tbody>
			</table>

			<!--
                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>
			-->
		</div>
	</xsl:template>

	<xsl:template match="sparql:result" mode="endpoint-table">
		<tr>
			<td>
				<a href="/endpoints/{encode-for-uri(sparql:binding[@name = 'endpoint']/sparql:uri)}">
				    <xsl:value-of select="sparql:binding[@name = 'title']/sparql:literal"/>
				</a>
			</td>
			<td>
				<a href="{sparql:binding[@name = 'endpoint']/sparql:uri}">
					<xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
				</a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'reportCount']/sparql:literal"/>
			</td>
		</tr>
	</xsl:template>


</xsl:stylesheet>