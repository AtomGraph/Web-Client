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
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
exclude-result-prefixes="#all">

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

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<a href="{$base-uri}{g:query-string($this, $endpoint-uri, (), (), (), (), $lang, ())}" title="{$this}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</a>
    </xsl:template>

    <!--
    <xsl:template match="foaf:Image/@rdf:about | *[rdf:type/@rdf:resource = '&foaf;Image']/@rdf:about | foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="g:HeaderMode">
	<a href="{$base-uri}{g:query-string(., $endpoint-uri, (), (), (), (), $lang, ())}">
	    <img src="{.}" alt="{g:label(., /, $lang)}"/>
	</a>
    </xsl:template>

    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="g:ImageMode">
	<xsl:if test="../../@rdf:about">
	    <a href="{$base-uri}{g:query-string(., $endpoint-uri, (), (), (), (), $lang, ())}">
		<img src="{.}" alt="{g:label(../../@rdf:about, /, $lang)}" />
	    </a>
	</xsl:if>
	<xsl:if test="../../@rdf:nodeID">
	    <img src="{.}" alt="{g:label(../../@rdf:nodeID, /, $lang)}" />
	</xsl:if>	    
    </xsl:template>
    -->

</xsl:stylesheet>