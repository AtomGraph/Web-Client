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
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:grddl="http://www.w3.org/2003/g/data-view#"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:sml="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
exclude-result-prefixes="xsl xs grddl rdf rdfs sml"
>

    <!-- Transforms SpreadsheetML documents into RDF/XML descriptions using established vocabularies -->

    <xsl:output method="xml" indent="yes" encoding="UTF-8" media-type="application/rdf+xml"/>
    
    <xsl:template match="/">
	<rdf:RDF>
	    <xsl:apply-templates/>
	</rdf:RDF>
    </xsl:template>

    <xsl:template match="sml:workbook">
	
    </xsl:template>

    <xsl:template match="sml:worksheet">
	<xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="sml:sheetData">
	<xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="sml:row">
	<rdf:Description>
	    <xsl:apply-templates/>
	</rdf:Description>
    </xsl:template>

    <xsl:template match="sml:c">
	<xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="sml:v">
	<rdf:value>
	    <xsl:apply-templates/>
	</rdf:value>
    </xsl:template>

    <!-- ignore other elements, otherwise they will produce unwanted text nodes -->
    <xsl:template match="*"/>

</xsl:stylesheet>