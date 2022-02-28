<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:rdf="&rdf;"
xmlns:sioc="&sioc;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
exclude-result-prefixes="#all">

    <xsl:template match="sioc:content/xhtml:*">
        <xsl:copy-of select="." copy-namespaces="no"/>
    </xsl:template>
    
    <xsl:template match="sioc:content/text()" mode="bs2:FormControl">
        <xsl:param name="name" select="'ol'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="style" as="xs:string?"/>
        <xsl:param name="value" select="." as="xs:string?"/>
        <xsl:param name="rows" select="10" as="xs:integer?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        
        <textarea name="{$name}">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$style">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
            <xsl:if test="$rows">
                <xsl:attribute name="rows" select="$rows"/>
            </xsl:if>

            <xsl:sequence select="$value"/>
        </textarea>
        
        <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
            <xsl:with-param name="type-label" select="$type-label"/>
        </xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>