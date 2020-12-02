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
    <!ENTITY ac             "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY dbpedia-owl    "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:dbpedia-owl="&dbpedia-owl;"
exclude-result-prefixes="#all">

    <xsl:template match="dbpedia-owl:wikiPageExternalLink/@rdf:resource">
        <a href="{.}">
            <xsl:choose>
                <xsl:when test="starts-with(., 'http://')">
                    <xsl:sequence select="substring-after(., 'http://')"/>
                </xsl:when>
                <xsl:when test="starts-with(., 'https://')">
                    <xsl:sequence select="substring-after(., 'https://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </a>
    </xsl:template>

    <xsl:template match="*[dbpedia-owl:thumbnail/@rdf:resource]" mode="ac:image">
        <xsl:sequence select="dbpedia-owl:thumbnail/@rdf:resource"/>
    </xsl:template>

    <xsl:template match="dbpedia-owl:thumbnail/@rdf:resource">
        <a href="{.}">
            <img src="{.}">
                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="ac:object-label"/></xsl:attribute>
            </img>
        </a>
    </xsl:template>

    <xsl:template match="*[dbpedia-owl:abstract/text()]" mode="ac:description">
        <xsl:sequence select="dbpedia-owl:abstract/text()"/>
    </xsl:template>

</xsl:stylesheet>