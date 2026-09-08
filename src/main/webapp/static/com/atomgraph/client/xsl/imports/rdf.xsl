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
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:rdf="&rdf;"
exclude-result-prefixes="#all">

    <!-- a rendered type is the design system's Tag in the type color, wherever it lands. This is the
         single pill emitter: every surface that shows a type applies templates into this rule (directly
         or via a list/header wrapper) rather than building its own pill -->
    <xsl:template match="rdf:type/@rdf:resource" priority="1">
        <xsl:param name="href" select="." as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <span title="{.}" class="ldhc-tag em-quiet co-primary sz-sm">
            <xsl:next-match>
                <xsl:with-param name="href" select="$href"/>
                <xsl:with-param name="id" select="$id"/>
                <xsl:with-param name="title" select="$title"/>
                <xsl:with-param name="class" select="$class"/>
                <xsl:with-param name="target" select="$target"/>
            </xsl:next-match>
        </span>
    </xsl:template>
    
</xsl:stylesheet>