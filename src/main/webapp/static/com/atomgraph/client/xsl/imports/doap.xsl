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
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY doap   "http://usefulinc.com/ns/doap#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:doap="&doap;"
exclude-result-prefixes="#all">
    
    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>

    <xsl:template match="*[$ldt:lang][doap:name[lang($ldt:lang)]/text()]" mode="ac:label" priority="1">
        <xsl:sequence select="doap:name[lang($ldt:lang)]/text()"/>
    </xsl:template>

    <xsl:template match="*[doap:name/text()]" mode="ac:label">
        <xsl:sequence select="doap:name/text()"/>
    </xsl:template>

   <xsl:template match="*[$ldt:lang][doap:description[lang($ldt:lang)]/text()]" mode="ac:description" priority="1">
        <xsl:sequence select="doap:description[lang($ldt:lang)]/text()"/>
    </xsl:template>
    
    <xsl:template match="*[doap:description/text()]" mode="ac:description">
        <xsl:sequence select="doap:description/text()"/>
    </xsl:template>

    <xsl:template match="doap:homepage/@rdf:resource | doap:browse/@rdf:resource | doap:location/@rdf:resource | doap:file-release/@rdf:resource">
        <a href="{.}">
            <xsl:sequence select="."/>
        </a>
    </xsl:template>

</xsl:stylesheet>