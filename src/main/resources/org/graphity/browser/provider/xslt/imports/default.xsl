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
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:url="&java;java.net.URLDecoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:skos="&skos;"
xmlns:list="&list;"
exclude-result-prefixes="xhtml xs g url rdf rdfs xsd sparql dc dct foaf skos list">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/> <!-- concat(namespace-uri(.), local-name(.)) -->
    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-range" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:range/@rdf:resource"/>

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource | sparql:uri">
	<a href="{$base-uri}{g:query-string(., $endpoint-uri, (), (), (), (), $lang, ())}" title="{.}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:about[starts-with(., $base-uri)] | @rdf:resource[starts-with(., $base-uri)] | sparql:uri[starts-with(., $base-uri)]">
	<a href="{.}{g:query-string((), (), (), (), (), (), $lang, ())}" title="{.}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:nodeID">
	<xsl:value-of select="g:label(., /, $lang)"/>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/* | *[@rdf:resource or @rdf:nodeID]">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<a href="{$base-uri}{g:query-string($this, $endpoint-uri, (), (), (), (), $lang, ())}" title="{$this}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:apply-templates select="key('resources', .)"/>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | sparql:literal[@datatype]">
	<span title="{../@rdf:datatype | @datatype}">
	    <xsl:value-of select="."/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:resource" mode="g:TypeMode">
	<a href="{$base-uri}{g:query-string(., $endpoint-uri, (), (), (), (), $lang, ())}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>
	
</xsl:stylesheet>