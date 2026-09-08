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
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:foaf="&foaf;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

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

    <xsl:template match="*[foaf:name[some $lang in ac:langs() satisfies lang($lang)]/text()]" mode="ac:label" priority="6">
        <xsl:sequence select="(for $lang in ac:langs() return foaf:name[lang($lang)])[1]/text()"/>
    </xsl:template>

    <xsl:template match="*[foaf:name/text()]" mode="ac:label" priority="4">
        <xsl:sequence select="(foaf:name[not(@xml:lang)], foaf:name)[1]/text()"/>
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

    <!-- FORM CONTROLS -->

    <xsl:template match="foaf:mbox/@rdf:resource[starts-with(., 'mailto:')]" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="control" as="item()*">
                <xsl:call-template name="xhtml:Input">
                    <xsl:with-param name="name" select="'ol'"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="value" select="substring-after(., 'mailto:')"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="foaf:phone/@rdf:resource[starts-with(., 'tel:')]" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="control" as="item()*">
                <xsl:call-template name="xhtml:Input">
                    <xsl:with-param name="name" select="'ol'"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="value" select="substring-after(., 'tel:')"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>