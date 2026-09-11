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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY dct    "http://purl.org/dc/terms/">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:dct="&dct;"
exclude-result-prefixes="#all">

    <xsl:template match="*[dct:title/text()]" mode="ac:label">
        <xsl:sequence select="ac:preferred-lang(dct:title)"/>
    </xsl:template>

    <xsl:template match="*[dct:description/text()]" mode="ac:description">
        <xsl:sequence select="ac:preferred-lang(dct:description)"/>
    </xsl:template>
    
    <!-- FORM CONTROLS -->

    <!-- a description is multi-line prose whatever its current length -->
    <xsl:template match="dct:description/text()" mode="ac:FormControl">
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:param name="rows" select="3" as="xs:integer"/>

        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="control" as="item()*">
                <textarea name="ol" id="{generate-id()}" rows="{$rows}">
                    <xsl:value-of select="."/>
                </textarea>
            </xsl:with-param>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>