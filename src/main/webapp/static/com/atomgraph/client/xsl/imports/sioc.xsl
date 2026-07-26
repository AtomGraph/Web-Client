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
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:sioc="&sioc;"
exclude-result-prefixes="#all">
    
    <xsl:template match="*[sioc:name[some $lang in $ac:langs satisfies lang($lang)]/text()]" mode="ac:label" priority="1">
        <xsl:sequence select="(for $lang in $ac:langs return sioc:name[lang($lang)])[1]/text()"/>
    </xsl:template>

    <xsl:template match="*[sioc:name/text()]" mode="ac:label">
        <xsl:sequence select="(sioc:name[not(@xml:lang)], sioc:name)[1]/text()"/>
    </xsl:template>

    <xsl:template match="sioc:email/@rdf:resource">
        <a href="{.}">
            <xsl:sequence select="substring-after(., 'mailto:')"/>
        </a>
    </xsl:template>

</xsl:stylesheet>