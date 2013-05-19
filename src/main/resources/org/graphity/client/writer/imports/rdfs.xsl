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
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
exclude-result-prefixes="#all">

    <xsl:template match="rdfs:label | rdfs:comment | rdfs:seeAlso" mode="gc:PropertyListMode"/>

    <xsl:template match="@rdf:about[../rdfs:label[lang($lang)]] | @rdf:nodeID[../rdfs:label[lang($lang)]]" mode="gc:LabelMode" priority="3">
	<xsl:variable name="label" select="../rdfs:label[lang($lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../rdfs:label[not(@xml:lang)]] | @rdf:nodeID[../rdfs:label[not(@xml:lang)]]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="../rdfs:label[not(@xml:lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../rdfs:label] | @rdf:about[../@rdfs:label] | @rdf:nodeID[../rdfs:label] | @rdf:nodeID[../@rdfs:label]" mode="gc:LabelMode">
	<xsl:variable name="label" select="(../rdfs:label | ../@rdfs:label)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../rdfs:comment[lang($lang)]] | @rdf:nodeID[../rdfs:comment[lang($lang)]]" mode="gc:DescriptionMode" priority="1">
	<p>
	    <xsl:value-of select="substring(../rdfs:comment[lang($lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="@rdf:about[../rdfs:comment[not(@xml:lang)]] | @rdf:nodeID[../rdfs:comment[not(@xml:lang)]]" mode="gc:DescriptionMode">
	<p>
	    <xsl:value-of select="substring(../rdfs:comment[not(@xml:lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="rdfs:seeAlso" mode="gc:SidebarNavMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	
	<div class="well sidebar-nav">
	    <h2 class="nav-header">
		<xsl:apply-templates select="."/>
	    </h2>

	    <ul class="nav nav-pills nav-stacked">
		<xsl:for-each-group select="key('predicates', $this)" group-by="@rdf:resource">
		    <xsl:sort select="gc:label(@rdf:resource, /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:apply-templates select="current-group()[1]/@rdf:resource" mode="gc:SidebarNavMode"/>
		</xsl:for-each-group>
	    </ul>
	</div>
    </xsl:template>

    <xsl:template match="rdfs:seeAlso/@rdf:resource" mode="gc:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="."/>
	</li>
    </xsl:template>

</xsl:stylesheet>