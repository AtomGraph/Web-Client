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
    
    <xsl:template match="gp:uriTemplate/text()" mode="gc:InlineMode">
        <pre>
            <xsl:next-match/>
        </pre>
    </xsl:template>

    <xsl:template match="gp:slug/@rdf:datatype | gp:limit/@rdf:datatype | gp:offset/@rdf:datatype | gp:orderBy/@rdf:datatype " mode="gc:EditMode">
        <xsl:next-match>
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:next-match>
    </xsl:template>

    <!--
    <xsl:template match="gp:slug/text()" mode="gc:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$mode = '&gc;EditMode'"/>
        </xsl:apply-templates>
        <xsl:if test="not($type = 'hidden')">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype" mode="gc:InlineMode"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="help-inline">Literal</span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    -->
    
</xsl:stylesheet>