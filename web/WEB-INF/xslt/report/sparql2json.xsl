<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of Graphity SemanticReports package.
Copyright (C) 2009-2011  Martynas Jusevičius

SemanticReports is free software: you can redistribute it and/or modify
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
<!DOCTYPE uridef[
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sparql="&sparql;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="#all">

    <xsl:template match="sparql:result" mode="binding-element-json">
	<xsl:text>{ 'element' :</xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-binding')</xsl:text>
	<xsl:text>, 'bindingType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="vis-toggle-json">
	<xsl:text>{ 'element' : </xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-toggle')</xsl:text>
	<xsl:text>, 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="vis-fieldset-json">
	<xsl:text>{ 'element' : </xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-controls')</xsl:text>
	<xsl:text>, 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

</xsl:stylesheet>