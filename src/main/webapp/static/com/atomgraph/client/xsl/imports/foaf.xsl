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
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="3.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="#all">

    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>

    <xsl:template match="foaf:page/@rdf:resource | foaf:homepage/@rdf:resource | foaf:workplaceHomepage/@rdf:resource | foaf:schoolHomepage/@rdf:resource | foaf:account/@rdf:resource">
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

    <xsl:template match="foaf:mbox/@rdf:resource">
        <a href="{.}">
            <xsl:sequence select="substring-after(., 'mailto:')"/>
        </a>
    </xsl:template>

    <xsl:template match="foaf:phone/@rdf:resource">
        <a href="{.}">
            <xsl:sequence select="substring-after(., 'tel:')"/>
        </a>
    </xsl:template>

    <xsl:template match="foaf:img/@rdf:resource | foaf:logo/@rdf:resource | foaf:depiction/@rdf:resource">
        <a href="{.}">
            <img src="{.}">
                <xsl:attribute name="alt">
                    <xsl:value-of>
                        <xsl:apply-templates select="." mode="ac:object-label"/>
                    </xsl:value-of>
                </xsl:attribute>
            </img>
        </a>
    </xsl:template>
    
    <xsl:template match="*[foaf:img/@rdf:resource]" mode="ac:image" priority="2">
        <xsl:sequence select="foaf:img/@rdf:resource"/>
    </xsl:template>

    <xsl:template match="*[foaf:logo/@rdf:resource]" mode="ac:image" priority="1">
        <xsl:sequence select="foaf:logo/@rdf:resource"/>
    </xsl:template>

    <xsl:template match="*[foaf:depiction/@rdf:resource]" mode="ac:image">
        <xsl:sequence select="foaf:depiction/@rdf:resource"/>
    </xsl:template>

    <xsl:template match="*[foaf:nick/text()]" mode="ac:label" priority="5">
        <xsl:sequence select="foaf:nick/text()"/>
    </xsl:template>

    <xsl:template match="*[$ldt:lang][foaf:name[lang($ldt:lang)]/text()]" mode="ac:label" priority="6">
        <xsl:sequence select="foaf:name[lang($ldt:lang)]/text()"/>
    </xsl:template>
    
    <xsl:template match="*[foaf:name[not(@xml:lang)]/text()]" mode="ac:label" priority="4">
        <xsl:sequence select="foaf:name[not(@xml:lang)]/text()"/>
    </xsl:template>

    <xsl:template match="*[foaf:firstName/text()][foaf:lastName/text()]" mode="ac:label" priority="3">
        <xsl:sequence select="foaf:firstName/text() || ' ' || foaf:lastName/text()"/>
    </xsl:template>

    <xsl:template match="*[foaf:givenName/text()][foaf:familyName/text()]" mode="ac:label" priority="2">
        <xsl:sequence select="foaf:givenName/text() || ' ' || foaf:familyName/text()"/>
    </xsl:template>

    <xsl:template match="*[foaf:familyName/text()]" mode="ac:label" priority="1">
        <xsl:sequence select="foaf:familyName/text()"/>
    </xsl:template>

    <xsl:template match="*[foaf:lastName/text()]" mode="ac:label" priority="1">
        <xsl:sequence select="foaf:lastName/text()"/>
    </xsl:template>

</xsl:stylesheet>