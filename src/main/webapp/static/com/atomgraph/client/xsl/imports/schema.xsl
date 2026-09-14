<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY ac         "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf        "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY schema1    "http://schema.org/">
    <!ENTITY schema2    "https://schema.org/">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:schema1="&schema1;"
xmlns:schema2="&schema2;"
exclude-result-prefixes="#all">

    <!-- schema.org is published under both the http: and https: namespace; the two spellings are the
         same property, so each pair of literals is one candidate set -->
    <xsl:template match="*[(schema1:name | schema2:name)/text()]" mode="ac:label">
        <xsl:sequence select="ac:preferred-lang(schema1:name | schema2:name)"/>
    </xsl:template>

    <xsl:template match="*[(schema1:description | schema2:description)/text()]" mode="ac:description">
        <xsl:sequence select="ac:preferred-lang(schema1:description | schema2:description)"/>
    </xsl:template>

    <xsl:template match="*[schema1:image/@rdf:resource or schema2:image/@rdf:resource]" mode="ac:image" priority="2">
        <xsl:sequence select="(schema1:image/@rdf:resource, schema2:image/@rdf:resource)[1]"/>
    </xsl:template>

    <xsl:template match="*[schema1:logo/@rdf:resource or schema2:logo/@rdf:resource]" mode="ac:image" priority="1">
        <xsl:sequence select="(schema1:logo/@rdf:resource, schema2:logo/@rdf:resource)[1]"/>
    </xsl:template>

    <xsl:template match="*[schema1:thumbnailUrl/@rdf:resource or schema2:thumbnailUrl/@rdf:resource]" mode="ac:image">
        <xsl:sequence select="(schema1:thumbnailUrl/@rdf:resource, schema2:thumbnailUrl/@rdf:resource)[1]"/>
    </xsl:template>

    <!-- the anchor navigates (proxied for external URIs via $href); the image bytes load from the raw URI -->
    <xsl:template match="schema1:image/@rdf:resource | schema2:image/@rdf:resource | schema1:logo/@rdf:resource | schema2:logo/@rdf:resource | schema1:thumbnailUrl/@rdf:resource | schema2:thumbnailUrl/@rdf:resource">
        <xsl:param name="href" select="." as="xs:anyURI"/>
        <xsl:param name="class" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <img src="{.}">
                <xsl:attribute name="alt">
                    <xsl:value-of>
                        <xsl:apply-templates select="." mode="ac:object-label"/>
                    </xsl:value-of>
                </xsl:attribute>
            </img>
        </a>
    </xsl:template>
    
</xsl:stylesheet>