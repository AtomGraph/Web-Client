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
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY gp     "http://graphity.org/gp#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:gp="&gp;"
exclude-result-prefixes="#all">

    <xsl:template match="gc:mode[position() &gt; 1]" mode="gc:EditMode"/>
    
    <xsl:template match="gc:mode/@rdf:resource" mode="gc:EditMode">
        <xsl:variable name="modes" select="key('resources-by-type', '&gc;Mode', $gp:ontModel)" as="element()*"/>
        <select name="ou" id="{generate-id(..)}" multiple="multiple" size="{count($modes)}">
            <xsl:apply-templates select="$modes" mode="gc:OptionMode">
                <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                <xsl:with-param name="selected" select="../../gc:mode/@rdf:resource"/>
            </xsl:apply-templates>
        </select>
    </xsl:template>

    <xsl:template match="gc:defaultMode/@rdf:resource" mode="gc:EditMode">
        <xsl:variable name="modes" select="key('resources-by-type', '&gc;Mode', $gp:ontModel)" as="element()*"/>
        <select name="ou" id="{generate-id(..)}">
            <xsl:apply-templates select="$modes" mode="gc:OptionMode">
                <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                <xsl:with-param name="selected" select="../@rdf:resource"/>
            </xsl:apply-templates>
        </select>
    </xsl:template>

</xsl:stylesheet>