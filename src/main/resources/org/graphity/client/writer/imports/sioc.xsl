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
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:sioc="&sioc;"
exclude-result-prefixes="#all">

    <xsl:template match="sioc:content" mode="gc:PropertyListMode"/>

    <xsl:template match="@rdf:about[../sioc:name[lang($lang)]] | @rdf:nodeID[../sioc:name[lang($lang)]]" mode="gc:LabelMode" priority="3">
	<xsl:value-of select="../sioc:name[lang($lang)][1]"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../sioc:name[not(@xml:lang)]] | @rdf:nodeID[../sioc:name[not(@xml:lang)]]" mode="gc:LabelMode" priority="2">
	<xsl:value-of select="../sioc:name[not(@xml:lang)][1]"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../sioc:name] | @rdf:about[../@sioc:name] | @rdf:nodeID[../sioc:name] | @rdf:nodeID[../@sioc:name]" mode="gc:LabelMode">
	<xsl:variable name="label" select="(../sioc:name | ../@sioc:name)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../sioc:content[lang($lang)]] | @rdf:nodeID[../sioc:content[lang($lang)]]" mode="gc:DescriptionMode" priority="1">
	<p>
	    <xsl:value-of select="substring(../sioc:content[lang($lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="@rdf:about[../sioc:content[not(@xml:lang)]] | @rdf:nodeID[../sioc:content[not(@xml:lang)]]" mode="gc:DescriptionMode">
	<p>
	    <xsl:value-of select="substring(../sioc:content[not(@xml:lang)][1], 1, 300)"/>
	</p>
    </xsl:template>

    <xsl:template match="sioc:avatar/@rdf:resource">
	<a href="{.}">
	    <img src="{.}">
		<xsl:attribute name="alt"><xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

</xsl:stylesheet>