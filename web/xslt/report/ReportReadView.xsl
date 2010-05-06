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

	<xsl:variable name="report" select="document('arg://report')" as="document-node()"/>
	<xsl:variable name="visualizations" select="document('arg://visualizations')" as="document-node()"/>
	<xsl:variable name="bindings" select="document('arg://bindings')" as="document-node()"/>
	<xsl:variable name="variables" select="document('arg://variables')" as="document-node()"/>
        <xsl:variable name="query-objects" select="document('arg://query-objects')" as="document-node()"/>
        <xsl:variable name="binding-types" select="document('arg://binding-types')" as="document-node()"/>

        <xsl:key name="binding-type-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="body-onload">
            <xsl:attribute name="onload">
                <xsl:text>countColumns(data); </xsl:text>
                <xsl:for-each select="$visualizations//sparql:result">
                    <xsl:text>initAndDraw(document.getElementById('</xsl:text>
                    <xsl:value-of select="generate-id()"/>
                    <xsl:text>-visualization'), '</xsl:text>
                    <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                    <xsl:text>', [</xsl:text>
                    <xsl:for-each select="key('binding-type-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, $binding-types)">
                        <xsl:text>{ 'type': '</xsl:text>
                        <xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
                        <xsl:text>'</xsl:text>
                        <xsl:if test="sparql:binding[@name = 'cardinality']/sparql:literal">
                            <xsl:text>, 'cardinality': </xsl:text>
                            <xsl:value-of select="sparql:binding[@name = 'cardinality']/sparql:literal"/>
                        </xsl:if>
                        <xsl:if test="sparql:binding[@name = 'minCardinality']/sparql:literal">
                            <xsl:text>, 'minCardinality': </xsl:text>
                            <xsl:value-of select="sparql:binding[@name = 'minCardinality']/sparql:literal"/>
                        </xsl:if>
                        <xsl:if test="sparql:binding[@name = 'maxCardinality']/sparql:literal">
                            <xsl:text>, 'maxCardinality': </xsl:text>
                            <xsl:value-of select="sparql:binding[@name = 'maxCardinality']/sparql:literal"/>
                        </xsl:if>
                        <xsl:if test="sparql:binding[@name = 'order']/sparql:literal">
                            <xsl:text>, 'order': </xsl:text>
                            <xsl:value-of select="sparql:binding[@name = 'order']/sparql:literal"/>
                        </xsl:if>
                        <xsl:text> }</xsl:text>
                        <xsl:if test="position() != last()">,</xsl:if>
                    </xsl:for-each>
                    <xsl:text>], [</xsl:text>
                    <xsl:for-each select="key('variable-by-visualization', sparql:binding[@name = 'visualization']/sparql:uri, $variables)">
                        <xsl:text>{ 'variable' : </xsl:text>
                        <xsl:value-of select="sparql:binding[@name = 'variable']/sparql:literal"/>
                        <xsl:text>, 'binding' : '</xsl:text>
                        <xsl:value-of select="sparql:binding[@name = 'binding']/sparql:uri"/>
                        <xsl:text>', 'bindingType' : '</xsl:text>
                        <xsl:value-of select="sparql:binding[@name = 'bindingType']/sparql:uri"/>
                        <xsl:text>' }</xsl:text>
                        <xsl:if test="position() != last()">,</xsl:if>
                    </xsl:for-each>
                    <xsl:text>]);</xsl:text>
                </xsl:for-each>
            </xsl:attribute>
        </xsl:template>
        
	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<!-- <xsl:copy-of select="$report"/> -->
                        <!-- <xsl:copy-of select="$results"/> -->
			<!-- <xsl:copy-of select="$visualizations"/> -->
                        <!--
			<xsl:copy-of select="$query-objects"/>
			<xsl:copy-of select="$bindings"/>
                        -->
                        <xsl:copy-of select="$variables"/>

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

			<xsl:apply-templates select="$visualizations//sparql:result" mode="vis-container"/>
		</div>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'visualization']]" mode="vis-container">
            <div id="{generate-id()}-visualization" style="width: 800px; height: 400px;">&#160;</div>
        </xsl:template>

</xsl:stylesheet>