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
xmlns:xsltsparql="http://berrueta.net/research/xsltsparql"
exclude-result-prefixes="#all">

        <xsl:include href="../FrontEndView.xsl"/>
        <xsl:include href="../query-string.xsl"/>
        <xsl:include href="../page-numbers.xsl"/>
        <xsl:include href="../xsltsparql.xsl"/>

        <xsl:param name="total-item-count" as="xs:integer"/>
        <xsl:param name="offset" select="0" as="xs:integer"/>
        <xsl:param name="limit" select="20" as="xs:integer"/>
        <xsl:param name="order-by" as="xs:string"/>
        <xsl:param name="desc-default" select="true()" as="xs:boolean"/>
        <xsl:param name="desc" select="$desc-default" as="xs:boolean"/>

	<xsl:variable name="reports" select="document('arg://reports')" as="document-node()"/>
        <xsl:variable name="endpoints" select="document('arg://endpoints')" as="document-node()"/>
        <!-- <xsl:variable name="query-objects" select="document('arg://query-objects')" as="document-node()"/> -->
	<xsl:variable name="query-uris" select="document('arg://query-uris')" as="document-node()"/>
	<xsl:variable name="schema-cache-endpoint" select="xs:anyURI('http://api.talis.com/stores/schema-cache/services/sparql')" as="xs:anyURI"/>
	<!-- query all external properties for labels -->
	<xsl:variable name="label-query" as="xs:string">
	    <xsl:variable name="query-items" as="xs:string*">
		<xsl:text>SELECT DISTINCT * { </xsl:text>
		<xsl:for-each-group select="$query-uris//sparql:binding[@name = 'uri']/sparql:uri[not(starts-with(., $host-uri))]" group-by=".">
		    <xsl:text>OPTIONAL { &lt;</xsl:text>
		    <xsl:value-of select="."/>
		    <xsl:text>&gt; rdfs:label ?label</xsl:text>
		    <xsl:value-of select="position()"/>
		    <xsl:text> } . </xsl:text>
		</xsl:for-each-group>
		<xsl:text> }</xsl:text>
	    </xsl:variable>
	    <xsl:value-of select="string-join($query-items, '')"/>
	</xsl:variable>
	<xsl:variable name="property-labels" select="xsltsparql:sparqlEndpoint(concat(xsltsparql:commonPrefixes(), $label-query), $schema-cache-endpoint)" as="document-node()?"/>

	<xsl:key name="result-by-uri" match="sparql:result" use="sparql:binding[@name = 'uri']/sparql:uri"/>
	<xsl:key name="binding-by-label-pos" match="sparql:binding" use="number(substring-after(@name, 'label'))"/>

	<xsl:template name="title">
		Reports
	</xsl:template>

	<xsl:template name="head"/>

	<xsl:template name="body-onload">
        </xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2>
                            <xsl:call-template name="title"/>
                        </h2>

