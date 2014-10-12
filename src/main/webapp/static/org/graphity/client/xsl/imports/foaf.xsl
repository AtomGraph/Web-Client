<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@graphity.org>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY gc "http://graphity.org/gc#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="#all">

    <xsl:template match="foaf:page/@rdf:resource | foaf:homepage/@rdf:resource | foaf:workplaceHomepage/@rdf:resource | foaf:schoolHomepage/@rdf:resource | foaf:account/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <xsl:choose>
		<xsl:when test="starts-with(., 'http://')">
		    <xsl:value-of select="substring-after(., 'http://')"/>
		</xsl:when>
		<xsl:when test="starts-with(., 'https://')">
		    <xsl:value-of select="substring-after(., 'https://')"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:value-of select="."/>
		</xsl:otherwise>
	    </xsl:choose>
	</a>
    </xsl:template>

    <xsl:template match="foaf:mbox/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <xsl:value-of select="substring-after(., 'mailto:')"/>
	</a>
    </xsl:template>

    <xsl:template match="foaf:phone/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <xsl:value-of select="substring-after(., 'tel:')"/>
	</a>
    </xsl:template>

    <xsl:template match="foaf:img | foaf:depiction | foaf:logo" mode="gc:PropertyListMode" priority="1"/>

    <!-- <xsl:template match="foaf:primaryTopic[key('resources', @rdf:about | @rdf:nodeID)] | foaf:isPrimaryTopicOf[key('resources', @rdf:about | @rdf:nodeID)]" mode="gc:PropertyListMode" priority="1"/> -->

    <xsl:template match="foaf:img[../@rdf:about][@rdf:resource]" mode="gc:ImageMode" priority="3">
	<a href="{../@rdf:about}">
	    <img src="{@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="foaf:img[../@rdf:nodeID][@rdf:resource]" mode="gc:ImageMode" priority="3">
	<img src="{@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="foaf:depiction[../@rdf:about][@rdf:resource]" mode="gc:ImageMode" priority="2">
	<a href="{../@rdf:about}">
	    <img src="{@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="foaf:depiction[../@rdf:nodeID][@rdf:resource]" mode="gc:ImageMode" priority="2">
	<img src="{@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="foaf:logo[../@rdf:about][@rdf:resource]" mode="gc:ImageMode" priority="1">
	<a href="{../@rdf:about}">
	    <img src="{@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="foaf:logo[../@rdf:nodeID][@rdf:resource]" mode="gc:ImageMode" priority="1">
	<img src="{@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>
    
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <img src="{.}">
		<xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:ObjectLabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>


    <xsl:template match="foaf:nick" mode="gc:LabelMode" priority="5">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="foaf:name" mode="gc:LabelMode" priority="4">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="foaf:nick | @foaf:nick" mode="gc:LabelMode" priority="3">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="foaf:firstName[../foaf:lastName]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="concat(., ' ', ../foaf:lastName[1])"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="foaf:givenName[../foaf:familyName]" mode="gc:LabelMode" priority="1.5">
	<xsl:variable name="label" select="concat(., ' ', ../foaf:familyName[1])"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="foaf:familyName | @foaf:familyName" mode="gc:LabelMode" priority="1">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="foaf:name | @foaf:name" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <!--
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="gc:EditMode">
	<input type="text" name="ol" id="{generate-id(..)}" value="{.}"/><br/>
	<input type="file" name="ol" id="{generate-id(..)}"/><br/>
	<img src="{.}" alt=""/>
    </xsl:template
    -->

</xsl:stylesheet>