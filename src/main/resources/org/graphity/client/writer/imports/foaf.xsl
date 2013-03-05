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
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="#all">

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

    <xsl:template match="foaf:img | foaf:depiction | foaf:thumbnail | foaf:logo" mode="gc:HeaderImageMode" priority="1">
	<p>
	    <xsl:apply-templates select="@rdf:resource"/>
	</p>
    </xsl:template>

    <xsl:template match="foaf:img | foaf:depiction | foaf:thumbnail | foaf:logo" mode="gc:ListImageMode" priority="1">
	<p>
	    <xsl:apply-templates select="@rdf:resource"/>
	</p>
    </xsl:template>

    <!--
    <xsl:template match="foaf:Image/@rdf:about | *[rdf:type/@rdf:resource = '&foaf;Image']/@rdf:about">
	<a href="{.}">
	    <img src="{.}" alt="{gc:label(., /, $lang)}"/>
	</a>
    </xsl:template>
    -->
    
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource">
	<xsl:if test="../../@rdf:about">
	    <a href="{.}"> <!-- {$base-uri}{gc:query-string($lang)} -->
		<img src="{.}">
		    <xsl:attribute name="alt"><xsl:apply-templates select="../../@rdf:about"/></xsl:attribute>
		</img>
	    </a>
	</xsl:if>
	<xsl:if test="../../@rdf:nodeID">
	    <img src="{.}">
		<xsl:attribute name="alt"><xsl:apply-templates select="../../@rdf:nodeID"/></xsl:attribute>
	    </img>
	</xsl:if>	    
    </xsl:template>

    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="gc:EditMode">
	<input type="text" name="ol" id="{generate-id(..)}" value="{.}"/><br/>
	<input type="file" name="ol" id="{generate-id(..)}"/><br/>
	<img src="{.}" alt=""/>
    </xsl:template>

</xsl:stylesheet>