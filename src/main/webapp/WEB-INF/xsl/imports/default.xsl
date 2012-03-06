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
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
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
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="xhtml g url rdf rdfs xsd dc dct foaf">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:param name="g:inference" select="true()" as="xs:boolean"/>
    
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource">
	<a href="{$base-uri}?uri={encode-for-uri(.)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="@rdf:about" mode="g:EditMode">
	<input type="hidden" name="su" id="{generate-id()}" value="{.}"/>
    </xsl:template>
	
    <!-- object -->
    <xsl:template match="@rdf:resource" mode="g:EditMode">
	<!--
	<option value="{.}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</option>
	-->
	<input type="text" name="ou" id="{generate-id(..)}" value="{.}"/>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[node() or @rdf:resource or @rdf:nodeID]">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<a href="{$base-uri}?uri={encode-for-uri($this)}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:template match="*[node() or @rdf:resource or @rdf:nodeID]" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<label for="{generate-id()}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</label>
	<input type="hidden" name="pu" value="{$this}"/>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:apply-templates select="key('resources', .)"/>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime']">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $lang, (), ())"/>
    </xsl:template>

    <xsl:template match="text()" mode="g:EditMode">
	<xsl:choose>
	    <xsl:when test="string-length(.) &lt; 20">
		<input type="text" name="ol" id="{generate-id(..)}" value="{.}"/>
	    </xsl:when>
	    <xsl:otherwise>
		<textarea name="ol" id="{generate-id(..)}" cols="{80}" rows="{string-length(.) div 80}">
		    <xsl:value-of select="."/>
		</textarea>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:resource" mode="g:TypeMode">
	<a href="{$base-uri}?uri={encode-for-uri(.)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:function name="g:local-label" as="xs:string?">
	<!-- http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms -->
	<!-- http://iswc2011.semanticweb.org/fileadmin/iswc/Papers/Research_Paper/09/70310161.pdf -->
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>
	<xsl:choose>
	    <xsl:when test="$resource/rdfs:label[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@rdfs:label">
		<xsl:sequence select="($resource/rdfs:label[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@rdfs:label[lang($lang)])[1]"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:nick[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:nick">
		<xsl:sequence select="$resource/foaf:nick[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:nick"/>
	    </xsl:when>
	    <xsl:when test="($resource/dc:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dc:title)[1]">
		<xsl:sequence select="$resource/dc:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dc:title"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:name[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:name">
		<xsl:sequence select="$resource/foaf:name[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:name"/>
	    </xsl:when>
	    <xsl:when test="$resource/dct:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dct:title">
		<xsl:sequence select="$resource/dct:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dct:title"/>
	    </xsl:when>
	    <!-- skos:prefLabel -->
	</xsl:choose>
    </xsl:function>

    <xsl:function name="g:document-uri" as="xs:anyURI">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:choose>
	    <!-- strip trailing fragment identifier (#) -->
	    <xsl:when test="contains($resource-uri, '#')">
		<xsl:value-of select="substring-before($resource-uri, '#')"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:value-of select="$resource-uri"/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:function>

    <xsl:function name="g:label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="local-label" select="g:local-label($resource-uri, $document, $lang)" as="xs:string?"/>

	<xsl:choose>
	    <xsl:when test="$local-label">
		<xsl:sequence select="concat(upper-case(substring($local-label, 1, 1)), substring($local-label, 2))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<!-- <xsl:variable name="imported-label" select="(document($ontologies)/g:local-label($resource-uri, ., $lang))[1]" as="xs:string?"/> -->
		<xsl:variable name="imported-label" select="g:local-label($resource-uri, document(g:document-uri($resource-uri)), $lang)[1]" as="xs:string?"/>
		<xsl:choose>
		    <xsl:when test="$imported-label">
			<xsl:sequence select="concat(upper-case(substring($imported-label, 1, 1)), substring($imported-label, 2))"/>
		    </xsl:when>
		    <xsl:when test="substring-after($resource-uri, '#')">
			<xsl:sequence select="substring-after($resource-uri, '#')"/>
		    </xsl:when>
		    <xsl:when test="contains($resource-uri, '/')">
			<xsl:sequence select="translate(url:decode(tokenize($resource-uri, '/')[last()], 'UTF-8'), '_', ' ')"/>
		    </xsl:when>
		    <xsl:otherwise>
			<xsl:sequence select="$resource-uri"/>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:function>

    <xsl:function name="rdfs:domain" as="xs:anyURI*">
	<xsl:param name="property-uri" as="xs:anyURI+"/>
<xsl:message>$property-uri: <xsl:value-of select="$property-uri"/></xsl:message>
	<xsl:for-each select="$property-uri">
	    <xsl:for-each select="document(g:document-uri($property-uri))">
		<xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
    <xsl:if test="key('resources', $property-uri)/rdfs:domain/@rdf:resource">
	<xsl:message>rdfs:domain: <xsl:value-of select="key('resources', $property-uri, .)/rdfs:domain/@rdf:resource"/></xsl:message>
    </xsl:if>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:inDomainOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<!-- <xsl:for-each select="document($ontologies)"> -->
	<xsl:for-each select="$type-uri">
	    <xsl:for-each select="document(g:document-uri(.))">
		<xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
<!--		
		<xsl:variable name="super-uris" select="rdfs:subClassOf($type-uri)" as="xs:anyURI*"/>
    <xsl:message>$super-uris: <xsl:value-of select="$super-uris"/></xsl:message>
		<xsl:if test="not(empty($super-uris))">
		    <xsl:sequence select="g:inDomainOf($super-uris)"/>
		</xsl:if>
-->

    <xsl:if test="key('resources-by-domain', $type-uri)/@rdf:about">
	<xsl:message>g:inDomainOf: <xsl:value-of select="key('resources-by-domain', $type-uri)/@rdf:about"/></xsl:message>
    </xsl:if>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<xsl:for-each select="$type-uri">
	    <xsl:for-each select="document(g:document-uri(.))">
		<xsl:sequence select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/>
    <xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
	<xsl:message>rdfs:subClassOf: <xsl:value-of select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/></xsl:message>
    </xsl:if>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:superClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<xsl:for-each select="document(g:document-uri($type-uri))">
	    <xsl:sequence select="key('resources-by-subclass', $type-uri)/@rdf:about"/>
<xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
    <xsl:message>g:superClassOf: <xsl:value-of select="key('resources-by-subclass', $type-uri)/@rdf:about"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

</xsl:stylesheet>