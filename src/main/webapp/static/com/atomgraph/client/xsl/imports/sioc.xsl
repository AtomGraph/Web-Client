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
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:sioc="&sioc;"
exclude-result-prefixes="#all">
    
    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>

    <xsl:template match="*[$ldt:lang][sioc:name[lang($ldt:lang)]/text()]" mode="ac:label" priority="1">
        <xsl:sequence select="sioc:name[lang($ldt:lang)]/text()"/>
    </xsl:template>
    
    <xsl:template match="*[sioc:name/text()]" mode="ac:label">
        <xsl:sequence select="sioc:name/text()"/>
    </xsl:template>

    <xsl:template match="sioc:email/@rdf:resource">
        <a href="{.}">
            <xsl:sequence select="substring-after(., 'mailto:')"/>
        </a>
    </xsl:template>

    <xsl:template match="sioc:has_container | sioc:has_parent | sioc:has_space" mode="ac:TablePredicate"/>

</xsl:stylesheet>