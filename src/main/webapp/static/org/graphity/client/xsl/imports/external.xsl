<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@graphity.org>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java       "http://xml.apache.org/xalan/java/">
    <!ENTITY gc         "http://graphity.org/gc#">
    <!ENTITY gp         "http://graphity.org/gp#">
    <!ENTITY rdf        "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs       "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl        "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd        "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql     "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sp         "http://spinrdf.org/sp#">
    <!ENTITY dct        "http://purl.org/dc/terms/">
    <!ENTITY foaf       "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc       "http://rdfs.org/sioc/ns#">
    <!ENTITY skos       "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:param name="gp:baseUri" as="xs:anyURI"/>
    
    <xsl:template match="@rdf:about[. = $gc:uri]" mode="gc:HeaderMode" priority="1">
	<!--
        <div class="btn-group pull-right">
	    <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>
	</div>
        -->

	<h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:about[not(starts-with(., $gp:baseUri))]" mode="gc:HeaderMode">
	<div class="btn-group pull-right">
	    <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>
	</div>

	<h2>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h2>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:MediaTypeSelectMode">
	<xsl:if test="not(starts-with(., $gp:baseUri))">
	    <a href="{.}" class="btn">Source</a>
	</xsl:if>
	<a href="{$gp:baseUri}?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
	<a href="{$gp:baseUri}?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
    </xsl:template>
	    
    <!-- subject resource -->
    <xsl:template match="@rdf:about[not(starts-with(., $gp:baseUri))]" mode="gc:InlineMode">
	<a href="{$gp:baseUri}{gc:query-string(., (), (), (), (), ())}" title="{.}">
	    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(starts-with(concat(namespace-uri(), local-name()), $gp:baseUri))]" mode="gc:InlineMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
	<a href="{$gp:baseUri}{gc:query-string($this, (), (), (), (), ())}" title="{$this}">
	    <xsl:apply-templates select="." mode="gc:PropertyLabelMode"/>
	</a>
    </xsl:template>

    <!-- object -->
    <xsl:template match="@rdf:resource[not(starts-with(., $gp:baseUri))] | sparql:uri[not(starts-with(., $gp:baseUri))]" mode="gc:InlineMode">
	<a href="{$gp:baseUri}{gc:query-string(., (), (), (), (), ())}" title="{.}">
	    <xsl:apply-templates select="." mode="gc:ObjectLabelMode"/>
	</a>
    </xsl:template>

    <xsl:template match="sparql:uri[not(starts-with(., $gp:baseUri))]" mode="gc:TableMode">
	<a href="{$gp:baseUri}{gc:query-string(., (), (), (), (), ())}" title="{.}">
	    <xsl:value-of select="."/>
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
	    <xsl:if test="$order-by">orderBy=<xsl:value-of select="$order-by"/>&amp;</xsl:if>
	    <xsl:if test="$desc">desc&amp;</xsl:if>
	    <xsl:if test="$mode">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
	</xsl:variable>
	
	<xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
    </xsl:function>

</xsl:stylesheet>