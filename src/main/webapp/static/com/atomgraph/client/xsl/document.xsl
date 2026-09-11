<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2019 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:geo="&geo;"
xmlns:foaf="&foaf;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <!-- GRAPH  -->
    
    <xsl:template match="rdf:RDF" mode="ac:Graph">
        <xsl:apply-templates select="." mode="ac:SVG">
            <xsl:with-param name="width" select="'100%'"/>
            <xsl:with-param name="step-count" select="20"/>
            <xsl:with-param name="spring-length" select="150" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- DEFAULT  -->
    
    <xsl:template match="rdf:RDF">
        <xsl:apply-templates>
            <xsl:sort select="ac:label(.)"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <!-- FORM  -->

    <xsl:template match="rdf:RDF" mode="ac:ResourceForm">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <!-- the action must carry the browsed URI: a bare relative '?_method=PUT' would replace the whole query string and drop the ?uri= the proxy routes on -->
        <xsl:param name="action" select="ac:build-uri((), map{ 'uri': string(ac:absolute-path(base-uri())), '_method': 'PUT' })" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'resource-form'" as="xs:string?"/>
        <xsl:param name="button-class" select="'ac-btn in-primary ap-solid sz-md'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>

        <form method="{$method}" action="{$action}">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$accept-charset">
                <xsl:attribute name="accept-charset" select="$accept-charset"/>
            </xsl:if>
            <xsl:if test="$enctype">
                <xsl:attribute name="enctype" select="$enctype"/>
            </xsl:if>

            <xsl:comment>This form uses RDF/POST encoding: https://atomgraph.github.io/RDF-POST/</xsl:comment>
            <xsl:call-template name="xhtml:Input">
                <xsl:with-param name="name" select="'rdf'"/>
                <xsl:with-param name="type" select="'hidden'"/>
            </xsl:call-template>
            
            <xsl:apply-templates select="." mode="ac:Legend"/>

            <xsl:apply-templates mode="#current">
                <xsl:sort select="ac:label(.)"/>
            </xsl:apply-templates>

            <xsl:apply-templates select="." mode="ac:FormActions">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
        </form>
    </xsl:template>
    
    <!-- LEGEND -->

    <xsl:template match="rdf:RDF" mode="ac:Legend" priority="2">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <!-- FORM ACTIONS -->
    
    <xsl:template match="rdf:RDF" mode="ac:FormActions">
        <xsl:param name="button-class" select="'ac-btn in-primary ap-solid sz-md'" as="xs:string?"/>

        <div class="form-actions">
            <button type="submit" class="{$button-class}">
                <span class="msi sm" aria-hidden="true">save</span>
                <xsl:apply-templates select="key('resources', 'save', ac:translations())" mode="ac:label"/>
            </button>
        </div>
    </xsl:template>
    
</xsl:stylesheet>