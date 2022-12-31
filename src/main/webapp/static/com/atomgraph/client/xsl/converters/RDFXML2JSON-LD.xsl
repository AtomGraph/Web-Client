<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
]>
<xsl:stylesheet version="3.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:ac="&ac;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:json="http://www.w3.org/2005/xpath-functions"
exclude-result-prefixes="#all"
>

    <!-- 
    
    An XSLT stylesheet transforming Jena's RDF/XML format to JSON-LD.
    Supports @context and prefixed names.
    
    RDF/XML: http://www.w3.org/TR/REC-rdf-syntax/
    JSON-LD: https://www.w3.org/TR/json-ld11/

    -->

    <xsl:output indent="no" omit-xml-declaration="yes" method="xml" encoding="UTF-8" media-type="application/ld+json"/>
    <xsl:strip-space elements="*"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    
    <xsl:template match="/" mode="ac:JSON-LD">
        <xsl:variable name="json-xml" as="element()">
            <xsl:apply-templates mode="#current"/>
        </xsl:variable>
        <xsl:sequence select="xml-to-json($json-xml)"/>
        <!-- <xsl:apply-templates mode="#current"/> -->
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="ac:JSON-LD">
        <json:map>
            <json:array key="@graph">
                <!-- do not process blank nodes that are triple objects-->
                <xsl:apply-templates select="*[@rdf:about or count(key('predicates-by-object', @rdf:nodeID)) &gt; 1]" mode="#current"/>
            </json:array>
        </json:map>
    </xsl:template>

    <!-- resource description -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:JSON-LD">
        <xsl:param name="key" as="xs:string?"/>

        <json:map>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <json:map key="@context">
                <!-- namespace prefixes -->
                <xsl:for-each-group select="*" group-by="prefix-from-QName(node-name())">
                    <json:string key="{current-grouping-key()}"><xsl:value-of select="namespace-uri()"/></json:string>
                </xsl:for-each-group>

                <!-- key to property mappings -->
                <xsl:variable name="safe-properties" as="element()*">
                    <xsl:for-each-group select="*" group-by="local-name()">
                        <xsl:if test="count(distinct-values(current-group()/namespace-uri())) = 1">
                            <xsl:sequence select="."/>
                        </xsl:if>
                    </xsl:for-each-group>
                </xsl:variable>
                <xsl:for-each-group select="$safe-properties" group-by="local-name()">
                    <xsl:apply-templates select="current-group()[1]" mode="ac:JSON-LDContext"/>
                </xsl:for-each-group>
            </json:map>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:variable name="resource" select="."/>
            <xsl:for-each-group select="*" group-by="concat(namespace-uri(), local-name())">
                <xsl:apply-templates select="current-group()[1]" mode="ac:JSON-LDPropertyGroup">
                    <xsl:with-param name="resource" select="$resource"/>
                    <xsl:with-param name="property" select="xs:anyURI(current-grouping-key())"/>
                    <xsl:with-param name="group" select="current-group()"/>
                </xsl:apply-templates>
            </xsl:for-each-group>
        </json:map>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:JSON-LDPropertyGroup">
        <xsl:param name="resource" as="element()"/>
        <xsl:param name="property" as="xs:anyURI"/>
        <xsl:param name="group" as="item()*"/>
        
        <xsl:variable name="key" as="xs:string">
            <xsl:choose>
                <xsl:when test="$property = '&rdf;type'">
                    <xsl:text>@type</xsl:text>
                </xsl:when>
                <xsl:when test="not($resource/*[local-name() = local-name(current())][not(namespace-uri() = namespace-uri(current()))])">
                    <xsl:value-of select="local-name()"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- conflicting namespace-uri()/local-name() - full URI is used -->
                    <xsl:value-of select="$property"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="count($group) &gt; 1">
                <json:array key="{$key}">
                    <xsl:apply-templates select="$group" mode="ac:JSON-LD"/>
                </json:array>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$group" mode="ac:JSON-LD">
                    <xsl:with-param name="key" select="$key"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/rdf:type" mode="ac:JSON-LD" priority="1">
        <xsl:param name="key" as="xs:string?"/>

        <json:string>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <xsl:value-of select="@rdf:resource"/>
        </json:string>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:JSON-LD">
        <xsl:param name="key" as="xs:string?"/>

        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current">
            <xsl:with-param name="key" select="$key"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="text()" mode="ac:JSON-LD">
        <xsl:param name="key" as="xs:string?"/>

        <json:string>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <xsl:value-of select="."/>
        </json:string>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype or ../@xml:lang]" mode="ac:JSON-LD" priority="1">
        <xsl:param name="key" as="xs:string?"/>

        <json:map>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <json:string key="@value"><xsl:value-of select="."/></json:string>
            
            <xsl:apply-templates select="../@rdf:datatype | ../@xml:lang" mode="#current"/>
        </json:map>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="ac:JSON-LD">
        <json:string key="@id"><xsl:value-of select="."/></json:string>
    </xsl:template>

    <xsl:template match="@rdf:resource" mode="ac:JSON-LD">
        <xsl:param name="key" as="xs:string?"/>

        <json:map>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <json:string key="@id"><xsl:value-of select="."/></json:string>
        </json:map>
    </xsl:template>

    <xsl:template match="@rdf:nodeID" mode="ac:JSON-LD">
        <json:string key="@id"><xsl:value-of select="'_:' || ."/></json:string>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="ac:JSON-LD" priority="1">
        <xsl:param name="key" as="xs:string?"/>

        <json:map>
            <xsl:if test="$key">
                <xsl:attribute name="key" select="$key"/>
            </xsl:if>

            <xsl:next-match/>
        </json:map>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID[count(key('predicates-by-object', .)) &lt;= 1]" mode="ac:JSON-LD" priority="2">
        <xsl:param name="key" as="xs:string?"/>
        <xsl:param name="traversed-ids" as="xs:string*"/>
        <xsl:variable name="bnode" select="key('resources', .)" as="element()?"/>
               
        <xsl:choose>
            <!-- loop if node not visited already -->
            <xsl:when test="not(. = $traversed-ids) and exists($bnode)">
                <xsl:apply-templates select="$bnode" mode="#current">
                    <xsl:with-param name="key" select="$key"/>
                    <xsl:with-param name="traversed-ids" select="(., $traversed-ids)"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match>
                    <xsl:with-param name="key" select="$key"/>
                </xsl:next-match>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:datatype" mode="ac:JSON-LD">
        <json:string key="@type"><xsl:value-of select="."/></json:string>
    </xsl:template>

    <xsl:template match="@xml:lang" mode="ac:JSON-LD">
        <json:string key="@language"><xsl:value-of select="."/></json:string>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:JSON-LDContext">
        <json:map key="{local-name()}">
            <!-- if property element has no prefix, output absolute property URI -->
            <json:string key="@id"><xsl:value-of select="if (not(prefix-from-QName(node-name()))) then concat(namespace-uri(), local-name()) else name()"/></json:string>
        </json:map>
    </xsl:template>

    <xsl:template match="rdf:type[@rdf:resource]" mode="ac:JSON-LDContext" priority="1"/>

    <xsl:template match="text()" mode="ac:JSON-LDContext"/>

</xsl:stylesheet>