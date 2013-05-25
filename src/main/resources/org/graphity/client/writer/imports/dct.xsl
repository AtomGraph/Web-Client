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
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY dct "http://purl.org/dc/terms/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:dct="&dct;"
exclude-result-prefixes="#all">

    <xsl:template match="dct:title | dct:description | dct:subject" mode="gc:PropertyListMode"/>

    <xsl:template match="@rdf:about[../dct:title[lang($lang)]] | @rdf:nodeID[../dct:title[lang($lang)]]" mode="gc:LabelMode" priority="3">
	<xsl:variable name="label" select="../dct:title[lang($lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../dct:title[not(@xml:lang)]] | @rdf:nodeID[../dct:title[not(@xml:lang)]]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="../dct:title[not(@xml:lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../dct:title] | @rdf:about[../@dct:title] | @rdf:nodeID[../dct:title] | @rdf:nodeID[../@dct:title]" mode="gc:LabelMode">
	<xsl:variable name="label" select="(../dct:title | ../@dct:title)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../dct:description[lang($lang)]] | @rdf:nodeID[../dct:description[lang($lang)]]" mode="gc:DescriptionMode" priority="1">
	<p>
	    <xsl:value-of select="substring(../dct:description[lang($lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="@rdf:about[../dct:description[not(@xml:lang)]] | @rdf:nodeID[../dct:description[not(@xml:lang)]]" mode="gc:DescriptionMode">
	<p>
	    <xsl:value-of select="substring(../dct:description[not(@xml:lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="dct:subject" mode="gc:SidebarNavMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	
	<div class="well sidebar-nav">
	    <h2 class="nav-header">
		<xsl:apply-templates select="."/>
	    </h2>
		
	    <!-- TO-DO: fix for a single resource! -->
	    <ul class="nav nav-pills nav-stacked">
		<xsl:for-each-group select="key('predicates', $this)" group-by="@rdf:resource">
		    <xsl:sort select="gc:label(@rdf:resource, /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:apply-templates select="current-group()[1]/@rdf:resource" mode="gc:SidebarNavMode"/>
		</xsl:for-each-group>
	    </ul>
	</div>
    </xsl:template>

    <xsl:template match="dct:subject/@rdf:resource" mode="gc:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="."/>
	</li>
    </xsl:template>

</xsl:stylesheet>