<!-- <xsl:copy-of select="$query-uris"/> -->
<!-- <xsl:copy-of select="$label-query"/> -->
<!-- <xsl:copy-of select="$property-labels"/> -->
<!-- <xsl:copy-of select="key('binding-by-label-pos', 24, $property-labels)[1]"/> -->

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="create">Create</button>
				</p>
			</form>

			<xsl:variable name="paging-controls">
			    <xsl:call-template name="sort-paging-controls">
				<xsl:with-param name="uri" select="'reports'"/>
				<xsl:with-param name="item-count-param" select="$total-item-count"/>
				<xsl:with-param name="offset-param" select="$offset"/>
				<xsl:with-param name="limit-param" select="$limit"/>
				<xsl:with-param name="order-by-param" select="$order-by"/>
				<xsl:with-param name="desc-param" select="$desc"/>
				<xsl:with-param name="desc-default-param" select="$desc-default"/>
			    </xsl:call-template>
			</xsl:variable>

			<div class="pagination">
			    <xsl:copy-of select="$paging-controls"/>
		        </div>
			
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

			<div class="pagination">
			    <xsl:copy-of select="$paging-controls"/>
		        </div>
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
                            <xsl:variable name="current-uris" select="$query-uris//sparql:result[sparql:binding[@name = 'report']/sparql:uri = current()/sparql:binding[@name = 'report']/sparql:uri]"/>
                            <xsl:if test="$current-uris">
                                <ul>
                                    <xsl:apply-templates select="$current-uris" mode="uri-list-item"/>
                                </ul>
                             </xsl:if>
			</td>
			<td>
				<a href="{sparql:binding[@name = 'endpoint']/sparql:uri}?query={encode-for-uri(sparql:binding[@name = 'queryString']/sparql:literal)}">
                                    <xsl:variable name="endpoint" select="$endpoints//sparql:result[sparql:binding[@name = 'endpoint']/sparql:uri = current()/sparql:binding[@name = 'endpoint']/sparql:uri]"/>
                                    <xsl:choose>
                                        <xsl:when test="$endpoint/sparql:binding[@name = 'title']">
                                            <xsl:value-of select="$endpoint/sparql:binding[@name = 'title']/sparql:literal"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
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

        <xsl:template match="sparql:result[sparql:binding[@name = 'uri']]" mode="uri-list-item">
            <xsl:variable name="uri" select="sparql:binding[@name = 'uri']/sparql:uri" as="xs:anyURI"/>
	    <xsl:variable name="unique-position" select="count(preceding::sparql:result[. is key('result-by-uri', sparql:binding[@name = 'uri']/sparql:uri, $query-uris)[1]])" as="xs:integer"/>
	    <xsl:variable name="lookup-label" as="xs:string?">
		<xsl:if test="$property-labels">
		    <xsl:sequence select="key('binding-by-label-pos', $unique-position, $property-labels)[1]/sparql:literal"/>
		</xsl:if>
	    </xsl:variable>
            <xsl:variable name="uri-label" as="xs:string">
                <xsl:choose>
		    <xsl:when test="$lookup-label">
			<xsl:value-of select="concat(upper-case(substring($lookup-label, 1, 1)), substring($lookup-label, 2))"/>
		    </xsl:when>
                    <xsl:when test="starts-with($uri, '&rdf;')">
                        <xsl:value-of select="concat('rdf:', substring-after($uri, '&rdf;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&rdfs;')">
                        <xsl:value-of select="concat('rdfs:', substring-after($uri, '&rdfs;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&owl;')">
                        <xsl:value-of select="concat('owl:', substring-after($uri, '&owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dc;')">
                        <xsl:value-of select="concat('dc:', substring-after($uri, '&dc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&foaf;')">
                        <xsl:value-of select="concat('foaf:', substring-after($uri, '&foaf;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&skos;')">
                        <xsl:value-of select="concat('skos:', substring-after($uri, '&skos;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&sioc;')">
                        <xsl:value-of select="concat('sioc:', substring-after($uri, '&sioc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&geo;')">
                        <xsl:value-of select="concat('geo:', substring-after($uri, '&geo;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&category;')">
                        <xsl:value-of select="concat('category:', substring-after($uri, '&category;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia;')">
                        <xsl:value-of select="concat('dbpedia:', substring-after($uri, '&dbpedia;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia-owl;')">
                        <xsl:value-of select="concat('dbpedia-owl:', substring-after($uri, '&dbpedia-owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpprop;')">
                        <xsl:value-of select="concat('dbpprop:', substring-after($uri, '&dbpprop;'))"/>
                    </xsl:when>
		    <xsl:otherwise>
                        <xsl:value-of select="$uri"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <li>
		<a href="{$uri}">
                    <xsl:value-of select="$uri-label"/>
                </a>
		<!-- <xsl:value-of select="$unique-position"/> -->
            </li>
        </xsl:template>

</xsl:stylesheet>