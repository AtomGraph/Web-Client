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
    <!ENTITY gr "http://purl.org/goodrelations/v1#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:gr="&gr;"
exclude-result-prefixes="#all">

    <xsl:template match="gr:name[lang($lang)]" mode="gc:LabelMode" priority="6">
	<xsl:value-of select="."/>
    </xsl:template>
    
    <xsl:template match="gr:name[not(@xml:lang)]" mode="gc:LabelMode" priority="4">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="gr:name | @gr:name" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="gr:description[lang($lang)]" mode="gc:DescriptionMode" priority="1">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="gr:description[not(@xml:lang)]" mode="gc:DescriptionMode">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>