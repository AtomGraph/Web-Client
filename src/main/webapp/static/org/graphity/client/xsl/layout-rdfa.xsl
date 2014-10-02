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
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY gc     "http://client.graphity.org/ontology#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:uuid="java:java.util.UUID"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/MarkUp/DTD/xhtml-rdfa-2.dtd" doctype-public="-//W3C//DTD XHTML+RDFa 1.1//EN" media-type="application/xhtml+xml"/>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ReadMode">
        <div about="{@rdf:about}">
            <xsl:next-match/>
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListReadMode">
        <div about="{@rdf:about}">WTF???
            <xsl:next-match/>
        </div>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:InlineMode">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
	<span title="{$this}" property="{$this}">
	    <xsl:apply-templates select="." mode="gc:PropertyLabelMode"/>
	</span>
    </xsl:template>

    <!-- object resource -->    
    <xsl:template match="@rdf:resource | sparql:uri" mode="gc:InlineMode">
	<a href="{.}" title="{.}" resource="{.}">
            <xsl:apply-templates select="." mode="gc:ObjectLabelMode"/>
	</a>
    </xsl:template>
	
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:InlineMode">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>

	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode" mode="gc:ReadMode">
                    <xsl:with-param name="nested" select="true()"/>
                </xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise>
		<span id="{.}" title="{.}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | sparql:literal[@datatype]" mode="gc:InlineMode">
	<span title="{../@rdf:datatype | @datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="."/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float'] | sparql:literal[@datatype = '&xsd;double']" priority="1" mode="gc:InlineMode">
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-number(., '#####.00')"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1" mode="gc:InlineMode">
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1" mode="gc:InlineMode">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $lang, (), ())"/>
	</span>
    </xsl:template>

</xsl:stylesheet>