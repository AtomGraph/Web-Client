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
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
exclude-result-prefixes="#all">

    <xsl:template match="gc:*" mode="gc:TablePredicateMode"/>
    
    <!--
    <xsl:template match="gc:supportedMode[position() &gt; 1]" mode="bs2:EditMode"/>
    
    <xsl:template match="gc:supportedMode/@rdf:*[$gc:sitemap]" mode="bs2:EditMode">
        <xsl:variable name="modes" select="key('resources-by-type', '&gc;Mode', $gc:sitemap)" as="element()*"/>
        <xsl:variable name="template" select="../.." as="element()"/>
        
        <select name="ou" id="{generate-id(.)}" multiple="multiple" size="{count($modes)}">
            <xsl:for-each select="$modes">
                <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                <xsl:apply-templates select="." mode="gc:OptionMode">
                    <xsl:with-param name="selected" select="(@rdf:about, @rdf:nodeID) = $template/gc:supportedMode/@rdf:resource"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </select>
    </xsl:template>

    <xsl:template match="gc:defaultMode/@rdf:*[$gc:sitemap]" mode="bs2:EditMode">
        <xsl:param name="modes" select="key('resources-by-type', '&gc;Mode', $gc:sitemap)" as="element()*"/>
        <xsl:variable name="template" select="../.." as="element()"/>
        
        <select name="ou" id="{generate-id(.)}">
            <xsl:for-each select="$modes">
                <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                <xsl:apply-templates select="." mode="gc:OptionMode">
                    <xsl:with-param name="selected" select="(@rdf:about, @rdf:nodeID) = $template/gc:defaultMode/@rdf:resource"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </select>
    </xsl:template>
    -->
    
</xsl:stylesheet>