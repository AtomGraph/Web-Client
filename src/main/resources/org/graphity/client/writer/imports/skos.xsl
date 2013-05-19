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
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:skos="&skos;"
exclude-result-prefixes="#all">

    <xsl:template match="skos:prefLabel" mode="gc:PropertyListMode"/>

    <xsl:template match="@rdf:about[../skos:prefLabel[lang($lang)]] | @rdf:nodeID[../skos:prefLabel[lang($lang)]]" mode="gc:LabelMode" priority="3">
	<xsl:variable name="label" select="../skos:prefLabel[lang($lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../skos:prefLabel[not(@xml:lang)]] | @rdf:nodeID[../skos:prefLabel[not(@xml:lang)]]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="../skos:prefLabel[not(@xml:lang)][1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../skos:prefLabel] | @rdf:about[../@skos:prefLabel] | @rdf:nodeID[../skos:prefLabel] | @rdf:nodeID[../@skos:prefLabel]" mode="gc:LabelMode">
	<xsl:variable name="label" select="(../skos:prefLabel | ../@skos:prefLabel)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

</xsl:stylesheet>