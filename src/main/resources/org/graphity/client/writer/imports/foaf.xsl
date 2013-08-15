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

    <xsl:template match="foaf:homepage/@rdf:resource | foaf:workplaceHomepage/@rdf:resource | foaf:schoolHomepage/@rdf:resource | foaf:account/@rdf:resource">
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

    <xsl:template match="foaf:mbox/@rdf:resource">
	<a href="{.}">
	    <xsl:value-of select="substring-after(., 'mailto:')"/>
	</a>
    </xsl:template>

    <xsl:template match="foaf:phone/@rdf:resource">
	<a href="{.}">
	    <xsl:value-of select="substring-after(., 'tel:')"/>
	</a>
    </xsl:template>

    <xsl:template match="foaf:img | foaf:depiction | foaf:logo" mode="gc:PropertyListMode" priority="1"/>

    <xsl:template match="@rdf:about[../foaf:img/@rdf:resource]" mode="gc:ImageMode" priority="3">
	<a href="{.}">
	    <img src="{../foaf:img/@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:nodeID[../foaf:img/@rdf:resource]" mode="gc:ImageMode" priority="3">
	<img src="{../foaf:img/@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:depiction/@rdf:resource]" mode="gc:ImageMode" priority="2">
	<a href="{.}">
	    <img src="{../foaf:depiction/@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:nodeID[../foaf:depiction/@rdf:resource]" mode="gc:ImageMode" priority="2">
	<img src="{../foaf:depiction/@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:logo/@rdf:resource]" mode="gc:ImageMode"  priority="1">
	<a href="{.}">
	    <img src="{../foaf:logo/@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:nodeID[../foaf:logo/@rdf:resource]" mode="gc:ImageMode"  priority="1">
	<img src="{../foaf:logo/@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:img/@rdf:resource] | @rdf:nodeID[../foaf:img/@rdf:resource] | @rdf:about[../foaf:depiction/@rdf:resource] | @rdf:nodeID[../foaf:depiction/@rdf:resource] | @rdf:about[../foaf:thumbnail/@rdf:resource] | @rdf:nodeID[../foaf:thumbnail/@rdf:resource] | @rdf:about[../foaf:logo/@rdf:resource] | @rdf:nodeID[../foaf:logo/@rdf:resource]" mode="gc:ParaImageMode">
	<p>
	    <xsl:apply-templates select="." mode="gc:ImageMode"/>
	</p>
    </xsl:template>
    
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource">
	<a href="{.}">
	    <img src="{.}">
		<xsl:attribute name="alt"><xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <!--
    <xsl:template match="foaf:img/@rdf:resource[../../@rdf:nodeID] | foaf:depiction/@rdf:resource[../../@rdf:nodeID] | foaf:thumbnail/@rdf:resource[../../@rdf:nodeID] | foaf:logo/@rdf:resource[../../@rdf:nodeID]">
	<img src="{.}">
	    <xsl:attribute name="alt"><xsl:apply-templates select="../../@rdf:nodeID" mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>
    -->

    <xsl:template match="@rdf:about[../foaf:nick[lang($lang)]] | @rdf:nodeID[../foaf:nick[lang($lang)]]" mode="gc:LabelMode" priority="7">
	<xsl:value-of select="../foaf:nick[lang($lang)][1]"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:name[lang($lang)]] | @rdf:nodeID[../foaf:name[lang($lang)]]" mode="gc:LabelMode" priority="6">
	<xsl:value-of select="../foaf:name[lang($lang)][1]"/>
    </xsl:template>
    
    <xsl:template match="@rdf:about[../foaf:nick[not(@xml:lang)]] | @rdf:nodeID[../foaf:nick[not(@xml:lang)]]" mode="gc:LabelMode" priority="5">
	<xsl:value-of select="../foaf:nick[not(@xml:lang)][1]"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:name[not(@xml:lang)]] | @rdf:nodeID[../foaf:name[not(@xml:lang)]]" mode="gc:LabelMode" priority="4">
	<xsl:value-of select="../foaf:name[not(@xml:lang)][1]"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:nick] | @rdf:about[../@foaf:nick] | @rdf:nodeID[../foaf:nick] | @rdf:nodeID[../@foaf:nick]" mode="gc:LabelMode" priority="3">
	<xsl:variable name="label" select="(../foaf:nick | ../@foaf:nick)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:firstName and ../foaf:lastName] | @rdf:nodeID[../foaf:firstName and ../foaf:lastName]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="concat(../foaf:firstName[1], ' ', ../foaf:lastName[1])"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:givenName and ../foaf:familyName] | @rdf:nodeID[../foaf:givenName and ../foaf:familyName]" mode="gc:LabelMode" priority="1">
	<xsl:variable name="label" select="concat(../foaf:givenName[1], ' ', ../foaf:familyName[1])"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[../foaf:name] | @rdf:about[../@foaf:name] | @rdf:nodeID[../foaf:name] | @rdf:nodeID[../@foaf:name]" mode="gc:LabelMode">
	<xsl:variable name="label" select="(../foaf:name | ../@foaf:name)[1]"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="gc:InputMode">
	<input type="text" name="ol" id="{generate-id(..)}" value="{.}"/><br/>
	<input type="file" name="ol" id="{generate-id(..)}"/><br/>
	<img src="{.}" alt=""/>
    </xsl:template>

</xsl:stylesheet>