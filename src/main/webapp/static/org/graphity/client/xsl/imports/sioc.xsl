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
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:sioc="&sioc;"
exclude-result-prefixes="#all">

    <xsl:template match="sioc:content" mode="gc:PropertyListMode"/>

    <xsl:template match="sioc:name | @sioc:name" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="sioc:content" mode="gc:DescriptionMode">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="sioc:avatar" mode="gc:PropertyListMode" priority="1"/>

    <xsl:template match="sioc:avatar[../@rdf:about][@rdf:resource]" mode="gc:ImageMode">
	<a href="{../@rdf:about}">
	    <img src="{@rdf:resource}">
		<xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="sioc:avatar[../@rdf:nodeID][@rdf:resource]" mode="gc:ImageMode">
	<img src="{sioc:avatar/@rdf:resource}">
	    <xsl:attribute name="alt"><xsl:apply-templates select=".." mode="gc:LabelMode"/></xsl:attribute>
	</img>
    </xsl:template>

    <xsl:template match="sioc:avatar/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <img src="{.}">
		<xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:ObjectLabelMode"/></xsl:attribute>
	    </img>
	</a>
    </xsl:template>

    <xsl:template match="sioc:email/@rdf:resource" mode="gc:InlineMode">
	<a href="{.}">
	    <xsl:value-of select="substring-after(., 'mailto:')"/>
	</a>
    </xsl:template>

    <xsl:template match="sioc:*" mode="gc:InlinePropertyListMode">
        <dl class="pull-left" style="margin: 0; margin-right: 1em">
            <dt>
                <xsl:apply-templates select="." mode="gc:InlineMode"/>
            </dt>
            <dd>
                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:InlineMode"/>
            </dd>
        </dl>
    </xsl:template>

</xsl:stylesheet>