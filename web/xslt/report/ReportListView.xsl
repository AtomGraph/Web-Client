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

        <xsl:include href="../FrontEndView.xsl"/>
        <xsl:include href="../query-string.xsl"/>
        <xsl:include href="../page-numbers.xsl"/>

        <xsl:param name="total-item-count"/>
        <xsl:param name="offset" select="0"/>
        <xsl:param name="limit" select="20"/>
        <xsl:param name="order-by"/>
        <xsl:param name="desc-default" select="true()"/>
        <xsl:param name="desc" select="$desc-default"/>

	<xsl:template name="title">
		Reports
	</xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="create">Create</button>
				</p>
			</form>
<!-- <xsl:copy-of select="document('arg://reports')"/> -->

                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>

			<table>
				<thead>
					<td>Title</td>
					<td>Description</td>
					<td>Keywords</td>
					<td>Datasource</td>
					<td>Creator</td>
					<td>Date</td>
				</thead>
				<tbody>
					<xsl:apply-templates select="document('arg://reports')" mode="report-table"/>
				</tbody>
			</table>

                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result" mode="report-table">
		<tr>
			<td>
				<a href="{sparql:binding[@name = 'report']/sparql:uri}">
					<xsl:value-of select="sparql:binding[@name = 'title']/sparql:literal"/>
				</a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'description']/sparql:literal"/>
			</td>
			<td>

			</td>
			<td>
				<a href="{sparql:binding[@name = 'endpoint']/sparql:uri}">
					<xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
				</a>
			</td>
			<td>
                                <a href="{sparql:binding[@name = 'creator']/sparql:uri}">
                                    <xsl:value-of select="sparql:binding[@name = 'creator']/sparql:uri"/>
                                </a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'date']/sparql:literal"/>
			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>