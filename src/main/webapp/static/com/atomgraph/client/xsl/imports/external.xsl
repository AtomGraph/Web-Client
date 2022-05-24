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
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY srx    "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="3.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:owl="&owl;"
xmlns:srx="&srx;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:svg="http://www.w3.org/2000/svg"
exclude-result-prefixes="#all">

    <xsl:template match="@rdf:about" mode="xhtml:Anchor">
        <xsl:param name="href" select="xs:anyURI('')" as="xs:anyURI"/>
        <xsl:param name="id" select="encode-for-uri(.)" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>
        <xsl:param name="fragment" select="encode-for-uri(.)" as="xs:string?"/>

        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="target" select="$target"/>
            <xsl:with-param name="query-params" select="map{ 'uri': string(.) }"/>
            <xsl:with-param name="fragment" select="$fragment" as="xs:string?"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="@rdf:resource | srx:uri">
        <xsl:param name="href" select="xs:anyURI(ac:build-uri((), let $params := map{ 'uri': string(ac:document-uri(.)) } return if ($ac:mode) then map:merge(($params, map{ 'mode': string($ac:mode) })) else $params) || '#' || encode-for-uri(.))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="target" select="$target"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="@rdf:about | @rdf:resource" mode="svg:Anchor">
        <xsl:param name="href" select="xs:anyURI(ac:build-uri((), let $params := map{ 'uri': string(ac:document-uri(.)) } return if ($ac:mode) then map:merge(($params, map{ 'mode': string($ac:mode) })) else $params) || '#' || encode-for-uri(.))" as="xs:anyURI"/>
        <xsl:param name="id" select="encode-for-uri(.)" as="xs:string?"/>
        <xsl:param name="label" select="if (parent::rdf:Description) then ac:svg-label(..) else ac:svg-object-label(.)" as="xs:string"/>
        <xsl:param name="title" select="$label" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="label" select="$label"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="target" select="$target"/>
        </xsl:next-match>
    </xsl:template>
    
</xsl:stylesheet>
