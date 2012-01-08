<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of Graphity Analytics package.
Copyright (C) 2009-2011  Martynas Jusevičius

Graphity Analytics is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
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

        <xsl:variable name="endpoint" select="/" as="document-node()"/>
        <xsl:variable name="reports" select="document('arg://reports')" as="document-node()"/>

	<xsl:template name="title">
		Semantic Reports: Endpoints: <xsl:value-of select="$endpoint//sparql:binding[@name = 'title']/sparql:literal"/>
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

			<xsl:apply-templates select="$endpoint" mode="endpoint"/>

			<h3>Reports</h3>
			<table>
				<thead>
					<td>Title</td>
					<td>Description</td>
					<td>Used URIs</td>
					<td>Endpoint</td>
					<td>Creator</td>
					<td>Created</td>
					<td>Modified</td>
                                </thead>
				<tbody>
					<xsl:apply-templates select="$reports" mode="report-table"/>
				</tbody>
			</table>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'endpoint']/sparql:uri]" mode="endpoint">
		<dl>
			<dt>URI</dt>
			<dd>
				<a href="{sparql:binding[@name = 'endpoint']/sparql:uri}">
					<xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
				</a>
			</dd>
			<!--
			<dt># of reports</dt>
			<dd>
				<xsl:value-of select="sparql:binding[@name = 'reportCount']/sparql:literal"/>
			</dd>
			-->
		</dl>
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
			    <!--
                            <xsl:variable name="current-uris" select="$query-uris//sparql:result[sparql:binding[@name = 'report']/sparql:uri = current()/sparql:binding[@name = 'report']/sparql:uri]"/>
                            <xsl:if test="$current-uris">
                                <ul>
                                    <xsl:apply-templates select="$current-uris" mode="uri-list-item"/>
                                </ul>
                             </xsl:if>
			     -->
			</td>
			<td>
				<a href="{$endpoint//sparql:binding[@name = 'endpoint']/sparql:uri}?query={encode-for-uri(sparql:binding[@name = 'queryString']/sparql:literal)}">
                                    <xsl:choose>
                                        <xsl:when test="$endpoint">
                                            <xsl:value-of select="$endpoint//sparql:binding[@name = 'title']/sparql:literal"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$endpoint//sparql:binding[@name = 'endpoint']/sparql:uri"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
				</a>
			</td>
			<td>
                                <a href="{sparql:binding[@name = 'creator']/sparql:uri}">
                                    <xsl:value-of select="sparql:binding[@name = 'creatorName']/sparql:literal"/>
                                </a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'dateCreated']/sparql:literal"/>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'dateModified']/sparql:literal"/>
			</td>
		</tr>
	</xsl:template>

</xsl:stylesheet>