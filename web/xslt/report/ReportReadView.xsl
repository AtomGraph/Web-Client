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

	<xsl:import href="../sparql2google-wire.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:variable name="report" select="document('arg://report')"/>
	<xsl:variable name="query-objects" select="document('arg://query-objects')"/>

        <xsl:key name="binding-type-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="body-onload">
            countColumns(data);
            <xsl:for-each select="document('arg://visualizations')//sparql:result">
                <xsl:text>initAndDraw(document.getElementById('</xsl:text>
                <xsl:value-of select="generate-id()"/>
                <xsl:text>-visualization'), '</xsl:text>
                <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                <xsl:text>', [</xsl:text>
                <xsl:for-each select="key('binding-by-visualization', sparql:binding[@name = 'visualization']/sparql:uri, document('arg://bindings'))">
                    <xsl:text>'</xsl:text>
                    <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                    <xsl:text>'</xsl:text>
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                <xsl:text>], [</xsl:text>
                <xsl:for-each select="key('binding-by-visualization', sparql:binding[@name = 'visualization']/sparql:uri, document('arg://bindings'))">
                    <xsl:text>{ 'columns' : [</xsl:text>
                    <xsl:for-each select="key('variable-by-binding', sparql:binding[@name = 'binding']/sparql:uri, document('arg://variables'))">
                        <xsl:value-of select="sparql:binding[@name = 'variable']/sparql:literal"/>
                        <xsl:if test="position() != last()">,</xsl:if>
                    </xsl:for-each>
                    <xsl:text>], 'bindingType' : '</xsl:text>
                    <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                    <xsl:text>' }</xsl:text>
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each>
                <xsl:text>]);</xsl:text>
            </xsl:for-each>
        </xsl:template>
        
	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<!-- <xsl:copy-of select="document('arg://report')"/> -->
                        <!-- <xsl:copy-of select="document('arg://results')"/> -->
			<!-- <xsl:copy-of select="document('arg://visualizations')"/> -->
                        <!--
			<xsl:copy-of select="document('arg://bindings')"/>
                        <xsl:copy-of select="document('arg://variables')"/>
			<xsl:copy-of select="document('arg://query-objects')"/>
                        -->

			<dl>
				<dt>Endpoint</dt>
				<dd>
					<a href="{$report//sparql:binding[@name = 'endpoint']/sparql:uri}">
						<xsl:value-of select="$report//sparql:binding[@name = 'endpoint']/sparql:uri"/>
					</a>
				</dd>
                                <xsl:if test="$query-objects//sparql:binding[@name = 'object']/sparql:uri">
                                    <dt>Used types</dt>
                                    <xsl:for-each select="$query-objects//sparql:binding[@name = 'object']/sparql:uri">
                                        <dd>
                                                <a href="{.}">
                                                    <xsl:value-of select="."/>
                                                </a>
                                        </dd>
                                    </xsl:for-each>
                                </xsl:if>
                                <dt>Created by</dt>
				<dd>
					<a href="{$report//sparql:binding[@name = 'creator']/sparql:uri}">
						<xsl:value-of select="$report//sparql:binding[@name = 'creator']/sparql:uri"/>
					</a>
				</dd>
				<dt>Date</dt>
				<dd>
                                        <xsl:value-of select="$report//sparql:binding[@name = 'date']/sparql:literal"/>
				</dd>
                        </dl>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="update">Edit</button>
				</p>
			</form>

			<xsl:apply-templates select="document('arg://visualizations')//sparql:result" mode="vis-container"/>
                        <!-- <xsl:apply-templates select="document('arg://visualization-types')" mode="vis-type-container"/> -->
		</div>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="vis-type-container">
            <div id="{generate-id()}-visualization" style="width: 800px; height: 400px;"></div>
        </xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'visualization']]" mode="vis-container">
            <div id="{generate-id()}-visualization" style="width: 800px; height: 400px;"></div>
        </xsl:template>

</xsl:stylesheet>