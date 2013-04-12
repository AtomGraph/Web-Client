<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

This program is free software: you can redistribute it and/or modify
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
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:param name="base-uri" as="xs:anyURI"/>

    <xsl:template match="rdf:type/@rdf:resource" priority="1">
	<span title="{.}" class="btn">
	    <xsl:next-match/>
	</span>
    </xsl:template>

    <xsl:template match="@rdf:about[not(starts-with(., $base-uri))]" mode="gc:HeaderMode">
	<xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>

	<h2>
	    <xsl:apply-templates select="."/>
	</h2>
    </xsl:template>
    
    <xsl:template match="@rdf:about[not(starts-with(., $base-uri))]" mode="gc:MediaTypeSelectMode">
	<div class="btn-group pull-right">
	    <a href="{.}" class="btn">Source</a>
	    <a href="{$base-uri}?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
	    <a href="{$base-uri}?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
	</div>
    </xsl:template>

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about[not(starts-with(., $base-uri))] | @rdf:resource[not(starts-with(., $base-uri))] | sparql:uri[not(starts-with(., $base-uri))]">
	<a href="{$base-uri}{gc:query-string(., (), (), (), (), ())}" title="{.}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(starts-with(concat(namespace-uri(), local-name()), $base-uri))]">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	<a href="{$base-uri}{gc:query-string($this, (), (), (), (), ())}" title="{$this}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <xsl:function name="gc:query-string" as="xs:string?">
	<xsl:param name="uri" as="xs:anyURI?"/>
	<xsl:param name="offset" as="xs:integer?"/>
	<xsl:param name="limit" as="xs:integer?"/>
	<xsl:param name="order-by" as="xs:string?"/>
	<xsl:param name="desc" as="xs:boolean?"/>
	<xsl:param name="mode" as="xs:string?"/>
	
	<xsl:variable name="query-string">
	    <xsl:if test="$uri">uri=<xsl:value-of select="encode-for-uri($uri)"/>&amp;</xsl:if>
	    <xsl:if test="$offset">offset=<xsl:value-of select="$offset"/>&amp;</xsl:if>
	    <xsl:if test="$limit">limit=<xsl:value-of select="$limit"/>&amp;</xsl:if>
	    <xsl:if test="$order-by">order-by=<xsl:value-of select="$order-by"/>&amp;</xsl:if>
	    <xsl:if test="$desc">desc&amp;</xsl:if>
	    <xsl:if test="$mode">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
	</xsl:variable>
	
	<xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
    </xsl:function>

</xsl:stylesheet>