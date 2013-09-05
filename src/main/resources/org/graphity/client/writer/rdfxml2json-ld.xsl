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
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY gc "http://client.graphity.org/ontology#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:gc="&gc;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:date="http://exslt.org/dates-and-times"
exclude-result-prefixes="xs">

	<xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/ld+json"/>
	<xsl:strip-space elements="*"/>

	<xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
	<xsl:key name="properties" match="*[@rdf:about or @rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>

	<xsl:template match="/">
	    <xsl:apply-templates mode="gc:JSONLDMode"/>
	</xsl:template>
	
	<xsl:template match="rdf:RDF" mode="gc:JSONLDMode">
[
	    <xsl:apply-templates mode="gc:JSONLDMode"/>	    
]
	</xsl:template>

	<!-- subject -->
	<xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:JSONLDMode">
	{
	    <xsl:if test="@rdf:about">
		<xsl:apply-templates select="@rdf:about" mode="gc:JSONLDMode"/>,
	    </xsl:if>

	    <xsl:apply-templates select="." mode="PropertyListMode"/>
	}
	
	    <xsl:if test="position() != last()">,
	    </xsl:if>
	</xsl:template>

	<xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="PropertyListMode">
	    <xsl:for-each-group select="*" group-by="concat(namespace-uri(), local-name())">
		<xsl:choose>
		    <xsl:when test="current-grouping-key() = '&rdf;type'">"@type"</xsl:when>
		    <xsl:otherwise>
			"<xsl:value-of select="current-grouping-key()"/>"
		    </xsl:otherwise>
		</xsl:choose>
		:
		[
		<xsl:apply-templates select="current-group()" mode="gc:JSONLDMode"/>
		]
		
		<xsl:if test="position() != last()">,
		</xsl:if>
	    </xsl:for-each-group>
	</xsl:template>
	
	<!-- property -->
	<xsl:template match="*[@rdf:about or @rdf:nodeID]/rdf:type" mode="gc:JSONLDMode" priority="1">
	    "<xsl:value-of select="@rdf:resource"/>"
	    
	    <xsl:if test="position() != last()">,
	    </xsl:if>
	</xsl:template>

	<xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:JSONLDMode">
	    {
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:JSONLDMode"/>
	    }
	    <xsl:if test="position() != last()">,
	    </xsl:if>
	</xsl:template>
	
	<xsl:template match="text()[. = 'true' or . = 'false'][../@rdf:datatype = '&xsd;boolean'] | text()[../@rdf:datatype = '&xsd;integer'] | text()[../@rdf:datatype = '&xsd;double']" mode="gc:JSONLDMode" priority="1">
	    "@value": <xsl:value-of select="."/>,
	    <xsl:apply-templates select="../@rdf:datatype" mode="gc:JSONLDMode"/>
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;string']" mode="gc:JSONLDMode" priority="1">
	    "@value": "<xsl:value-of select="."/>"
	</xsl:template>

	<xsl:template match="text()" mode="gc:JSONLDMode">
	    "@value": "<xsl:value-of select="."/>"
	    <xsl:if test="../@rdf:datatype">
		, <xsl:apply-templates select="../@rdf:datatype" mode="gc:JSONLDMode"/>
	    </xsl:if>
	    <xsl:if test="../@xml:lang">
		, <xsl:apply-templates select="../@xml:lang" mode="gc:JSONLDMode"/>
	    </xsl:if>
	</xsl:template>

	<xsl:template match="@rdf:about | @rdf:resource" mode="gc:JSONLDMode">
	    "@id": "<xsl:value-of select="."/>"
	</xsl:template>

	<xsl:template match="@rdf:nodeID" mode="gc:JSONLDMode"/>
	
	<xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:JSONLDMode">
	    <xsl:apply-templates select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)]" mode="PropertyListMode"/>
	</xsl:template>

	<xsl:template match="@rdf:datatype" mode="gc:JSONLDMode">
	    "@type": "<xsl:value-of select="."/>"
	</xsl:template>

	<xsl:template match="@xml:lang" mode="gc:JSONLDMode">
	    "@language": "<xsl:value-of select="."/>"
	</xsl:template>
	
</xsl:stylesheet>